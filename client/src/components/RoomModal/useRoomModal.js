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


    const handleReserve = () => {
        alert(`Reserved room ${roomId}`);
    };

    return {
        roomData: modalData,
        handleReserve,
    };
};

export default useRoomModal;