import useRoomModal from './useRoomModal';

export default function RoomModal({ roomId, roomData, onClose, onReserveSuccess }) {
  const { roomData: modalData, handleReserve, handleCancel } = useRoomModal(roomId, roomData, onClose, onReserveSuccess);

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
          className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 transition-colors cursor-pointer"
        >
          ✕
        </button>

        <div className="p-6">
          <div className="flex items-center gap-3 mb-6">
            <h2 className="text-2xl font-bold text-gray-900">ოთახი {roomId}</h2>
            <span className={`px-3 py-1 rounded-full text-xs font-bold tracking-wide uppercase ${
              modalData.isFree ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}>
              {modalData.isFree ? 'თავისუფალი' : 'დაკავებული'}
            </span>
          </div>

          <div className="space-y-6">
            {!modalData.isFree ? (
                <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                  {modalData.isReserved && modalData.isMyOccupancy ? (
                      <>
                        <div className="bg-yellow-50 rounded-xl p-5 border border-yellow-200">
                          <p className="text-sm font-bold text-yellow-700 mb-3">🔑 თქვენი ოთახი</p>
                          <div className="grid grid-cols-2 gap-4 mb-4">
                            <div>
                              <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დაჯავშნა</p>
                              <p className="font-medium text-gray-900">{modalData.reservedBy ?? '—'}</p>
                            </div>
                            <div>
                              <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">თავისუფლდება</p>
                              <p className="font-medium text-gray-900">{modalData.reservedUntil ?? '—'}</p>
                            </div>
                          </div>
                          <button
                              onClick={handleCancel}
                              className="w-full bg-red-500 hover:bg-red-600 text-white font-semibold py-3 rounded-lg transition-colors"
                          >
                            გაუქმება
                          </button>
                        </div>
                      </>
                  ) : modalData.isReserved ? (
                      <>
                        <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-3">დაჯავშნილია</p>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დაჯავშნა</p>
                            <p className="font-medium text-gray-900">{modalData.reservedBy ?? '—'}</p>
                          </div>
                          <div>
                            <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">თავისუფლდება</p>
                            <p className="font-medium text-gray-900">{modalData.reservedUntil ?? '—'}</p>
                          </div>
                        </div>
                      </>
                  ) : (
                      <>
                        <div className="mb-4">
                          <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">მიმდინარე ლექცია</p>
                          <p className="text-lg font-semibold text-gray-900">{modalData.lectureName}</p>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">ლექტორი</p>
                            <p className="font-medium text-gray-900">{modalData.lecturer}</p>
                          </div>
                          <div>
                            <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">დრო</p>
                            <p className="font-medium text-gray-900">{modalData.startTime} - {modalData.endTime}</p>
                          </div>
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
                <div className="space-y-2">
                  <p className="text-sm text-gray-600 mb-1">დაჯავშნის ხანგრძლივობა:</p>
                  <div className="flex gap-2">
                    <button
                        onClick={() => { handleReserve(30); }}
                        className="flex-1 bg-yellow-400 hover:bg-yellow-500 text-yellow-900 font-semibold py-3 rounded-lg"
                    >
                      30 წუთი
                    </button>
                    <button
                        onClick={() => { handleReserve(60); }}
                        className="flex-1 bg-yellow-400 hover:bg-yellow-500 text-yellow-900 font-semibold py-3 rounded-lg"
                    >
                      1 საათი
                    </button>
                    <button
                        onClick={() => { handleReserve(120); }}
                        className="flex-1 bg-yellow-400 hover:bg-yellow-500 text-yellow-900 font-semibold py-3 rounded-lg"
                    >
                      2 საათი
                    </button>
                  </div>
                </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
