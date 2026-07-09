import { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
    reserveRoom,
    cancelOccupancy,
    getChatMessages,
    sendChatMessage,
    requestJoinRoom,
    approveJoinRequest,
    rejectJoinRequest,
    updatePublicNote,
    kickUserFromRoom
} from '@/services/api/endpoints.js';
import { useNotification } from '@/context';
import { useRealtime } from '@/context';
import { ROOM_STATUS } from '@/utils';

const ENV_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const CLEAN_BASE_URL = ENV_BASE_URL.endsWith('/') ? ENV_BASE_URL.slice(0, -1) : ENV_BASE_URL;

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
    const { showNotification } = useNotification();
    const { setIsChatOpenGlobal } = useRealtime();
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [isAuthorized, setIsAuthorized] = useState(null);
    const [messageText, setMessageText] = useState('');
    const [noteText, setNoteText] = useState('');
    const [hasMore, setHasMore] = useState(true);
    const [isLoadingOlder, setIsLoadingOlder] = useState(false);
    const [prevRoomId, setPrevRoomId] = useState(roomId);
    const [reloadTrigger, setReloadTrigger] = useState(0);
    const [loadingAction, setLoadingAction] = useState(null);
    const [localKickedUsers, setLocalKickedUsers] = useState(new Set());
    const [localApprovedUsers, setLocalApprovedUsers] = useState(new Map());
    const [hasRequestedJoin, setHasRequestedJoin] = useState(false);

    const chatContainerRef = useRef(null);
    const usersMenuRef = useRef(null);
    const isFetchingOlder = useRef(false);
    const isMountedRef = useRef(true);
    const hasEditedNoteRef = useRef(false);
    const messagesRef = useRef(messages);

    const [showUsersMenu, setShowUsersMenu] = useState(false);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (usersMenuRef.current && !usersMenuRef.current.contains(e.target)) {
                setShowUsersMenu(false);
            }
        };
        if (showUsersMenu) document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [showUsersMenu]);

    useEffect(() => {
        isMountedRef.current = true;
        return () => {
            isMountedRef.current = false;
        };
    }, []);

    useEffect(() => {
        messagesRef.current = messages;
    }, [messages]);

    if (roomId !== prevRoomId) {
        setPrevRoomId(roomId);
        setIsAuthorized(null);
        setMessages([]);
        setNoteText(roomData?.currentOccupancy?.publicNote || '');
        setHasMore(true);
        setLocalKickedUsers(new Set());
        setLocalApprovedUsers(new Map());
        setHasRequestedJoin(false);
    }

    useEffect(() => {
        if (!roomId) return;

        let isMounted = true;

        getChatMessages(roomId)
            .then((data) => {
                if (isMounted) {
                    setMessages(data.sort((a, b) => a.id - b.id));
                    setIsAuthorized(true);
                    setHasRequestedJoin(false);
                }
            })
            .catch((err) => {
                if (isMounted) {
                    console.error(err);
                    setIsAuthorized(false);
                    setMessages([]);
                }
            });

        return () => {
            isMounted = false;
        };
    }, [roomId, reloadTrigger]);

    useEffect(() => {
        let interval = null;
        if (roomId && isAuthorized === false) {
            interval = setInterval(() => {
                setReloadTrigger(prev => prev + 1);
            }, 3000);
        }
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [roomId, isAuthorized]);

    useEffect(() => {
        hasEditedNoteRef.current = false;
    }, [roomId]);

    useEffect(() => {
        if (roomId !== prevRoomId || hasEditedNoteRef.current) return;
        setNoteText(roomData?.currentOccupancy?.publicNote || '');
    }, [roomData?.currentOccupancy?.publicNote, roomData?.currentOccupancy, roomId, prevRoomId]);

    useEffect(() => {
        if (isChatOpen && chatContainerRef.current && !isFetchingOlder.current) {
            setTimeout(() => {
                if (chatContainerRef.current && !isFetchingOlder.current) {
                    chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
                }
            }, 50);
        }
    }, [isChatOpen, messages]);

    useEffect(() => {
        setIsChatOpenGlobal(roomId, isChatOpen);
        return () => setIsChatOpenGlobal(roomId, false);
    }, [isChatOpen, setIsChatOpenGlobal, roomId]);

    const { subscribeToRoom } = useRealtime();

    useEffect(() => {
        if (!roomId) return;

        const handleMessage = (newMsg) => {
            if (isMountedRef.current) {
                if (newMsg.messageType === 'APPROVAL' && newMsg.message.startsWith("Approved access for ") && newMsg.message.endsWith(" to join.")) {
                    const targetNickname = newMsg.message.slice(20, -9);
                    const targetUserMsg = messagesRef.current.find(m => m.nickname === targetNickname);
                    if (targetUserMsg) {
                        setLocalApprovedUsers(prev => {
                            const map = new Map(prev);
                            map.set(targetUserMsg.author, {
                                id: targetUserMsg.author,
                                nickname: targetUserMsg.nickname,
                                photoUrl: targetUserMsg.photoUrl,
                                email: targetUserMsg.email
                            });
                            return map;
                        });
                        setLocalKickedUsers(prev => {
                            const set = new Set(prev);
                            set.delete(targetUserMsg.author);
                            return set;
                        });
                    }
                } else if (newMsg.messageType === 'TEXT' && newMsg.message.endsWith(" has been kicked from the room.")) {
                    const targetNickname = newMsg.message.slice(0, -31);
                    const targetUserMsg = messagesRef.current.find(m => m.nickname === targetNickname);
                    if (targetUserMsg) {
                        setLocalKickedUsers(prev => new Set(prev).add(targetUserMsg.author));
                        setLocalApprovedUsers(prev => {
                            const map = new Map(prev);
                            map.delete(targetUserMsg.author);
                            return map;
                        });
                    }
                }

                if (newMsg.messageType === 'APPROVAL') {
                    setReloadTrigger((prev) => prev + 1);
                }

                setMessages((prev) => {
                    let updated = prev;
                    if (newMsg.messageType === 'REQUEST') {
                        updated = updated.filter(m => !(m.messageType === 'REQUEST' && m.author === newMsg.author));
                    }
                    if (updated.some(m => m.id === newMsg.id)) {
                        return updated.map(m => m.id === newMsg.id ? newMsg : m);
                    }
                    return [...updated, newMsg].sort((a, b) => a.id - b.id);
                });
            }
        };

        const handleReload = () => {
            if (isMountedRef.current) {
                setReloadTrigger((prev) => prev + 1);
            }
        };

        const unsubscribe = subscribeToRoom(roomId, handleMessage, handleReload);

        if (isMountedRef.current) {
            setReloadTrigger((prev) => prev + 1);
        }

        return () => {
            unsubscribe();
        };
    }, [roomId, subscribeToRoom]);

    const loadOlderMessages = async () => {
        if (!roomId || isFetchingOlder.current || !hasMore || messages.length === 0) return;
        isFetchingOlder.current = true;
        setIsLoadingOlder(true);
        const container = chatContainerRef.current;
        const prevScrollHeight = container ? container.scrollHeight : 0;

        try {
            const oldestId = messages[0].id;
            const data = await getChatMessages(roomId, oldestId);

            if (!isMountedRef.current) return;

            if (data.length < 20) setHasMore(false);
            if (data.length > 0) {
                setMessages(prev => {
                    const existingIds = new Set(prev.map(m => m.id));
                    const uniqueOlder = data.filter(m => !existingIds.has(m.id));
                    return [...uniqueOlder, ...prev].sort((a, b) => a.id - b.id);
                });
                requestAnimationFrame(() => {
                    if (chatContainerRef.current) {
                        chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight - prevScrollHeight;
                    }
                    setTimeout(() => {
                        isFetchingOlder.current = false;
                    }, 50);
                });
            } else {
                isFetchingOlder.current = false;
            }
        } catch (err) {
            console.error(err);
            isFetchingOlder.current = false;
        } finally {
            if (isMountedRef.current) {
                setIsLoadingOlder(false);
            }
        }
    };

    const handleScroll = () => {
        if (chatContainerRef.current?.scrollTop === 0) loadOlderMessages();
    };

    const formatTime = (iso) => iso ? new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) : null;

    const timeUntilNextLecture = (roomData?.nextLecture?.startAt && roomData?.serverNow)
        ? Math.floor((new Date(roomData.nextLecture.startAt) - new Date(roomData.serverNow)) / 60000)
        : null;

    const getAvailableDurations = () => {
        if (timeUntilNextLecture === null) return [30, 60, 120];
        if (timeUntilNextLecture <= 0) return [];

        const presets = [30, 60, 120].filter((d) => d <= timeUntilNextLecture);

        if (!presets.includes(timeUntilNextLecture) && timeUntilNextLecture < 120) {
            presets.push(timeUntilNextLecture);
        }

        return presets.length > 0 ? presets : [timeUntilNextLecture];
    };

    const availableDurations = getAvailableDurations();

    const modalData = roomId ? {
        id: roomId,
        isFree: roomData?.status !== ROOM_STATUS.OCCUPIED,
        isReserved: roomData?.currentOccupancy != null,
        lectureName: roomData?.currentLecture?.title ?? null,
        lecturer: roomData?.currentLecture?.organizer ?? null,
        startTime: formatTime(roomData?.currentLecture?.startAt),
        endTime: formatTime(roomData?.currentLecture?.endAt),
        groupNumber: roomData?.currentLecture?.groupNumber ?? null,
        reservedBy: roomData?.currentOccupancy?.isMyOccupancy
            ? 'თქვენ'
            : (roomData?.currentOccupancy?.isFriendOccupancy || roomData?.currentOccupancy?.isPublicOccupancy)
                ? roomData?.currentOccupancy?.reserverDisplayName
                : 'სხვა სტუდენტი',
        reservedByPhotoUrl: (roomData?.currentOccupancy?.isFriendOccupancy || roomData?.currentOccupancy?.isPublicOccupancy)
            ? roomData?.currentOccupancy?.reserverPhotoUrl
            : null,
        reserverId: (roomData?.currentOccupancy?.isFriendOccupancy || roomData?.currentOccupancy?.isMyOccupancy || roomData?.currentOccupancy?.isPublicOccupancy)
            ? roomData?.currentOccupancy?.reserverId
            : null,
        isFriendOccupancy: roomData?.currentOccupancy?.isFriendOccupancy ?? false,
        reservedUntil: formatTime(roomData?.currentOccupancy?.expectedEndAt),
        nextLectureTitle: roomData?.nextLecture?.title ?? null,
        nextLectureStart: formatTime(roomData?.nextLecture?.startAt),
        nextLectureEnd: formatTime(roomData?.nextLecture?.endAt),
        isMyOccupancy: roomData?.currentOccupancy?.isMyOccupancy ?? false,
        publicNote: roomData?.currentOccupancy?.publicNote ?? null,
        capacity: roomData?.capacity ?? null,
    } : null;

    const handleReserve = async (durationMinutes) => {
        if (!modalData?.isFree) {
            showNotification({ message: 'ოთახი დაკავებულია', type: 'error' });
            return;
        }
        if (timeUntilNextLecture !== null && durationMinutes > timeUntilNextLecture) {
            showNotification({ message: 'არჩეული ხანგრძლივობა სცილდება შემდეგ ლექციამდე დარჩენილ დროს', type: 'error' });
            return;
        }
        setLoadingAction(`reserve-${durationMinutes}`);
        try {
            await reserveRoom(roomId, durationMinutes, noteText);
            onClose();
            onReserveSuccess();
            showNotification({ message: `ოთახი ${roomId} დაჯავშნილია ${durationMinutes} წუთით`, type: 'success' });
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.error || 'დაჯავშნა ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleCancel = async () => {
        if (!modalData?.isMyOccupancy) {
            showNotification({ message: 'მხოლოდ საკუთარი ჯავშნის გაუქმება შეგიძლიათ', type: 'error' });
            return;
        }
        setLoadingAction('cancel');
        try {
            await cancelOccupancy(roomId);
            onClose();
            onReserveSuccess();
            showNotification({ message: `ოთახი ${roomId} გათავისუფლდა`, type: 'success' });
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'გაუქმება ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleSendMessage = async () => {
        if (!messageText.trim()) return;
        setLoadingAction('sendMessage');
        try {
            await sendChatMessage(roomId, messageText);
            if (isMountedRef.current) {
                setMessageText('');
            }
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'შეტყობინება ვერ გაიგზავნა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleRequestJoin = async () => {
        if (isAuthorized) {
            return;
        }
        setLoadingAction('requestJoin');
        try {
            await requestJoinRoom(roomId);
            setHasRequestedJoin(true);
            showNotification({ message: 'მოთხოვნა გაიგზავნა წარმატებით', type: 'success' });
            setReloadTrigger(prev => prev + 1);
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'მოთხოვნა ვერ გაიგზავნა.', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleApproveUser = async (targetUserId) => {
        setLoadingAction(`approve-${targetUserId}`);
        try {
            const targetMsg = messages.find(m => m.author === targetUserId && m.messageType === 'REQUEST');
            await approveJoinRequest(roomId, targetUserId);

            if (targetMsg) {
                setLocalApprovedUsers(prev => {
                    const newMap = new Map(prev);
                    newMap.set(targetUserId, {
                        id: targetMsg.author,
                        nickname: targetMsg.nickname,
                        photoUrl: targetMsg.photoUrl,
                        email: targetMsg.email
                    });
                    return newMap;
                });
            }

            setLocalKickedUsers(prev => {
                const newSet = new Set(prev);
                newSet.delete(targetUserId);
                return newSet;
            });

            showNotification({ message: 'მომხმარებელი წარმატებით დაემატა', type: 'success' });
            if (isMountedRef.current) {
                setReloadTrigger((prev) => prev + 1);
            }
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'დამტკიცება ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleRejectUser = async (targetUserId) => {
        setLoadingAction(`reject-${targetUserId}`);
        try {
            await rejectJoinRequest(roomId, targetUserId);
            showNotification({ message: 'მოთხოვნა უარყოფილია', type: 'info' });
            if (isMountedRef.current) {
                setReloadTrigger((prev) => prev + 1);
            }
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'უარყოფა ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleKickUser = async (targetUserId) => {
        setLoadingAction(`kick-${targetUserId}`);
        try {
            await kickUserFromRoom(roomId, targetUserId);

            setLocalKickedUsers(prev => new Set(prev).add(targetUserId));
            setLocalApprovedUsers(prev => {
                const newMap = new Map(prev);
                newMap.delete(targetUserId);
                return newMap;
            });

            showNotification({ message: 'მომხმარებელი გაძევებულია ჩათიდან', type: 'info' });
            if (isMountedRef.current) {
                setReloadTrigger((prev) => prev + 1);
            }
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'გაძევება ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleUpdateNote = async () => {
        if (!modalData?.isMyOccupancy) return;
        setLoadingAction('updateNote');
        try {
            await updatePublicNote(roomId, noteText);
            hasEditedNoteRef.current = false;
            showNotification({ message: 'სტატუსი განახლდა', type: 'success' });
            onReserveSuccess();
        } catch (err) {
            console.error(err);
            showNotification({ message: 'სტატუსის განახლება ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const processedMessages = (() => {
        const requestsByUser = new Map();
        messages.forEach(msg => {
            if (msg.messageType === 'REQUEST') {
                requestsByUser.set(msg.author, msg.id);
            }
        });
        return messages.filter(msg => {
            if (msg.messageType === 'REQUEST') {
                return requestsByUser.get(msg.author) === msg.id;
            }
            return true;
        });
    })();

    const chatUsers = (() => {
        const usersById = new Map();
        const statusByNickname = new Map();

        messages.forEach(msg => {
            if (msg.author && msg.nickname) {
                usersById.set(msg.author, {
                    id: msg.author,
                    nickname: msg.nickname,
                    photoUrl: msg.photoUrl,
                    email: msg.email
                });
            }

            const text = msg.message || "";

            if (msg.messageType === 'REQUEST') {
                if (!statusByNickname.has(msg.nickname)) {
                    statusByNickname.set(msg.nickname, 'REQUESTING');
                }
            } else if (msg.messageType === 'APPROVAL' && text.startsWith("Approved access for ") && text.endsWith(" to join.")) {
                const targetNickname = text.slice(20, -9);
                statusByNickname.set(targetNickname, 'ACTIVE');
            } else if (msg.messageType === 'TEXT' && text.endsWith(' has been kicked from the room.')) {
                const targetNickname = text.slice(0, -31);
                statusByNickname.set(targetNickname, 'KICKED');
            } else if (msg.messageType === 'TEXT') {
                statusByNickname.set(msg.nickname, 'ACTIVE');
            }
        });

        const activeUsersMap = new Map();

        if (roomData?.currentOccupancy?.reserverId) {
            const reserverId = roomData.currentOccupancy.reserverId;
            activeUsersMap.set(reserverId, {
                id: reserverId,
                nickname: roomData.currentOccupancy.reserverDisplayName || 'ოთახის მფლობელი',
                photoUrl: roomData.currentOccupancy.reserverPhotoUrl,
                email: ''
            });
        }

        for (const [id, user] of usersById.entries()) {
            if (statusByNickname.get(user.nickname) === 'ACTIVE') {
                activeUsersMap.set(id, user);
            }
        }

        localApprovedUsers.forEach((user, id) => {
            activeUsersMap.set(id, user);
        });

        for (const [id, user] of usersById.entries()) {
            if (statusByNickname.get(user.nickname) === 'KICKED') {
                activeUsersMap.delete(id);
            }
        }
        localKickedUsers.forEach(id => {
            activeUsersMap.delete(id);
        });

        return Array.from(activeUsersMap.values());
    })();

    return {
        roomData: modalData,
        handleReserve,
        handleCancel,
        availableDurations,
        timeUntilNextLecture,
        isChatOpen,
        setIsChatOpen,
        messages: processedMessages,
        chatUsers,
        isAuthorized,
        messageText,
        setMessageText,
        handleSendMessage,
        handleRequestJoin,
        handleApproveUser,
        handleRejectUser,
        handleKickUser,
        chatContainerRef,
        isLoadingOlder,
        handleScroll,
        noteText,
        setNoteText,
        handleUpdateNote,
        loadingAction,
        hasEditedNoteRef,
        hasRequestedJoin,
        showUsersMenu,
        setShowUsersMenu,
        usersMenuRef
    };
};

export default useRoomModal;