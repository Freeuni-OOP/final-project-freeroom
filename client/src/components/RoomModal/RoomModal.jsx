import { Link } from 'react-router-dom';
import useRoomModal from './useRoomModal';

const ButtonSpinner = () => (
    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-current inline-block" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
);

export default function RoomModal({ roomId, roomData, onClose, onReserveSuccess }) {

  const {
    roomData: modalData,
    handleReserve,
    handleCancel,
    availableDurations,
    isChatOpen,
    setIsChatOpen,
    messages,
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
    hasEditedNoteRef,
    handleUpdateNote,
    loadingAction,
    hasRequestedJoin,
    showUsersMenu,
    setShowUsersMenu,
    usersMenuRef
  } = useRoomModal(roomId, roomData, onClose, onReserveSuccess);

  if (!roomId || !modalData) return null;

  return (
      <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4"
          onClick={onClose}
      >
        <div
            className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden relative cursor-default"
            onClick={(e) => e.stopPropagation()}
        >
          <button
              onClick={onClose}
              className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 transition-colors cursor-pointer z-[60]"
          >
            ✕
          </button>

          <div className="p-6">
            {!isChatOpen ? (
                <>
                  <div className="flex items-center gap-3 mb-6 pr-8">
                    <h2 className="text-2xl font-bold text-gray-900">ოთახი {roomId}</h2>
                    <span className={`px-3 py-1 rounded-full text-xs font-bold tracking-wide uppercase ${modalData.isFree ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                    }`}>
                  {modalData.isFree ? 'თავისუფალი' : 'დაკავებული'}
                </span>
                  </div>

                  <div className="space-y-6">
                    {!modalData.isFree ? (
                        <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                          {modalData.isReserved && modalData.isMyOccupancy ? (
                              <div className="bg-brand-accent-light rounded-xl p-5 border border-brand-accent">
                                <p className="text-sm font-bold text-brand-accent-text mb-3">🔑 თქვენი ოთახი</p>
                                <div className="grid grid-cols-2 gap-4 mb-4">
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დაჯავშნა</p>
                                    {modalData.reserverId ? (
                                        <Link to={`/profile/${modalData.reserverId}`} className="flex items-center gap-2 hover:opacity-80 transition-opacity cursor-pointer">
                                          {modalData.reservedByPhotoUrl && (
                                              <img
                                                  src={modalData.reservedByPhotoUrl}
                                                  alt={modalData.reservedBy}
                                                  referrerPolicy="no-referrer"
                                                  className="h-6 w-6 rounded-full object-cover ring-1 ring-black/10"
                                              />
                                          )}
                                          <p className="font-medium text-gray-900">{modalData.reservedBy || 'სხვა სტუდენტი'}</p>
                                        </Link>
                                    ) : (
                                        <div className="flex items-center gap-2">
                                          {modalData.reservedByPhotoUrl && (
                                              <img
                                                  src={modalData.reservedByPhotoUrl}
                                                  alt={modalData.reservedBy}
                                                  referrerPolicy="no-referrer"
                                                  className="h-6 w-6 rounded-full object-cover ring-1 ring-black/10"
                                              />
                                          )}
                                          <p className="font-medium text-gray-900">{modalData.reservedBy || 'სხვა სტუდენტი'}</p>
                                        </div>
                                    )}
                                  </div>
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">თავისუფლდება</p>
                                    <p className="font-medium text-gray-900">{modalData.reservedUntil ?? '—'}</p>
                                  </div>
                                </div>
                                <div className="mb-4">
                                  <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">სტატუსი</p>
                                  <div className="flex gap-2">
                                    <input
                                        type="text"
                                        maxLength={40}
                                        value={noteText}
                                        onChange={(e) => {
                                          hasEditedNoteRef.current = true
                                          setNoteText(e.target.value)
                                        }}
                                        placeholder="არასავალდებულო"
                                        className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-accent bg-white"
                                    />
                                    {noteText !== (modalData.publicNote || '') && (
                                        <button
                                            disabled={loadingAction != null}
                                            onClick={handleUpdateNote}
                                            className="bg-brand-accent text-white px-3 py-2 rounded-lg text-sm font-semibold transition-colors disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                                        >
                                          {loadingAction === 'updateNote' && <ButtonSpinner />}
                                          შენახვა
                                        </button>
                                    )}
                                  </div>
                                </div>
                                <button
                                    disabled={loadingAction != null}
                                    onClick={handleCancel}
                                    className="w-full bg-red-500 hover:bg-red-600 text-white font-semibold py-3 rounded-lg transition-colors mb-2 disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                                >
                                  {loadingAction === 'cancel' && <ButtonSpinner />}
                                  გაუქმება
                                </button>
                                <button
                                    onClick={() => setIsChatOpen(true)}
                                    className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 rounded-lg transition-colors"
                                >
                                  ჩათის გახსნა
                                </button>
                              </div>
                          ) : modalData.isReserved ? (
                              <>
                                <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-3">დაჯავშნილია</p>
                                <div className="grid grid-cols-2 gap-4 mb-4">
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დაჯავშნა</p>
                                    {modalData.reserverId ? (
                                        <Link to={`/profile/${modalData.reserverId}`} className="flex items-center gap-2 hover:opacity-80 transition-opacity cursor-pointer">
                                          {modalData.reservedByPhotoUrl && (
                                              <img
                                                  src={modalData.reservedByPhotoUrl}
                                                  alt={modalData.reservedBy}
                                                  referrerPolicy="no-referrer"
                                                  className="h-6 w-6 rounded-full object-cover ring-1 ring-black/10"
                                              />
                                          )}
                                          <p className="font-medium text-gray-900">{modalData.reservedBy || 'სხვა სტუდენტი'}</p>
                                        </Link>
                                    ) : (
                                        <div className="flex items-center gap-2">
                                          {modalData.reservedByPhotoUrl && (
                                              <img
                                                  src={modalData.reservedByPhotoUrl}
                                                  alt={modalData.reservedBy}
                                                  referrerPolicy="no-referrer"
                                                  className="h-6 w-6 rounded-full object-cover ring-1 ring-black/10"
                                              />
                                          )}
                                          <p className="font-medium text-gray-900">{modalData.reservedBy || 'სხვა სტუდენტი'}</p>
                                        </div>
                                    )}
                                  </div>
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">თავისუფლდება</p>
                                    <p className="font-medium text-gray-900">{modalData.reservedUntil ?? '—'}</p>
                                  </div>
                                </div>
                                {modalData.publicNote && (
                                    <div className="mb-4 bg-white p-3 rounded-lg border border-gray-100">
                                      <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">სტატუსი</p>
                                      <p className="text-sm text-gray-800">{modalData.publicNote}</p>
                                    </div>
                                )}
                                {isAuthorized === null ? (
                                    <div className="w-full bg-gray-100 text-gray-400 font-semibold py-3 rounded-lg text-center text-sm animate-pulse">
                                      მოწმდება წვდომა...
                                    </div>
                                ) : isAuthorized ? (
                                    <button
                                        onClick={() => setIsChatOpen(true)}
                                        className="w-full bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 rounded-lg transition-colors"
                                    >
                                      ჩათის გახსნა
                                    </button>
                                ) : (
                                    <button
                                        disabled={loadingAction != null || hasRequestedJoin}
                                        onClick={handleRequestJoin}
                                        className={`w-full font-semibold py-3 rounded-lg transition-colors flex items-center justify-center ${
                                            hasRequestedJoin
                                                ? 'bg-gray-400 text-white cursor-not-allowed'
                                                : 'bg-amber-500 hover:bg-amber-600 text-white'
                                        }`}
                                    >
                                      {loadingAction === 'requestJoin' && <ButtonSpinner />}
                                      {hasRequestedJoin ? 'მოთხოვნა გაგზავნილია' : 'შესვლის მოთხოვნა'}
                                    </button>
                                )}
                              </>
                          ) : (
                              <>
                                <div className="mb-4">
                                  <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">მიმდინარე ლექცია</p>
                                  <p className="text-lg font-semibold text-gray-900">{modalData.lectureName}</p>
                                </div>
                                <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">ლექტორი</p>
                                    <p className="font-medium text-gray-900">{modalData.lecturer}</p>
                                  </div>
                                  <div>
                                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დრო</p>
                                    <p className="font-medium text-gray-900">{modalData.startTime} - {modalData.endTime}</p>
                                  </div>
                                  {modalData.groupNumber && (
                                      <div>
                                        <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">ჯგუფი</p>
                                        <p className="font-medium text-gray-900">{modalData.groupNumber}</p>
                                      </div>
                                  )}
                                </div>
                              </>
                          )}
                        </div>
                    ) : (
                        <div className="bg-green-50 rounded-xl p-5 border border-green-100 text-center">
                          <p className="text-green-800 font-medium mb-2 text-lg">ოთახი ამჟამად თავისუფალია.</p>
                          <p className="text-green-600 text-sm">შეგიძლიათ დაჯავშნოთ ახლავე.</p>
                        </div>
                    )}

                    <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-sm px-1 border-t border-gray-100 pt-4">
                  <span className="text-xs text-gray-500 uppercase tracking-wider font-semibold shrink-0">
                    შემდეგი ლექცია:
                  </span>
                      {modalData.nextLectureTitle ? (
                          <span className="text-gray-800 font-medium">
                      {modalData.nextLectureTitle}
                            <span className="text-gray-500 font-normal ml-2">
                        {modalData.nextLectureStart} – {modalData.nextLectureEnd}
                      </span>
                    </span>
                      ) : (
                          <span className="text-gray-400 italic">ოთახში დღეს მეტი ლექცია აღარ არის</span>
                      )}
                    </div>

                    {modalData.isFree && (
                        <div className="space-y-4">
                          <div>
                            <p className="text-sm text-gray-600 mb-1">სტატუსი (არასავალდებულო):</p>
                            <input
                                type="text"
                                maxLength={40}
                                value={noteText}
                                onChange={(e) => {
                                  hasEditedNoteRef.current = true
                                  setNoteText(e.target.value)
                                }}
                                placeholder="მაგ: ვსწავლობთ კალკულუსს"
                                className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500"
                            />
                          </div>
                          <div>
                            <p className="text-sm text-gray-600 mb-1">დაჯავშნის ხანგრძლივობა:</p>
                            {availableDurations.length > 0 ? (
                                <div className="flex gap-2">
                                  {availableDurations.map((duration) => (
                                      <button
                                          key={duration}
                                          disabled={loadingAction != null}
                                          onClick={() => { handleReserve(duration); }}
                                          className="flex-1 bg-yellow-400 hover:bg-yellow-500 text-yellow-900 font-semibold py-3 rounded-lg disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                                      >
                                        {loadingAction === `reserve-${duration}` && <ButtonSpinner />}
                                        {duration === 30 && '30 წუთი'}
                                        {duration === 60 && '1 საათი'}
                                        {duration === 120 && '2 საათი'}
                                        {![30, 60, 120].includes(duration) && `${duration} წუთი (დარჩენილი დრო)`}
                                      </button>
                                  ))}
                                </div>
                            ) : (
                                <p className="text-sm text-amber-600 italic bg-amber-50 border border-amber-100 rounded-lg px-3 py-2">
                                  ⏰ შემდეგი ლექცია მალე იწყება.
                                </p>
                            )}
                          </div>
                        </div>
                    )}
                  </div>
                </>
            ) : (
                <div className="flex flex-col h-[450px]">
                  <div className="flex items-center justify-between border-b pb-3 mb-3 relative">
                    <button
                        onClick={() => setIsChatOpen(false)}
                        className="text-sm font-semibold text-blue-600 hover:text-blue-800 w-16 text-left"
                    >
                      ← უკან
                    </button>
                    <h3 className="text-lg font-bold text-gray-900">ოთახის ჩათი ({roomId})</h3>
                    <div ref={usersMenuRef} className="relative w-16 flex justify-end mr-10">
                      {isAuthorized && (
                          <button
                              onClick={() => setShowUsersMenu(!showUsersMenu)}
                              className="p-1 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors"
                          >
                            <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                              <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                            </svg>
                          </button>
                      )}
                      {showUsersMenu && isAuthorized && (
                          <div className="absolute right-0 top-full mt-2 w-64 bg-white rounded-xl shadow-xl border border-gray-100 z-50 overflow-hidden text-left">
                            <div className="px-4 py-2 bg-gray-50 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
                              მომხმარებლები
                            </div>
                            <div className="max-h-60 overflow-y-auto p-2 space-y-1">
                              {chatUsers.length === 0 ? (
                                  <p className="text-center text-xs text-gray-400 py-4">არავინ არის ჩათში</p>
                              ) : (
                                  chatUsers.map((user) => (
                                      <div key={user.id} className="flex items-center justify-between p-2 hover:bg-gray-50 rounded-lg transition-colors">
                                        <Link to={`/profile/${user.id}`} className="flex items-center gap-2 flex-1 min-w-0 hover:opacity-80">
                                          <img
                                              src={user.photoUrl || "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y"}
                                              alt={user.nickname}
                                              referrerPolicy="no-referrer"
                                              className="h-6 w-6 rounded-full object-cover ring-1 ring-black/10 shrink-0"
                                          />
                                          <span className="text-sm font-medium text-gray-800 truncate">{user.nickname}</span>
                                        </Link>
                                        {modalData.isMyOccupancy && user.id !== modalData.reserverId && (
                                            <button
                                                disabled={loadingAction != null}
                                                onClick={() => handleKickUser(user.id)}
                                                className="ml-2 shrink-0 text-[10px] bg-red-50 hover:bg-red-100 text-red-600 font-bold px-2 py-1 rounded border border-red-200 transition-colors disabled:opacity-75 disabled:cursor-wait flex items-center gap-1"
                                            >
                                              {loadingAction === `kick-${user.id}` && <ButtonSpinner />}
                                              გაძევება
                                            </button>
                                        )}
                                      </div>
                                  ))
                              )}
                            </div>
                          </div>
                      )}
                    </div>
                  </div>

                  {isAuthorized === null ? (
                      <div className="flex-1 flex items-center justify-center text-gray-400 text-sm italic">
                        მოწმდება წვდომა...
                      </div>
                  ) : isAuthorized ? (
                      <>
                        <div
                            ref={chatContainerRef}
                            onScroll={handleScroll}
                            className="flex-1 overflow-y-auto space-y-3 pr-1 mb-3 scroll-smooth"
                        >
                          {isLoadingOlder && (
                              <div className="flex justify-center items-center py-2 text-xs text-gray-400 animate-pulse bg-gray-50 rounded-lg border border-dashed">
                                ⏳ ძველი შეტყობინებები იტვირთება...
                              </div>
                          )}

                          {messages.map((msg) => (
                              <div
                                  key={msg.id}
                                  className={`p-3 rounded-xl border flex gap-3 ${msg.messageType === 'REQUEST'
                                      ? 'bg-amber-50 border-amber-200'
                                      : msg.messageType === 'APPROVAL'
                                          ? 'bg-green-50 border-green-200'
                                          : 'bg-gray-50 border-gray-100'
                                  }`}
                              >
                                <div className="shrink-0">
                                  <Link to={`/profile/${msg.author}`} className="hover:opacity-80 transition-opacity cursor-pointer block">
                                    <img
                                        src={msg.photoUrl || "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y"}
                                        alt={msg.nickname}
                                        referrerPolicy="no-referrer"
                                        className="h-8 w-8 rounded-full object-cover ring-1 ring-black/10"
                                    />
                                  </Link>
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="flex justify-between items-start mb-1">
                                    <div className="flex flex-col">
                                      <span className="text-xs font-bold text-gray-900">{msg.nickname}</span>
                                      <span className="text-[10px] text-gray-500">{msg.email}</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                              <span className="text-[10px] text-gray-400">
                                {new Date(msg.sendingTime).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                              </span>
                                    </div>
                                  </div>
                                  <p className="text-sm text-gray-800 mt-1">{msg.message}</p>
                                  {msg.messageType === 'REQUEST' && (
                                      <div className="mt-2 flex gap-2">
                                        <button
                                            disabled={loadingAction != null}
                                            onClick={() => handleApproveUser(msg.author)}
                                            className="flex-1 bg-green-600 hover:bg-green-700 text-white text-xs font-bold py-2 px-3 rounded-lg transition-colors disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                                        >
                                          {loadingAction === `approve-${msg.author}` && <ButtonSpinner />}
                                          დამტკიცება
                                        </button>
                                        <button
                                            disabled={loadingAction != null}
                                            onClick={() => handleRejectUser(msg.author)}
                                            className="flex-1 bg-red-500 hover:bg-red-600 text-white text-xs font-bold py-2 px-3 rounded-lg transition-colors disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                                        >
                                          {loadingAction === `reject-${msg.author}` && <ButtonSpinner />}
                                          უარყოფა
                                        </button>
                                      </div>
                                  )}
                                </div>
                              </div>
                          ))}
                          {messages.length === 0 && (
                              <p className="text-center text-sm text-gray-400 italic pt-10">ჩათი ცარიელია</p>
                          )}
                        </div>

                        <div className="flex gap-2 border-t pt-3">
                          <input
                              type="text"
                              value={messageText}
                              onChange={(e) => setMessageText(e.target.value)}
                              placeholder="ჩაწერეთ შეტყობინება..."
                              className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500"
                              onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
                          />
                          <button
                              disabled={loadingAction != null}
                              onClick={handleSendMessage}
                              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-colors disabled:opacity-75 disabled:cursor-wait flex items-center justify-center"
                          >
                            {loadingAction === 'sendMessage' && <ButtonSpinner />}
                            გაგზავნა
                          </button>
                        </div>
                      </>
                  ) : (
                      <div className="flex-1 flex flex-col items-center justify-center text-center p-4">
                        <p className="text-gray-600 font-medium mb-6">თქვენ არ გაქვთ წვდომა ამ ოთახის ჩეთზე.</p>
                        <button
                            disabled={loadingAction != null || hasRequestedJoin}
                            onClick={handleRequestJoin}
                            className={`font-semibold py-3 px-6 rounded-xl shadow-md transition-colors flex items-center justify-center ${
                                hasRequestedJoin
                                    ? 'bg-gray-400 text-white cursor-not-allowed'
                                    : 'bg-amber-500 hover:bg-amber-600 text-white'
                            }`}
                        >
                          {loadingAction === 'requestJoin' && <ButtonSpinner />}
                          {hasRequestedJoin ? 'მოთხოვნა გაგზავნილია' : 'შესვლის მოთხოვნა'}
                        </button>
                      </div>
                  )}
                </div>
            )}
          </div>
        </div>
      </div>
  );
}