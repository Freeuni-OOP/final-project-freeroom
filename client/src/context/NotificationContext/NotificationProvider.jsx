import useNotificationProvider from './useNotificationProvider';
import { NotificationContext } from './useNotificationContext';
import { NotificationContainer } from '@/components';

const NotificationProvider = ({ children }) => {
  const {
    contextValue,
    notifications,
    removeNotification
  } = useNotificationProvider();

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
      <NotificationContainer notifications={notifications} removeNotification={removeNotification} />
    </NotificationContext.Provider>
  );
};

export default NotificationProvider;
