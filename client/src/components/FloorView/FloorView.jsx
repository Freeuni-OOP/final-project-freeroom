import { TransformWrapper, TransformComponent } from 'react-zoom-pan-pinch';
import useFloorView from './useFloorView';
import { RoomModal } from '@/components';
import './FloorView.css';

export default function FloorView() {
  const {
    selectedFloor,
    selectedRoomId,
    svgContainerRef,
    handleFloorChange,
    handleCloseModal,
    isOccupied,
  } = useFloorView();

  return (
    <div className="floor-view">
      <div className="floor-view__controls">
        <label className="floor-view__label" htmlFor="floor-select">
          Floor
        </label>
        <select
          id="floor-select"
          className="floor-view__select"
          value={selectedFloor}
          onChange={handleFloorChange}
        >
          <option value={1}>Floor 1</option>
          <option value={2}>Floor 2</option>
          <option value={3}>Floor 3</option>
          <option value={4}>Floor 4</option>
        </select>
      </div>

      <div className="floor-view__canvas-wrapper">
        <TransformWrapper
          initialScale={1.5}
          minScale={0.3}
          maxScale={4}
          centerOnInit
          limitToBounds={false}
        >
          <TransformComponent wrapperStyle={{ width: '100%', height: '100%' }}>
            <div className="floor-view__svg-container" ref={svgContainerRef} />
          </TransformComponent>
        </TransformWrapper>

        <div className="floor-view__zoom-hint">Scroll to zoom · Drag to pan</div>
      </div>

      {selectedRoomId && (
        <RoomModal
          roomId={selectedRoomId}
          isOccupied={isOccupied(selectedRoomId)}
          onClose={handleCloseModal}
        />
      )}
    </div>
  );
}
