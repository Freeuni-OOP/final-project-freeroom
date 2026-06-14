import {useEffect, useRef, useState} from 'react';
import FLOORS from './floorLayout';
import {getRoomsMap} from '@/services/index.js';

const useFloorView = () => {
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [tooltip, setTooltip] = useState({ visible: false, x: 0, y: 0, text: '' });
  const [roomsData, setRoomsData] = useState({});
  const [loading, setLoading] = useState(true);
  const svgContainerRef = useRef(null);
  const tooltipTimerRef = useRef(null);
  const roomsDataRef = useRef({});

  const [initialScale] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.innerWidth < 768 ? 1.2 : 1.5;
    }
    return 1.5;
  });

  useEffect(() => {
    roomsDataRef.current = roomsData;
  }, [roomsData]);

  useEffect(() => {
    const loadRoomsMap = async () => {
      try {
        const res = await getRoomsMap();
        const grouped = {};
        res.data.forEach((room) => {
          if (!grouped[room.floorNumber]) grouped[room.floorNumber] = {};
          grouped[room.floorNumber][room.roomNumber] = room;
        });
        setRoomsData(grouped);
      } catch (err) {
        console.error('Failed to load rooms map', err);
      } finally {
        setLoading(false);
      }
    };

    loadRoomsMap();
  }, []);

  const applyRoomColors = (floor) => {
    const container = svgContainerRef.current;
    if (!container) return;

    container.querySelectorAll('g[id^="room-"]').forEach((group) => {
      const roomId = parseInt(group.id.replace('room-', ''), 10);
      const rect = group.querySelector('rect');
      if (!rect) return;

      const roomData = roomsDataRef.current[floor]?.[roomId];
      const occupied = roomData?.status === 'occupied';
      rect.style.fill = occupied ? '#ef4444' : '';
    });
  };

  useEffect(() => {
    const container = svgContainerRef.current;
    if (!container) return;

    const loadFloorSvg = async () => {
      try {
        const r = await fetch(FLOORS[selectedFloor]);
        container.innerHTML = await r.text();

        const svgEl = container.querySelector('svg');
        if (!svgEl) return;

        svgEl.setAttribute('width', '100%');
        svgEl.setAttribute('height', '100%');

        const bgRect = svgEl.querySelector('rect:first-child');
        if (bgRect) bgRect.remove();

        svgEl.querySelectorAll('g[id^="room-"]').forEach((group) => {
          const roomId = parseInt(group.id.replace('room-', ''), 10);

          group.style.cursor = 'pointer';
          group.addEventListener('click', () => setSelectedRoomId(roomId));

          const cursorPos = { x: 0, y: 0 };

          group.addEventListener('mouseenter', (e) => {
            cursorPos.x = e.clientX;
            cursorPos.y = e.clientY;
            const roomData = roomsDataRef.current[selectedFloor]?.[roomId];
            const text = roomData?.currentLecture?.title ?? 'თავისუფალი';
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

        applyRoomColors(selectedFloor);
      } catch (err) {
        console.error('Failed to load floor SVG', err);
      }
    };

    loadFloorSvg();

    return () => clearTimeout(tooltipTimerRef.current);
  }, [selectedFloor]);

  useEffect(() => {
    applyRoomColors(selectedFloor);
  }, [roomsData, selectedFloor]);

  const selectFloor = (floor) => {
    setSelectedFloor(floor);
    setSelectedRoomId(null);
  };

  const handleCloseModal = () => {
    setSelectedRoomId(null);
  };

  const isOccupied = (roomId) =>
      roomsData[selectedFloor]?.[roomId]?.status === 'occupied';

  const getRoomData = (roomId) => {
    return roomsData[selectedFloor]?.[roomId] ?? null;
  }

  return {
    selectedFloor,
    selectedRoomId,
    tooltip,
    svgContainerRef,
    selectFloor,
    handleCloseModal,
    isOccupied,
    getRoomData,
    loading,
    initialScale,
  };
};

export default useFloorView;