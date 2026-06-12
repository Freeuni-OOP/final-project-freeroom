import { useAuth } from '@/context';

const useProtectedRoute = () => {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return { status: 'loading' };
    }

    return { status: isAuthenticated ? 'allowed' : 'redirect' };
};

export default useProtectedRoute;