import { TransformWrapper, TransformComponent } from 'react-zoom-pan-pinch';
import useFloorView from './useFloorView';
import { RoomModal } from '@/components';
import './FloorView.css';

const FLOORS = [1, 2, 3, 4];

export default function FloorView() {
  const {
    selectedFloor,
    selectedRoomId,
    tooltip,
    svgContainerRef,
    selectFloor,
    handleCloseModal,
    isOccupied,
    initialScale,
  } = useFloorView();

  return (
    <div className="flex flex-col w-full h-full min-h-[480px] bg-white rounded-2xl border border-gray-200 shadow-lg overflow-hidden">
      <div className="flex items-center gap-2 px-4 py-3 border-b border-gray-100 shrink-0">
        <p className="text-sm font-semibold text-gray-500 mr-1">სართული:</p>
        {FLOORS.map((floor) => (
          <button
            key={floor}
            onClick={() => selectFloor(floor)}
            className={`w-9 h-9 rounded-full border text-sm font-semibold cursor-pointer flex items-center justify-center transition-all duration-150
              ${selectedFloor === floor
                ? 'bg-brand-gold border-brand-gold text-brand-ink'
                : 'bg-gray-50 border-gray-200 text-brand-ink hover:border-brand-gold hover:bg-brand-gold/10'
              }`}
          >
            {floor}
          </button>
        ))}
      </div>

      <div className="flex-1 overflow-hidden relative bg-gray-50">
        <TransformWrapper
          key={initialScale}
          initialScale={initialScale}
          minScale={0.3}
          maxScale={4}
          centerOnInit
          limitToBounds={false}
        >
          <TransformComponent wrapperStyle={{ width: '100%', height: '100%' }}>
            <div className="floor-view__svg-container" ref={svgContainerRef} />
          </TransformComponent>
        </TransformWrapper>
      </div>

      {selectedRoomId && (
        <RoomModal
          roomId={selectedRoomId}
          isOccupied={isOccupied(selectedRoomId)}
          onClose={handleCloseModal}
        />
      )}

      {tooltip.visible && (
        <div
          className="floor-view__tooltip"
          style={{ left: tooltip.x + 14, top: tooltip.y - 36 }}
        >
          {tooltip.text}
        </div>
      )}
    </div>
  );
}
