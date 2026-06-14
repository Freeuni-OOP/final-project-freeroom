
const useRoomModal = (roomId, isOccupied) => {
  const roomData = roomId
    ? {
        id: roomId,
        isFree: !isOccupied,
        lectureName: isOccupied ? 'Object Oriented Programming' : null,
        lecturer: isOccupied ? 'Lekva' : null,
        startTime: isOccupied ? '10:00' : null,
        endTime: isOccupied ? '12:00' : null,
      }
    : null;

  const handleReserve = () => {
    alert(`ოთახი ${roomId} დაჯავშნილია`);
  };

  return {
    roomData,
    handleReserve,
  };
};

export default useRoomModal;
