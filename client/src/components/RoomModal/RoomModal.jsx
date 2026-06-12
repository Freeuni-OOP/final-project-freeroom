import useRoomModal from './useRoomModal';

export default function RoomModal({ roomId, isOccupied, onClose }) {
  const { roomData, isLoading, handleReserve } = useRoomModal(roomId, isOccupied);

  if (!roomId) return null;

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
            <h2 className="text-2xl font-bold text-gray-900">Room {roomId}</h2>
            {!isLoading && roomData && (
              <span className={`px-3 py-1 rounded-full text-xs font-bold tracking-wide uppercase ${
                roomData.isFree
                  ? 'bg-green-100 text-green-700'
                  : 'bg-red-100 text-red-700'
              }`}>
                {roomData.isFree ? 'Free' : 'Occupied'}
              </span>
            )}
          </div>

          {isLoading || !roomData ? (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-yellow-500"></div>
            </div>
          ) : (
            <div className="space-y-6">
              {!roomData.isFree ? (
                <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                  <div className="mb-4">
                    <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">Current Lecture</p>
                    <p className="text-lg font-semibold text-gray-900">{roomData.lectureName}</p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">Lecturer</p>
                      <p className="font-medium text-gray-900">{roomData.lecturer}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 uppercase tracking-wider font-semibold mb-1">Time</p>
                      <p className="font-medium text-gray-900">{roomData.startTime} - {roomData.endTime}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="bg-green-50 rounded-xl p-5 border border-green-100 text-center py-8">
                  <p className="text-green-800 font-medium mb-2 text-lg">This room is currently available.</p>
                  <p className="text-green-600 text-sm">You can book it right now for your study session.</p>
                </div>
              )}

              {roomData.isFree && (
                <button
                  onClick={() => { handleReserve(); onClose(); }}
                  className="w-full bg-yellow-400 hover:bg-yellow-500 text-yellow-900 font-semibold py-3 px-6 rounded-xl transition-colors shadow-sm cursor-pointer"
                >
                  Reserve Now
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
