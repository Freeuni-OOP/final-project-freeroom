import useFriendsPanel from './useFriendsPanel';
import { Link } from 'react-router-dom';
import {RELATIONSHIP_STATUS, UNIVERSITY} from "@/utils";

const Spinner = () => (
  <div className="flex justify-center py-10">
    <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand-accent border-t-transparent" />
  </div>
);

const EmptyState = ({ message, sub }) => (
  <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-black/10 py-10 text-center">
    <p className="text-sm font-medium text-brand-ink/60">{message}</p>
    {sub && <p className="mt-1 text-xs text-brand-ink/40">{sub}</p>}
  </div>
);

const Avatar = ({ photoUrl, displayName, size = 'md' }) => {
  const dim = size === 'sm' ? 'h-9 w-9 text-xs' : 'h-10 w-10 text-sm';
  return photoUrl ? (
    <img
      src={photoUrl}
      alt={displayName}
      referrerPolicy="no-referrer"
      className={`${dim} rounded-full object-cover ring-2 ring-brand-accent/15 shrink-0`}
    />
  ) : (
    <div className={`${dim} flex shrink-0 items-center justify-center rounded-full bg-brand-accent/10 font-bold text-brand-accent-text ring-2 ring-brand-accent/15`}>
      {displayName?.[0]?.toUpperCase() ?? '?'}
    </div>
  );
};

