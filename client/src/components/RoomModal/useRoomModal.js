import { useState, useEffect, useCallback } from 'react';
import {
    reserveRoom,
    cancelOccupancy,
    getChatMessages,
    sendChatMessage,
    requestJoinRoom,
    approveJoinRequest,
    rejectJoinRequest
} from '@/services/api/endpoints.js';
import { useNotification } from '@/context';

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
    const { showNotification } = useNotification();
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [isAuthorized, setIsAuthorized] = useState(null);
    const [messageText, setMessageText] = useState('');
    const [prevRoomId, setPrevRoomId] = useState(roomId);

    if (roomId !== prevRoomId) {
        setPrevRoomId(roomId);
        setIsAuthorized(null);
        setMessages([]);
    }

    const formatTime = (iso) => {
        if (!iso) return null;
        return new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    };

    const isMyOccupancy = roomData?.currentOccupancy?.isMyOccupancy ?? false;

    const modalData = roomId
        ? {
            id: roomId,
            isFree: roomData?.status !== 'occupied',
            isReserved: roomData?.currentOccupancy != null,
            lectureName: roomData?.currentLecture?.title ?? null,
            lecturer: roomData?.currentLecture?.organizer ?? null,
            startTime: formatTime(roomData?.currentLecture?.startAt),
            endTime: formatTime(roomData?.currentLecture?.endAt),
            groupNumber: roomData?.currentLecture?.groupNumber ?? null,
            reservedBy: isMyOccupancy
                ? (roomData?.currentOccupancy?.reserverDisplayName ?? 'თქვენ')
                : "Not Your Friend",
            reservedUntil: formatTime(roomData?.currentOccupancy?.expectedEndAt),
            nextLectureTitle: roomData?.nextLecture?.title ?? null,
            nextLectureStart: formatTime(roomData?.nextLecture?.startAt),
            nextLectureEnd:   formatTime(roomData?.nextLecture?.endAt),
            isMyOccupancy,
            capacity: roomData?.capacity ?? null,
        }
        : null;

    const loadChat = useCallback(async () => {
        if (!roomId) return;
        try {
            const data = await getChatMessages(roomId);
            setMessages(data);
            setIsAuthorized(true);
        } catch (err) {
            if (err.response?.status === 403 || err.response?.status === 500) {
                setIsAuthorized(false);
                setMessages([]);
            }
        }
    }, [roomId]);

    useEffect(() => {
        if (roomId) {
            const timeout = setTimeout(() => {
                loadChat();
            }, 0);
            return () => clearTimeout(timeout);
        }
    }, [roomId, loadChat]);

    useEffect(() => {
        if (roomId && isChatOpen && isAuthorized) {
            const timeout = setTimeout(loadChat, 0);
            const interval = setInterval(loadChat, 4000);
            return () => {
                clearTimeout(timeout);
                clearInterval(interval);
            };
        }
    }, [roomId, isChatOpen, loadChat, isAuthorized]);

    const handleReserve = async (durationMinutes) => {
        if(!modalData?.isFree) {
            showNotification({ message: 'ოთახი დაკავებულია', type: 'error' });
            return;
        }
        try {
            await reserveRoom(roomData.id, durationMinutes);
            onClose();
            onReserveSuccess();
            showNotification({ message: `ოთახი ${roomId} დაჯავშნილია ${durationMinutes} წუთით`, type: 'success' });
        } catch (err) {
            showNotification({ message: err.response?.data?.error || 'დაჯავშნა ვერ მოხერხდა', type: 'error' });
        }
    };

    const handleCancel = async () => {
        if (!modalData?.isMyOccupancy) {
            showNotification({ message: 'მხოლოდ საკუთარი ჯავშნის გაუქმება შეგიძლიათ', type: 'error' });
            return;
        }
        try {
            await cancelOccupancy(roomData.id);
            onClose();
            onReserveSuccess();
            showNotification({ message: `ოთახი ${roomId} გათავისუფლდა`, type: 'success' });
        } catch (err) {
            showNotification({ message: err.response?.data?.message || 'გაუქმება ვერ მოხერხდა', type: 'error' });
        }
    };

    const handleSendMessage = async () => {
        if (!messageText.trim()) return;
        try {
            await sendChatMessage(roomId, messageText);
            setMessageText('');
            loadChat();
        } catch (err) {
            showNotification({ message: err.response?.data?.message || 'შეტყობინება ვერ გაიგზავნა', type: 'error' });
        }
    };

    const handleRequestJoin = async () => {
        try {
            await requestJoinRoom(roomId);
            showNotification({ message: 'მოთხოვნა გაიგზავნა წარმატებით', type: 'success' });
        } catch (err) {
            showNotification({ message: err.response?.data?.message || 'მოთხოვნა ვერ გაიგზავნა. მოთხოვნის გაგზავნა შესაძლებელია წუთში ერთხელ.', type: 'error' });
        }
    };

    const handleApproveUser = async (targetUserId) => {
        try {
            await approveJoinRequest(roomId, targetUserId);
            showNotification({ message: 'მომხმარებელი წარმატებით დაემატა', type: 'success' });
            loadChat();
        } catch (err) {
            showNotification({ message: err.response?.data?.message || 'დამტკიცება ვერ მოხერხდა', type: 'error' });
        }
    };

    const handleRejectUser = async (targetUserId) => {
        try {
            await rejectJoinRequest(roomId, targetUserId);
            showNotification({ message: 'მოთხოვნა უარყოფილია', type: 'info' });
            loadChat();
        } catch (err) {
            showNotification({ message: err.response?.data?.message || 'უარყოფა ვერ მოხერხდა', type: 'error' });
        }
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
        handleRejectUser
    };
};

export default useRoomModal;