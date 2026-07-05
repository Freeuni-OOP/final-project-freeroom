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
    updatePublicNote
} from '@/services/api/endpoints.js';
import { useNotification } from '@/context';
import { ROOM_STATUS } from '@/utils';

const ENV_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const CLEAN_BASE_URL = ENV_BASE_URL.endsWith('/') ? ENV_BASE_URL.slice(0, -1) : ENV_BASE_URL;

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
    const { showNotification } = useNotification();
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

    const chatContainerRef = useRef(null);
    const isFetchingOlder = useRef(false);
    const isMountedRef = useRef(true);

    useEffect(() => {
        isMountedRef.current = true;
        return () => {
            isMountedRef.current = false;
        };
    }, []);

    if (roomId !== prevRoomId) {
        setPrevRoomId(roomId);
        setIsAuthorized(null);
        setMessages([]);
        setNoteText(roomData?.currentOccupancy?.publicNote || '');
        setHasMore(true);
    }

    useEffect(() => {
        if (!roomId) return;

        let isMounted = true;

        getChatMessages(roomId)
            .then((data) => {
                if (isMounted) {
                    setMessages(data.sort((a, b) => a.id - b.id));
                    setIsAuthorized(true);
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
        if (roomData?.currentOccupancy?.publicNote !== undefined && roomId === prevRoomId) {
            setNoteText(roomData.currentOccupancy.publicNote || '');
        }
    }, [roomData?.currentOccupancy?.publicNote, roomId, prevRoomId]);

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
        let stompClient = null;

        if (roomId && isChatOpen) {
            const token = localStorage.getItem('token');
            const socketUrl = token ? `${CLEAN_BASE_URL}/ws?token=${token}` : `${CLEAN_BASE_URL}/ws`;
            const socket = new SockJS(socketUrl);

            stompClient = new Client({
                webSocketFactory: () => socket,
                onConnect: () => {
                    if (isMountedRef.current) {
                        setReloadTrigger((prev) => prev + 1);
                    }

                    stompClient.subscribe(`/topic/room/${roomId}`, (message) => {
                        const newMsg = JSON.parse(message.body);
                        if (isMountedRef.current) {
                            setMessages((prev) => {
                                if (prev.some(m => m.id === newMsg.id)) return prev;
                                return [...prev, newMsg].sort((a, b) => a.id - b.id);
                            });
                        }
                    });

                    stompClient.subscribe(`/topic/room/${roomId}/reload`, () => {
                        if (isMountedRef.current) {
                            setReloadTrigger((prev) => prev + 1);
                        }
                    });
                },
                onStompError: (err) => {
                    console.error(err);
                    showNotification({
                        message: 'ჩატთან კავშირი შეწყდა. შეტყობინებები დროებით ვერ განახლდება.',
                        type: 'error'
                    });
                },
                onWebSocketClose: () => {
                    if (isMountedRef.current && isChatOpen) {
                        showNotification({
                            message: 'სერვერთან კავშირი დაკარგულია. მიმდინარეობს ხელახლა დაკავშირება...',
                            type: 'info'
                        });
                    }
                }
            });
            stompClient.activate();
        }

        return () => {
            if (stompClient) stompClient.deactivate();
        };
    }, [roomId, isChatOpen]);

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
        setLoadingAction('requestJoin');
        try {
            await requestJoinRoom(roomId);
            showNotification({ message: 'მოთხოვნა გაიგზავნა წარმატებით', type: 'success' });
        } catch (err) {
            console.error(err);
            showNotification({ message: err.response?.data?.message || 'მოთხოვნა ვერ გაიგზავნა. მოთხოვნის გაგზავნა შესაძლებელია წუთში ერთხელ.', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    const handleApproveUser = async (targetUserId) => {
        setLoadingAction(`approve-${targetUserId}`);
        try {
            await approveJoinRequest(roomId, targetUserId);
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

    const handleUpdateNote = async () => {
        if (!modalData?.isMyOccupancy) return;
        setLoadingAction('updateNote');
        try {
            await updatePublicNote(roomId, noteText);
            showNotification({ message: 'სტატუსი განახლდა', type: 'success' });
            onReserveSuccess();
        } catch (err) {
            console.error(err);
            showNotification({ message: 'სტატუსის განახლება ვერ მოხერხდა', type: 'error' });
        } finally {
            if (isMountedRef.current) setLoadingAction(null);
        }
    };

    return {
        roomData: modalData,
        handleReserve,
        handleCancel,
        availableDurations,
        timeUntilNextLecture,
        isChatOpen,
        setIsChatOpen,
        messages,
        isAuthorized,
        messageText,
        setMessageText,
        handleSendMessage,
        handleRequestJoin,
        handleApproveUser,
        handleRejectUser,
        chatContainerRef,
        isLoadingOlder,
        handleScroll,
        noteText,
        setNoteText,
        handleUpdateNote,
        loadingAction
    };
};

export default useRoomModal;