export default function FriendsPanel() {
  const {
    activeTab,
    setActiveTab,
    requestSubTab,
    setRequestSubTab,
    searchQuery,
    setSearchQuery,
    searchResults,
    friends,
    incomingRequests,
    isLoadingFriends,
    isLoadingRequests,
    isSearching,
    hasSearched,
    actionPending,
    handleSendRequest,
    handleAccept,
    handleAcceptFromSearch,
    handleReject,
    university,
  } = useFriendsPanel();

  return (
    <div>
      <div className="mb-6 flex gap-1 rounded-xl bg-gray-100 p-1">
        <button
          onClick={() => setActiveTab('friends')}
          className={`flex-1 rounded-lg py-2 text-sm font-semibold transition-colors ${
            activeTab === 'friends'
              ? 'bg-white text-brand-ink shadow-sm'
              : 'text-brand-ink/50 hover:text-brand-ink/70'
          }`}
        >
          მეგობრები
          {friends.length > 0 && (
            <span className="ml-2 rounded-full bg-brand-accent/15 px-2 py-0.5 text-xs text-brand-accent-text">
              {friends.length}
            </span>
          )}
        </button>
        <button
          onClick={() => setActiveTab('requests')}
          className={`flex-1 rounded-lg py-2 text-sm font-semibold transition-colors ${
            activeTab === 'requests'
              ? 'bg-white text-brand-ink shadow-sm'
              : 'text-brand-ink/50 hover:text-brand-ink/70'
          }`}
        >
          მოთხოვნები
          {incomingRequests.length > 0 && (
            <span className="ml-2 rounded-full bg-red-100 px-2 py-0.5 text-xs text-red-600">
              {incomingRequests.length}
            </span>
          )}
        </button>
      </div>

      {activeTab === 'friends' && (
          <>
            {isLoadingFriends ? (
                <Spinner />
            ) : friends.length === 0 ? (
                <EmptyState
                    message="მეგობრები ჯერ არ გყავთ"
                    sub="დაუკავშირდით ჯგუფელებს, რათა ნახოთ ვინ სწავლობს ახლომახლო."
                />
            ) : (
                <div className="space-y-3">
                  {friends.map((friend) => (
                      <div
                          key={friend.id}
                          className="flex items-center gap-3 rounded-xl bg-gray-50 p-3 ring-1 ring-black/5"
                      >
                        <Link
                            to={`/profile/${friend.id}`}
                            className="flex min-w-0 flex-1 items-center gap-3"
                        >
                          <Avatar photoUrl={friend.photoUrl} displayName={friend.displayName} />
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-semibold text-brand-ink">
                              {friend.displayName}
                            </p>
                            {friend.occupancyLabel && (
                                <p className="mt-0.5 text-xs text-brand-ink/50">
                                  {friend.occupancyLabel}
                                </p>
                            )}
                          </div>
                        </Link>
                        {friend.hasActiveOccupancy && (
                            <span className="shrink-0 rounded-full bg-brand-accent/10 px-2.5 py-1 text-xs font-semibold text-brand-accent-text">
                ოთახშია
              </span>
                        )}
                      </div>
                  ))}
                </div>
            )}
          </>
      )}

      {activeTab === 'requests' && (
        <>
          <div className="mb-5 flex gap-1 rounded-xl bg-gray-100 p-1">
            <button
              onClick={() => setRequestSubTab('send')}
              className={`flex-1 rounded-lg py-1.5 text-xs font-semibold transition-colors ${
                requestSubTab === 'send'
                  ? 'bg-white text-brand-ink shadow-sm'
                  : 'text-brand-ink/50 hover:text-brand-ink/70'
              }`}
            >
              ძებნა
            </button>
            <button
              onClick={() => setRequestSubTab('received')}
              className={`flex-1 rounded-lg py-1.5 text-xs font-semibold transition-colors ${
                requestSubTab === 'received'
                  ? 'bg-white text-brand-ink shadow-sm'
                  : 'text-brand-ink/50 hover:text-brand-ink/70'
              }`}
            >
              მიღებული
              {incomingRequests.length > 0 && (
                <span className="ml-1.5 rounded-full bg-red-100 px-1.5 py-0.5 text-xs text-red-600">
                  {incomingRequests.length}
                </span>
              )}
            </button>
          </div>

          {requestSubTab === 'send' && (
            <>
              <div className="relative mb-4">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="სახელით მოძებნეთ..."
                  className="w-full rounded-xl border border-black/10 bg-gray-50 px-4 py-2.5 pr-10 text-sm text-brand-ink placeholder:text-brand-ink/30 outline-none transition-colors focus:border-brand-accent focus:ring-1 focus:ring-brand-accent"
                />
                {isSearching && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-brand-accent border-t-transparent" />
                  </div>
                )}
              </div>

              {searchQuery.trim().length > 0 && searchQuery.trim().length < 2 && (
                <p className="py-4 text-center text-xs text-brand-ink/40">
                  მინიმუმ 2 სიმბოლო
                </p>
              )}

              {hasSearched && searchResults.length === 0 && (
                <p className="py-4 text-center text-xs text-brand-ink/40">
                  მომხმარებელი ვერ მოიძებნა
                </p>
              )}

              {searchResults.length > 0 && (
                  <div className="space-y-2">
                    {searchResults.map((user) => (
                        <div
                            key={user.id}
                            className="flex items-center gap-3 rounded-xl bg-gray-50 p-3 ring-1 ring-black/5"
                        >
                          <Link
                              to={`/profile/${user.id}`}
                              className="flex min-w-0 flex-1 items-center gap-3"
                          >
                            <Avatar photoUrl={user.photoUrl} displayName={user.displayName} size="sm" />
                            <p className="min-w-0 flex-1 truncate text-sm font-semibold text-brand-ink">
                              {user.displayName}
                            </p>
                          </Link>

                          {user.relationshipStatus === RELATIONSHIP_STATUS.FRIENDS && (
                              <span className="shrink-0 rounded-full bg-brand-green/10 px-2.5 py-1 text-xs font-semibold text-brand-green">
            მეგობარი
          </span>
                          )}

                          {user.relationshipStatus === RELATIONSHIP_STATUS.PENDING_SENT && (
                              <span className="shrink-0 rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-brand-ink/40">
            გაგზავნილია
          </span>
                          )}

                          {user.relationshipStatus === RELATIONSHIP_STATUS.PENDING_RECEIVED && (
                              <button
                                  onClick={() => handleAcceptFromSearch(user.id)}
                                  disabled={actionPending.has(user.id)}
                                  className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold transition-colors disabled:opacity-50 ${
                                      university === UNIVERSITY.FREEUNI
                                          ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                                          : university === UNIVERSITY.AGRUNI
                                              ? 'bg-green-600 hover:bg-green-700 text-white'
                                              : 'bg-brand-ink hover:bg-brand-ink/90 text-white'
                                  }`}
                              >
                                დადასტურება
                              </button>
                          )}

                          {user.relationshipStatus === RELATIONSHIP_STATUS.NONE && (
                              <button
                                  onClick={() => handleSendRequest(user.id)}
                                  disabled={actionPending.has(user.id)}
                                  className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold transition-colors disabled:opacity-50 ${
                                      university === UNIVERSITY.FREEUNI
                                          ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                                          : university === UNIVERSITY.AGRUNI
                                              ? 'bg-green-600 hover:bg-green-700 text-white'
                                              : 'bg-brand-ink hover:bg-brand-ink/90 text-white'
                                  }`}
                              >
                                {actionPending.has(user.id) ? '...' : 'დამატება'}
                              </button>
                          )}
                        </div>
                    ))}
                  </div>
              )}
            </>
          )}

          {requestSubTab === 'received' && (
              <>
                {isLoadingRequests ? (
                    <Spinner />
                ) : incomingRequests.length === 0 ? (
                    <EmptyState message="შემოსული მოთხოვნები არ გაქვთ" />
                ) : (
                    <div className="space-y-3">
                      {incomingRequests.map((req) => (
                          <div
                              key={req.requestId}
                              className="flex items-center gap-3 rounded-xl bg-gray-50 p-3 ring-1 ring-black/5"
                          >
                            <Link
                                to={`/profile/${req.senderId}`}
                                className="flex min-w-0 flex-1 items-center gap-3"
                            >
                              <Avatar
                                  photoUrl={req.senderPhotoUrl}
                                  displayName={req.senderDisplayName}
                              />
                              <p className="min-w-0 flex-1 truncate text-sm font-semibold text-brand-ink">
                                {req.senderDisplayName}
                              </p>
                            </Link>
                            <div className="flex shrink-0 gap-2">
                              <button
                                  onClick={() => handleAccept(req.requestId)}
                                  disabled={actionPending.has(req.requestId)}
                                  className="rounded-full bg-brand-accent/10 px-3 py-1 text-xs font-semibold text-brand-accent-text transition-colors hover:bg-brand-accent/20 disabled:opacity-50"
                              >
                                დადასტურება
                              </button>
                              <button
                                  onClick={() => handleReject(req.requestId)}
                                  disabled={actionPending.has(req.requestId)}
                                  className="rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600 transition-colors hover:bg-red-100 disabled:opacity-50"
                              >
                                უარყოფა
                              </button>
                            </div>
                          </div>
                      ))}
                    </div>
                )}
              </>
          )}
        </>
      )}
    </div>
  );
}