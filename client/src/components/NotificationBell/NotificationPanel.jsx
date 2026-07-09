import { useNotification } from '@/context';
import { useNavigate } from 'react-router-dom';

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

export default function NotificationPanel({ onClose }) {
    const { persistentNotifications, unreadCount, markRead, markAllRead } = useNotification();
    const navigate = useNavigate();

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

    return (
        <div className="w-80 max-h-[480px] flex flex-col rounded-2xl bg-white border border-gray-100 shadow-2xl overflow-hidden animate-in">
            {/* Header */}
            <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 shrink-0">
                <h3 className="text-sm font-bold text-brand-ink">შეტყობინებები</h3>
                {unreadCount > 0 && (
                    <button
                        onClick={handleMarkAllRead}
                        className="text-xs font-semibold text-brand-accent-text hover:underline"
                    >
                        ყველის წაკითხვა
                    </button>
                )}
            </div>

            {/* List */}
            <div className="overflow-y-auto flex-1">
                {persistentNotifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-gray-400 gap-2">
                        <span className="text-3xl">🔔</span>
                        <p className="text-sm font-medium">შეტყობინება არ არის</p>
                    </div>
                ) : (
                    persistentNotifications.map((notif) => (
                        <button
                            key={notif.id}
                            onClick={() => handleClick(notif)}
                            className={`w-full flex items-start gap-3 px-4 py-3 text-left hover:bg-gray-50 transition-colors border-b border-gray-50 last:border-0 ${!notif.isRead ? 'bg-brand-accent/5' : ''}`}
                        >
                            {/* Actor photo or emoji */}
                            <div className="shrink-0 mt-0.5">
                                {notif.actorPhotoUrl ? (
                                    <img
                                        src={notif.actorPhotoUrl}
                                        alt=""
                                        className="w-9 h-9 rounded-full object-cover"
                                    />
                                ) : (
                                    <div className="w-9 h-9 rounded-full bg-brand-accent/10 flex items-center justify-center text-lg">
                                        {TYPE_ICONS[notif.type] ?? '🔔'}
                                    </div>
                                )}
                            </div>

                            <div className="flex-1 min-w-0">
                                <p className={`text-xs leading-snug text-brand-ink ${!notif.isRead ? 'font-semibold' : 'font-medium'}`}>
                                    {notif.message}
                                </p>
                                <p className="text-[10px] text-gray-400 mt-0.5">
                                    {timeAgo(notif.createdAt)}
                                </p>
                            </div>

                            {/* Unread dot */}
                            {!notif.isRead && (
                                <div className="shrink-0 mt-1.5 w-2 h-2 rounded-full bg-brand-accent" />
                            )}
                        </button>
                    ))
                )}
            </div>
        </div>
    );
}
