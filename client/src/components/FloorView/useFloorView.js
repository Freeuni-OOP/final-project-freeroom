import { useEffect, useRef, useState, useCallback } from 'react';
import FLOORS from './floorLayout';
import { getRoomsMap } from '@/services/index.js';

const MOUSE_WHEEL_THRESHOLD = 50;
const MOUSE_ZOOM_FACTOR = 0.18;
const MOUSE_ZOOM_DURATION = 120;
const TRACKPAD_ZOOM_MULTIPLIER = 0.004;
const SCROLL_THROTTLE_MS = 40;

const useFloorView = () => {
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [tooltip, setTooltip] = useState({ visible: false, x: 0, y: 0, text: '' });
  const [roomsData, setRoomsData] = useState({});
  const [loading, setLoading] = useState(true);
  const svgContainerRef = useRef(null);
  const tooltipTimerRef = useRef(null);
  const roomsDataRef = useRef({});
  const transformRef = useRef(null);
  const wrapperRef = useRef(null);
  const lastScrollTime = useRef(0);

  const [initialScale] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.innerWidth < 768 ? 0.9 : 1;
    }
    return 1.2;
  });

  useEffect(() => {
    roomsDataRef.current = roomsData;
  }, [roomsData]);

  useEffect(() => {
    const el = wrapperRef.current;
    if (!el) return;

    const handleWheel = (e) => {
      e.preventDefault();
      if (!transformRef.current) return;

      const isMouseWheel = Math.abs(e.deltaY) >= MOUSE_WHEEL_THRESHOLD;

      if (isMouseWheel) {
        const now = Date.now();
        if (now - lastScrollTime.current < SCROLL_THROTTLE_MS) return;
        lastScrollTime.current = now;
        const zoom = e.deltaY < 0 ? transformRef.current.zoomIn : transformRef.current.zoomOut;
        zoom(MOUSE_ZOOM_FACTOR, MOUSE_ZOOM_DURATION);
      } else {
        const factor = Math.abs(e.deltaY) * TRACKPAD_ZOOM_MULTIPLIER;
        const zoom = e.deltaY < 0 ? transformRef.current.zoomIn : transformRef.current.zoomOut;
        zoom(factor, 0);
      }
    };

    el.addEventListener('wheel', handleWheel, { passive: false });
    return () => el.removeEventListener('wheel', handleWheel);
  }, []);

  const loadRoomsMap = useCallback(async () => {
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
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
      loadRoomsMap();
  }, [loadRoomsMap]);

  const FRIEND_BADGE_CLASS = 'friend-occupancy-badge';

  const applyRoomColors = (floor) => {
    const container = svgContainerRef.current;
    if (!container) return;
    const svgNS = 'http://www.w3.org/2000/svg';

    container.querySelectorAll('g[id^="room-"]').forEach((group) => {
      const roomId = parseInt(group.id.replace('room-', ''), 10);
      const rect = group.querySelector('rect');
      if (!rect) return;

      const roomData = roomsDataRef.current[floor]?.[roomId];
      const occupied = roomData?.status === 'occupied';
      rect.style.fill = occupied ? '#ef4444' : '';

      group.querySelectorAll(`.${FRIEND_BADGE_CLASS}`).forEach((el) => el.remove());

      const occupancy = roomData?.currentOccupancy;
      if (!occupancy?.isFriendOccupancy) return;

      const bbox = rect.getBBox();
      const cx = bbox.x + bbox.width - 12;
      const cy = bbox.y + 12;
      const radius = 11;

      const badgeGroup = document.createElementNS(svgNS, 'g');
      badgeGroup.classList.add(FRIEND_BADGE_CLASS);
      badgeGroup.style.pointerEvents = 'none';

      if (occupancy.reserverPhotoUrl) {
        const clipId = `friend-clip-${floor}-${roomId}`;
        const defs = document.createElementNS(svgNS, 'defs');
        const clipPath = document.createElementNS(svgNS, 'clipPath');
        clipPath.setAttribute('id', clipId);
        const clipCircle = document.createElementNS(svgNS, 'circle');
        clipCircle.setAttribute('cx', cx);
        clipCircle.setAttribute('cy', cy);
        clipCircle.setAttribute('r', radius);
        clipPath.appendChild(clipCircle);
        defs.appendChild(clipPath);
        badgeGroup.appendChild(defs);

        const image = document.createElementNS(svgNS, 'image');
        image.setAttributeNS('http://www.w3.org/1999/xlink', 'href', occupancy.reserverPhotoUrl);
        image.setAttribute('href', occupancy.reserverPhotoUrl);
        image.setAttribute('x', cx - radius);
        image.setAttribute('y', cy - radius);
        image.setAttribute('width', radius * 2);
        image.setAttribute('height', radius * 2);
        image.setAttribute('clip-path', `url(#${clipId})`);
        image.setAttribute('preserveAspectRatio', 'xMidYMid slice');
        badgeGroup.appendChild(image);
      } else {
        const circle = document.createElementNS(svgNS, 'circle');
        circle.setAttribute('cx', cx);
        circle.setAttribute('cy', cy);
        circle.setAttribute('r', radius);
        circle.setAttribute('fill', '#facc15');
        badgeGroup.appendChild(circle);

        const text = document.createElementNS(svgNS, 'text');
        text.setAttribute('x', cx);
        text.setAttribute('y', cy + 4);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('font-size', '9');
        text.setAttribute('font-weight', 'bold');
        text.setAttribute('fill', '#1f1f1f');
        text.textContent = occupancy.reserverDisplayName?.[0]?.toUpperCase() ?? '?';
        badgeGroup.appendChild(text);
      }

      const ring = document.createElementNS(svgNS, 'circle');
      ring.setAttribute('cx', cx);
      ring.setAttribute('cy', cy);
      ring.setAttribute('r', radius);
      ring.setAttribute('fill', 'none');
      ring.setAttribute('stroke', 'white');
      ring.setAttribute('stroke-width', '1.5');
      badgeGroup.appendChild(ring);

      group.appendChild(badgeGroup);
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
            let text = 'თავისუფალი';
            if (roomData?.currentLecture?.title) {
                text = roomData.currentLecture.title;
            } else if (roomData?.currentOccupancy != null) {
                text = roomData.currentOccupancy.publicNote 
                    ? `დაკავებული - ${roomData.currentOccupancy.publicNote}`
                    : 'დაკავებული';
            }
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

  const handleCloseModal = () => setSelectedRoomId(null);

  const isOccupied = (roomId) =>
      roomsData[selectedFloor]?.[roomId]?.status === 'occupied';

  const getRoomData = (roomId) =>
    roomsData[selectedFloor]?.[roomId] ?? null;

  return {
    selectedFloor,
    selectedRoomId,
    tooltip,
    svgContainerRef,
    wrapperRef,
    transformRef,
    selectFloor,
    handleCloseModal,
    isOccupied,
    getRoomData,
    loading,
    initialScale,
    loadRoomsMap,
  };
};

export default useFloorView;

