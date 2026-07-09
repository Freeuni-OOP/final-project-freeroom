import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotification } from '@/context';

const TYPE_ICONS = {
    FRIEND_REQUEST_RECEIVED: '👥',
    FRIEND_REQUEST_ACCEPTED: '✅',
    FRIEND_REQUEST_REJECTED: '❌',
    CHAT_JOIN_REQUEST: '🚪',
    CHAT_JOIN_APPROVED: '🔓',
    CHAT_JOIN_REJECTED: '🚫',
};

function timeAgo(isoString) {
    if (!isoString) return '';
    const diff = Math.floor((Date.now() - new Date(isoString).getTime()) / 1000);
    if (diff < 60) return 'ახლა';
    if (diff < 3600) return `${Math.floor(diff / 60)} წ წინ`;
    if (diff < 86400) return `${Math.floor(diff / 3600)} სთ წინ`;
    return `${Math.floor(diff / 86400)} დ წინ`;
}

const useNotificationPanel = (onClose) => {
    const { 
        persistentNotifications, 
        unreadCount, 
        markRead, 
        markAllRead, 
        fetchNotifications, 
        hasMoreNotifications, 
        notificationPage 
    } = useNotification();
    
    const navigate = useNavigate();
    const [isLoadingMore, setIsLoadingMore] = useState(false);

    const handleClick = async (notif) => {
        if (!notif.isRead) await markRead(notif.id);
        onClose();

        if (notif.type === 'FRIEND_REQUEST_RECEIVED' || notif.type === 'FRIEND_REQUEST_ACCEPTED' || notif.type === 'FRIEND_REQUEST_REJECTED') {
            if (notif.actorId) navigate(`/profile/${notif.actorId}`);
        } else if (notif.type === 'CHAT_JOIN_REQUEST' || notif.type === 'CHAT_JOIN_APPROVED' || notif.type === 'CHAT_JOIN_REJECTED') {
            navigate('/floors');
        }
    };

    const handleMarkAllRead = async () => {
        await markAllRead();
    };

    const handleLoadMore = async () => {
        setIsLoadingMore(true);
        await fetchNotifications(notificationPage + 1);
        setIsLoadingMore(false);
    };

    return {
        persistentNotifications,
        unreadCount,
        isLoadingMore,
        hasMoreNotifications,
        handleClick,
        handleMarkAllRead,
        handleLoadMore,
        TYPE_ICONS,
        timeAgo
    };
};

export default useNotificationPanel;
