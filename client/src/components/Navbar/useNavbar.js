import { useLocation, useNavigate } from 'react-router-dom';
import { logout } from '@/services/firebase';

const NAV_LINKS = [
    { label: 'Floors', path: '/floors' },
    { label: 'Profile', path: '/profile' },
];

const useNavbar = () => {
    const navigate = useNavigate();
    const { pathname } = useLocation();

    const goTo = (path) => navigate(path);

    const handleLogout = async () => {
        await logout().catch(() => {});
        navigate('/');
    };

    return { navLinks: NAV_LINKS, currentPath: pathname, goTo, handleLogout };
};

export default useNavbar;