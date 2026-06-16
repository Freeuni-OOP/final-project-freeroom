import {reserveRoom} from '@/services/api/endpoints.js'

const useRoomModal = (roomId, roomData, onClose, onReserveSuccess) => {
  const formatTime = (iso) => {
    if (!iso) return null;
    return new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
  };

  const modalData = roomId
    ? {
        id: roomId,
        isFree: roomData?.status !== 'occupied',
        isReserved: roomData?.currentOccupancy != null,
        // lecture fields :
        lectureName: roomData?.currentLecture?.title ?? null,
        lecturer: roomData?.currentLecture?.organizer ?? null,
        startTime: formatTime(roomData?.currentLecture?.startAt),
        endTime: formatTime(roomData?.currentLecture?.endAt),
        // occupancy fields :
        reservedBy: "Not Your Friend", // [future | for friends possibly show reserver] reservedBy: roomData?.currentOccupancy?.reserverUserName ?? null,
        reservedUntil: formatTime(roomData?.currentOccupancy?.expectedEndAt),

        capacity: roomData?.capacity ?? null,
      }
    : null;

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
    }catch (err) {
        alert(err.response?.data?.error || 'დაჯავშნა ვერ მოხერხდა');
    }
  };

  return {
    roomData: modalData,
    handleReserve,
  };
};

export default useRoomModal;
