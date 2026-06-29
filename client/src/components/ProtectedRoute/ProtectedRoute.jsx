import { Navigate, Outlet } from 'react-router-dom';
import useProtectedRoute from './useProtectedRoute';
import { Loader } from '@/components';

export default function ProtectedRoute() {
    const { status } = useProtectedRoute();

    if (status === 'loading') {
        return <Loader fullScreen={true} />;
    }

    if (status === 'redirect') {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
}