import { useState, useRef, useEffect } from 'react';
import { useNotification } from '@/context';

const useNotificationBell = () => {
    const { unreadCount } = useNotification();
    const [isOpen, setIsOpen] = useState(false);
    const containerRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (containerRef.current && !containerRef.current.contains(e.target)) {
                setIsOpen(false);
            }
        };
        if (isOpen) document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isOpen]);

    return {
        unreadCount,
        isOpen,
        setIsOpen,
        containerRef
    };
};

export default useNotificationBell;
