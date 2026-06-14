import { FloorView } from '@/components';
import useFloorsPage from './useFloorsPage';

export default function FloorsPage() {
  useFloorsPage();

  return (
    <div className="w-full max-w-6xl mx-auto px-4 py-6 md:py-10 mb-8">
      <div className="h-[60vh] min-h-[450px] md:h-[75vh] md:max-h-[800px]">
        <FloorView />
      </div>
    </div>
  );
}
