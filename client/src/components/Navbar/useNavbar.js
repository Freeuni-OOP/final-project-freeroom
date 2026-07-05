import { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { logout } from '@/services/firebase';
import { useAuth } from '@/context';

const BASE_NAV_LINKS = [
    { label: 'კალენდარი', path: '/calendar' },
    { label: 'საგნები', path: '/subjects' },
    { label: 'სართულები', path: '/floors' },
    { label: 'პროფილი', path: '/profile' },
];

const ADMIN_NAV_LINK = { label: 'ადმინი', path: '/admin/reports' };

const useNavbar = () => {
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const { isAdmin } = useAuth();
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [showFeatures, setShowFeatures] = useState(false);
    const searchContainerRef = useRef(null);
    const mobileSearchContainerRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (
                (searchContainerRef.current && !searchContainerRef.current.contains(event.target)) &&
                (!mobileSearchContainerRef.current || !mobileSearchContainerRef.current.contains(event.target))
            ) {
                setShowFeatures(false);
            }
        };

        if (showFeatures) {
            document.addEventListener('mousedown', handleClickOutside);
        }
        
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showFeatures]);

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

    const toggleFeatures = (e) => {
        if (e) e.stopPropagation();
        setShowFeatures((prev) => !prev);
    };
    const closeFeatures = () => setShowFeatures(false);

    const navLinks = isAdmin ? [ADMIN_NAV_LINK, ...BASE_NAV_LINKS] : BASE_NAV_LINKS;

    return {
        navLinks,
        currentPath: pathname,
        goTo,
        handleLogout,
        isMenuOpen,
        toggleMenu,
        closeMenu,
        showFeatures,
        toggleFeatures,
        closeFeatures,
        searchContainerRef,
        mobileSearchContainerRef
    };
};

export default useNavbar;