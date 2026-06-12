import { useState, useEffect } from 'react';

const useRoomModal = (roomId, isOccupied) => {
  const [roomData, setRoomData] = useState(null);

  useEffect(() => {
    if (!roomId) return;

    setRoomData({
      id: roomId,
      isFree: !isOccupied,
      lectureName: isOccupied ? 'Object Oriented Programming' : null,
      lecturer: isOccupied ? 'Lekva' : null,
      startTime: isOccupied ? '10:00' : null,
      endTime: isOccupied ? '12:00' : null,
    });
  }, [roomId, isOccupied]);

  const handleReserve = () => {
    alert(`Reserved room ${roomId}`);
  };

  return {
    roomData,
    handleReserve,
  };
};

export default useRoomModal;
