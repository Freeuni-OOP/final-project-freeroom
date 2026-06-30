import { useState, useCallback, useMemo } from 'react';

const useNotificationProvider = () => {
  const [notifications, setNotifications] = useState([]);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const showNotification = useCallback((options) => {
    const id = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
    
    const newNotification = {
      id,
      message: options.message,
      type: options.type || 'info', // 'info', 'success', 'error', 'action'
      onAccept: options.onAccept,
      onReject: options.onReject,
      duration: options.type === 'action' ? 10000 : (options.duration || 4000),
    };

    setNotifications((prev) => [...prev, newNotification]);

    if (newNotification.duration > 0) {
      setTimeout(() => {
        removeNotification(id);
      }, newNotification.duration);
    }
  }, [removeNotification]);

  const contextValue = useMemo(() => ({
    showNotification,
    removeNotification
  }), [showNotification, removeNotification]);

  return {
    contextValue,
    notifications,
    removeNotification
  };
};

export default useNotificationProvider;
