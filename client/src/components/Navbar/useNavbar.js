import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { logout } from '@/services/firebase';

const NAV_LINKS = [
    { label: 'სართულები', path: '/floors' },
    { label: 'პროფილი', path: '/profile' },
];

const useNavbar = () => {
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const goTo = (path) => {
        setIsMenuOpen(false);
        navigate(path);
    };

    const handleLogout = async () => {
        await logout().catch(() => {});
        setIsMenuOpen(false);
        navigate('/');
    };

    const toggleMenu = () => setIsMenuOpen((prev) => !prev);
    const closeMenu = () => setIsMenuOpen(false);

    return { 
        navLinks: NAV_LINKS, 
        currentPath: pathname, 
        goTo, 
        handleLogout, 
        isMenuOpen, 
        toggleMenu, 
        closeMenu 
    };
};

export default useNavbar;