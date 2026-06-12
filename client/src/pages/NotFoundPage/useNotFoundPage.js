import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context';

const useNotFoundPage = () => {
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();

    const homePath = isAuthenticated ? '/floors' : '/';

    const goHome = () => navigate(homePath);

    return { goHome };
};

export default useNotFoundPage;