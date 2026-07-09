import { useState, useCallback, useMemo } from 'react';
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '@/services/api/endpoints';

const useNotificationProvider = () => {
  const [notifications, setNotifications] = useState([]);
  const [persistentNotifications, setPersistentNotifications] = useState([]);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const showNotification = useCallback((options) => {
    const id = Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
    
    const newNotification = {
      id,
      message: options.message,
      type: options.type || 'info',
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

  const [notificationPage, setNotificationPage] = useState(0);
  const [hasMoreNotifications, setHasMoreNotifications] = useState(true);

  const fetchNotifications = useCallback(async (page = 0) => {
    try {
      const data = await getNotifications(page);
      if (page === 0) {
        setPersistentNotifications(data.content);
      } else {
        setPersistentNotifications((prev) => [...prev, ...data.content]);
      }
      setNotificationPage(page);
      setHasMoreNotifications(!data.last);
    } catch {
      // ignore — user may not be logged in yet
    }
  }, []);

  const addPersistentNotification = useCallback((notif) => {
    setPersistentNotifications((prev) => [notif, ...prev]);
  }, []);

  const markRead = useCallback(async (id) => {
    await markNotificationRead(id);
    setPersistentNotifications((prev) =>
      prev.map((n) => n.id === id ? { ...n, isRead: true } : n)
    );
  }, []);

  const markAllRead = useCallback(async () => {
    await markAllNotificationsRead();
    setPersistentNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  }, []);

  const unreadCount = persistentNotifications.filter((n) => !n.isRead).length;

  const contextValue = useMemo(() => ({
    showNotification,
    removeNotification,
    persistentNotifications,
    unreadCount,
    fetchNotifications,
    addPersistentNotification,
    markRead,
    hasMoreNotifications,
    notificationPage,
  }), [showNotification, removeNotification, persistentNotifications, unreadCount, fetchNotifications, addPersistentNotification, markRead, markAllRead, hasMoreNotifications, notificationPage]);

  return {
    contextValue,
    notifications,
    removeNotification
  };
};

export default useNotificationProvider;
