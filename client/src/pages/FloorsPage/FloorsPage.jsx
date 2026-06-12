import { FloorView } from '@/components';
import useFloorsPage from './useFloorsPage';

export default function FloorsPage() {
  useFloorsPage();

  return (
    <div className="flex flex-col h-screen">
      <FloorView />
    </div>
  );
}
