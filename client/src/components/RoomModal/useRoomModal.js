import {reserveRoom} from '@/services/api/endpoints.js'

const useRoomModal = (roomId, roomData) => {
  const formatTime = (iso) => {
    if (!iso) return null;
    return new Date(iso).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
  };

  const modalData = roomId
    ? {
        id: roomId,
        isFree: roomData?.status !== 'occupied',
        lectureName: roomData?.currentLecture?.title ?? null,
        lecturer: roomData?.currentLecture?.organizer ?? null,
        startTime: formatTime(roomData?.currentLecture?.startAt),
        endTime: formatTime(roomData?.currentLecture?.endAt),
        capacity: roomData?.capacity ?? null,
      }
    : null;

  const handleReserve = async () => {
    if(!modalData?.isFree) {
        alert('ოთახი დაკავებულია');
        return;
    }
    try {
        await reserveRoom(roomId);
        alert(`ოთახი ${roomId} დაჯავშნილია`);
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
