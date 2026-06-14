import { FloorView } from '@/components';
import useFloorsPage from './useFloorsPage';

export default function FloorsPage() {
  useFloorsPage();

  return (
    <div className="w-full max-w-6xl mx-auto px-4 py-6 md:py-10">
      <div className="h-[500px] md:h-[700px]">
        <FloorView />
      </div>
    </div>
  );
}
