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
    rejectJoinRequest
} from '@/services/api/endpoints.js';

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [isAuthorized, setIsAuthorized] = useState(null);
    const [messageText, setMessageText] = useState('');
    const [hasMore, setHasMore] = useState(true);
    const [isLoadingOlder, setIsLoadingOlder] = useState(false);

    // Track previous ID for render-phase resets
    const [prevRoomId, setPrevRoomId] = useState(roomId);
    // Declarative trigger for WebSocket reloads
    const [reloadTrigger, setReloadTrigger] = useState(0);

    const chatContainerRef = useRef(null);
    const isFetchingOlder = useRef(false);

    // 1. Reset state during render when the ID changes
    if (roomId !== prevRoomId) {
        setPrevRoomId(roomId);
        setIsAuthorized(null);
        setMessages([]);
        setHasMore(true);
    }

    // 2. Fetch messages using standard Promise callbacks (satisfies the IDE)
    useEffect(() => {
        if (!roomId) return;

        let isMounted = true; // Prevents race conditions if closed quickly

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
    }, [roomId, reloadTrigger]); // Re-runs when roomId or reloadTrigger changes

    // 3. Auto-scroll chat to bottom
    useEffect(() => {
        if (isChatOpen && chatContainerRef.current && !isFetchingOlder.current) {
            setTimeout(() => {
                if (chatContainerRef.current && !isFetchingOlder.current) {
                    chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
                }
            }, 50);
        }
    }, [isChatOpen, messages]);

    // 4. WebSocket connection
    useEffect(() => {
        let stompClient = null;

        if (roomId && isChatOpen) {
            const token = localStorage.getItem('token');
            const socketUrl = token ? `http://localhost:8080/ws?token=${token}` : 'http://localhost:8080/ws';
            const socket = new SockJS(socketUrl);

            stompClient = new Client({
                webSocketFactory: () => socket,
                onConnect: () => {
                    // Trigger a fetch to ensure we have the latest on connect
                    setReloadTrigger((prev) => prev + 1);

                    stompClient.subscribe(`/topic/room/${roomId}`, (message) => {
                        const newMsg = JSON.parse(message.body);
                        setMessages((prev) => {
                            if (prev.some(m => m.id === newMsg.id)) return prev;
                            return [...prev, newMsg].sort((a, b) => a.id - b.id);
                        });
                    });

                    stompClient.subscribe(`/topic/room/${roomId}/reload`, () => {
                        // Simply increment the trigger to force the fetch effect to run
                        setReloadTrigger((prev) => prev + 1);
                    });
                },
                onStompError: (err) => console.error(err),
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
            setIsLoadingOlder(false);
        }
    };

    const handleScroll = () => {
        if (chatContainerRef.current?.scrollTop === 0) loadOlderMessages();
    };

    const formatTime = (iso) => iso ? new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) : null;

    const modalData = roomId ? {
        id: roomId,
        isFree: roomData?.status !== 'occupied',
        isReserved: roomData?.currentOccupancy != null,
        lectureName: roomData?.currentLecture?.title ?? null,
        lecturer: roomData?.currentLecture?.organizer ?? null,
        startTime: formatTime(roomData?.currentLecture?.startAt),
        endTime: formatTime(roomData?.currentLecture?.endAt),
        groupNumber: roomData?.currentLecture?.groupNumber ?? null,
        reservedBy: roomData?.currentOccupancy?.isMyOccupancy ? 'თქვენ' : "Not Your Friend",
        reservedUntil: formatTime(roomData?.currentOccupancy?.expectedEndAt),
        nextLectureTitle: roomData?.nextLecture?.title ?? null,
        nextLectureStart: formatTime(roomData?.nextLecture?.startAt),
        nextLectureEnd: formatTime(roomData?.nextLecture?.endAt),
        isMyOccupancy: roomData?.currentOccupancy?.isMyOccupancy ?? false,
        capacity: roomData?.capacity ?? null,
    } : null;

    const handleReserve = async (duration) => {
        if (!modalData?.isFree) return alert('ოთახი დაკავებულია');
        try {
            await reserveRoom(roomData.id, duration);
            onClose();
            onReserveSuccess();
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.error || 'დაჯავშნა ვერ მოხერხდა');
        }
    };

    const handleCancel = async () => {
        try {
            await cancelOccupancy(roomData.id);
            onClose();
            onReserveSuccess();
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.message || 'გაუქმება ვერ მოხერხდა');
        }
    };

    const handleSendMessage = async () => {
        if (!messageText.trim()) return;
        try {
            await sendChatMessage(roomId, messageText);
            setMessageText('');
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.message || 'შეტყობინება ვერ გაიგზავნა');
        }
    };

    const handleRequestJoin = async () => {
        try {
            await requestJoinRoom(roomId);
            alert('მოთხოვნა გაიგზავნა');
        } catch (err) {
            console.error(err);
            alert('მოთხოვნა ვერ გაიგზავნა');
        }
    };

    const handleApproveUser = async (userId) => {
        try { await approveJoinRequest(roomId, userId); }
        catch (err) { console.error(err); alert('დამტკიცება ვერ მოხერხდა'); }
    };

    const handleRejectUser = async (userId) => {
        try { await rejectJoinRequest(roomId, userId); }
        catch (err) { console.error(err); alert('უარყოფა ვერ მოხერხდა'); }
    };

    return {
        roomData: modalData,
        handleReserve,
        handleCancel,
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
        handleScroll
    };
};

export default useRoomModal;