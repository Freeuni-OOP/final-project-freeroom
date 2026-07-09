import useNotificationPanel from './useNotificationPanel';

export default function NotificationPanel({ onClose }) {
    const {
        persistentNotifications,
        unreadCount,
        isLoadingMore,
        hasMoreNotifications,
        handleClick,
        handleMarkAllRead,
        handleLoadMore,
        TYPE_ICONS,
        timeAgo
    } = useNotificationPanel(onClose);

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
            <div className="overflow-y-auto flex-1 pb-2">
                {persistentNotifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-12 text-gray-400 gap-2">
                        <span className="text-3xl">🔔</span>
                        <p className="text-sm font-medium">შეტყობინება არ არის</p>
                    </div>
                ) : (
                    <>
                        {persistentNotifications.map((notif) => (
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
                        ))}
                        
                        {hasMoreNotifications && (
                            <div className="pt-2 pb-1 flex justify-center">
                                <button
                                    onClick={handleLoadMore}
                                    disabled={isLoadingMore}
                                    className="px-4 py-1.5 text-xs font-semibold text-brand-ink/70 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors disabled:opacity-50 flex items-center gap-2"
                                >
                                    {isLoadingMore ? (
                                        <>
                                            <span className="w-3 h-3 border-2 border-brand-ink/30 border-t-brand-ink rounded-full animate-spin"></span>
                                            იტვირთება...
                                        </>
                                    ) : (
                                        'ჩატვირთე მეტი'
                                    )}
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
