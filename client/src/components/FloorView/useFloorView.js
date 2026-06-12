import { useState, useEffect, useRef } from 'react';
import FLOORS from './floorLayout';

const OCCUPIED_ROOMS = new Set([101, 104, 107, 110, 113, 116, 119, 201, 204, 207, 210, 213, 216, 219, 301, 304, 307, 310, 313, 316, 319, 401, 404, 407, 410]);

const MOCK_LECTURE = 'Object Oriented Programming';

const useFloorView = () => {
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [tooltip, setTooltip] = useState({ visible: false, x: 0, y: 0, text: '' });
  const svgContainerRef = useRef(null);
  const tooltipTimerRef = useRef(null);

  const [initialScale] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.innerWidth < 768 ? 1.2 : 1.5;
    }
    return 1.5;
  });

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
          const occupied = OCCUPIED_ROOMS.has(roomId);
          const rect = group.querySelector('rect');

          if (occupied) {
            if (rect) rect.style.fill = '#ef4444';
          }

          group.style.cursor = 'pointer';

          group.addEventListener('click', () => setSelectedRoomId(roomId));

          const cursorPos = { x: 0, y: 0 };

          group.addEventListener('mouseenter', (e) => {
            cursorPos.x = e.clientX;
            cursorPos.y = e.clientY;
            const text = occupied ? MOCK_LECTURE : 'თავისუფალი';
            tooltipTimerRef.current = setTimeout(() => {
              setTooltip({ visible: true, x: cursorPos.x, y: cursorPos.y, text });
            }, 500);
          });

          group.addEventListener('mousemove', (e) => {
            cursorPos.x = e.clientX;
            cursorPos.y = e.clientY;
            setTooltip((prev) => prev.visible ? { ...prev, x: e.clientX, y: e.clientY } : prev);
          });

          group.addEventListener('mouseleave', () => {
            clearTimeout(tooltipTimerRef.current);
            setTooltip({ visible: false, x: 0, y: 0, text: '' });
          });
        });
      });

    return () => clearTimeout(tooltipTimerRef.current);
  }, [selectedFloor]);

  const selectFloor = (floor) => {
    setSelectedFloor(floor);
    setSelectedRoomId(null);
  };

  const handleCloseModal = () => {
    setSelectedRoomId(null);
  };

  const isOccupied = (roomId) => OCCUPIED_ROOMS.has(roomId);

  return {
    selectedFloor,
    selectedRoomId,
    tooltip,
    svgContainerRef,
    selectFloor,
    handleCloseModal,
    isOccupied,
    initialScale,
  };
};

export default useFloorView;
