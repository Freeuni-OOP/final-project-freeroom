import useNotificationContainer from './useNotificationContainer';
import { NotificationItem } from '@/components/NotificationItem';

const NotificationContainer = (props) => {
  const { notifications, removeNotification } = useNotificationContainer(props);

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 pointer-events-none">
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          removeNotification={removeNotification}
        />
      ))}
    </div>
  );
};

export default NotificationContainer;
