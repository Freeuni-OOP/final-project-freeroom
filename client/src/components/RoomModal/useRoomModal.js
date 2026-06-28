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

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
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
            alert('ოთახი დაკავებულია');
            return;
        }
        try {
            await reserveRoom(roomData.id, durationMinutes);
            onClose();
            onReserveSuccess();
            alert(`ოთახი ${roomId} დაჯავშნილია ${durationMinutes} წუთით`);
        } catch (err) {
            alert(err.response?.data?.error || 'დაჯავშნა ვერ მოხერხდა');
        }
    };

    const handleCancel = async () => {
        if (!modalData?.isMyOccupancy) {
            alert('მხოლოდ საკუთარი ჯავშნის გაუქმება შეგიძლიათ');
            return;
        }
        try {
            await cancelOccupancy(roomData.id);
            onClose();
            onReserveSuccess();
            alert(`ოთახი ${roomId} გათავისუფლდა`);
        } catch (err) {
            alert(err.response?.data?.message || 'გაუქმება ვერ მოხერხდა');
        }
    };

    const handleSendMessage = async () => {
        if (!messageText.trim()) return;
        try {
            await sendChatMessage(roomId, messageText);
            setMessageText('');
            loadChat();
        } catch (err) {
            alert(err.response?.data?.message || 'შეტყობინება ვერ გაიგზავნა');
        }
    };

    const handleRequestJoin = async () => {
        try {
            await requestJoinRoom(roomId);
            alert('მოთხოვნა გაიგზავნა წარმატებით');
        } catch (err) {
            alert(err.response?.data?.message || 'მოთხოვნა ვერ გაიგზავნა. მოთხოვნის გაგზავნა შესაძლებელია წუთში ერთხელ.');
        }
    };

    const handleApproveUser = async (targetUserId) => {
        try {
            await approveJoinRequest(roomId, targetUserId);
            alert('მომხმარებელი წარმატებით დაემატა');
            loadChat();
        } catch (err) {
            alert(err.response?.data?.message || 'დამტკიცება ვერ მოხერხდა');
        }
    };

    const handleRejectUser = async (targetUserId) => {
        try {
            await rejectJoinRequest(roomId, targetUserId);
            alert('მოთხოვნა უარყოფილია');
            loadChat();
        } catch (err) {
            alert(err.response?.data?.message || 'უარყოფა ვერ მოხერხდა');
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