import { Outlet } from 'react-router-dom';
import { Navbar } from '@/components';

export default function Layout() {
    return (
        <div className="flex min-h-screen flex-col bg-brand-bg">
            <Navbar />
            <main className="flex-1">
                <Outlet />
            </main>
        </div>
    );
}