import { Navigate } from 'react-router-dom';
import useProtectedRoute from './useProtectedRoute';

export default function ProtectedRoute({ children }) {
    const { status } = useProtectedRoute();

    if (status === 'loading') {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-100">
                <div className="h-10 w-10 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin" />
            </div>
        );
    }

    if (status === 'redirect') {
        return <Navigate to="/" replace />;
    }

    return children;
}