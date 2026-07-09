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
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                        </svg>
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
                                        <div className="w-9 h-9 rounded-full bg-brand-accent/10 flex items-center justify-center">
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-brand-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                                            </svg>
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
