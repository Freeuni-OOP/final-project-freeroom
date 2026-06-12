import { useState, useEffect, useRef } from 'react';
import FLOORS from './floorLayout';

const OCCUPIED_ROOMS = new Set([101, 104, 107, 110, 113, 116, 119, 201, 204, 207, 210, 213, 216, 219, 301, 304, 307, 310, 313, 316, 319, 401, 404, 407, 410]);

const useFloorView = () => {
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const svgContainerRef = useRef(null);

  useEffect(() => {
    const container = svgContainerRef.current;
    if (!container) return;

    fetch(FLOORS[selectedFloor])
      .then((r) => r.text())
      .then((svgText) => {
        container.innerHTML = svgText;

        const svgEl = container.querySelector('svg');
        if (!svgEl) return;

        svgEl.setAttribute('width', '100%');
        svgEl.setAttribute('height', '100%');

        const bgRect = svgEl.querySelector('rect:first-child');
        if (bgRect) bgRect.remove();

        svgEl.querySelectorAll('g[id^="room-"]').forEach((group) => {
          const roomId = parseInt(group.id.replace('room-', ''), 10);
          const rect = group.querySelector('rect');

          if (OCCUPIED_ROOMS.has(roomId)) {
            if (rect) rect.style.fill = '#ef4444';
          }

          group.style.cursor = 'pointer';
          group.addEventListener('click', () => setSelectedRoomId(roomId));
        });
      });
  }, [selectedFloor]);

  const handleFloorChange = (e) => {
    setSelectedFloor(Number(e.target.value));
    setSelectedRoomId(null);
  };

  const handleRoomClick = (roomId) => {
    setSelectedRoomId(roomId);
  };

  const handleCloseModal = () => {
    setSelectedRoomId(null);
  };

  const isOccupied = (roomId) => OCCUPIED_ROOMS.has(roomId);

  return {
    selectedFloor,
    selectedRoomId,
    svgContainerRef,
    handleFloorChange,
    handleRoomClick,
    handleCloseModal,
    isOccupied,
  };
};

export default useFloorView;
