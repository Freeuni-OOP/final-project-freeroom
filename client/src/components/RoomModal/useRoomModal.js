import { useState, useEffect } from 'react';

const useRoomModal = (roomId, isOccupied) => {
  const [roomData, setRoomData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!roomId) return;

    setIsLoading(true);
    const timer = setTimeout(() => {
      setRoomData({
        id: roomId,
        isFree: !isOccupied,
        lectureName: isOccupied ? 'Object Oriented Programming' : null,
        lecturer: isOccupied ? 'Lekva' : null,
        startTime: isOccupied ? '10:00' : null,
        endTime: isOccupied ? '12:00' : null,
      });
      setIsLoading(false);
    }, 400);

    return () => clearTimeout(timer);
  }, [roomId, isOccupied]);

  const handleReserve = () => {
    alert(`Reserved room ${roomId}`);
  };

  return {
    roomData,
    isLoading,
    handleReserve,
  };
};

export default useRoomModal;
