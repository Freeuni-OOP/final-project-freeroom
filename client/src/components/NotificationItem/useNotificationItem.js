import { useState, useEffect } from 'react';

const useNotificationItem = ({ notification, removeNotification }) => {
  const [isLeaving, setIsLeaving] = useState(false);
  const [isEntering, setIsEntering] = useState(true);

  useEffect(() => {
    // Trigger entry animation next frame
    requestAnimationFrame(() => {
      setIsEntering(false);
    });
  }, []);

  const handleDismiss = () => {
    setIsLeaving(true);
    setTimeout(() => {
      removeNotification(notification.id);
    }, 300); // Wait for transition
  };

  const handleAccept = () => {
    if (notification.onAccept) notification.onAccept();
    handleDismiss();
  };

  const handleReject = () => {
    if (notification.onReject) notification.onReject();
    handleDismiss();
  };

  // Base styles
  const baseClasses = `flex flex-col w-80 bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden pointer-events-auto transition-all duration-300 transform ${
    isEntering || isLeaving ? 'translate-x-full opacity-0' : 'translate-x-0 opacity-100'
  }`;

  return {
    isLeaving,
    handleDismiss,
    handleAccept,
    handleReject,
    baseClasses,
    type: notification.type,
    message: notification.message
  };
};

export default useNotificationItem;
