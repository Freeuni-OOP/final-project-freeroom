import useNotificationItem from './useNotificationItem';

const NotificationItem = ({ notification, removeNotification }) => {
  const {
    handleDismiss,
    handleAccept,
    handleReject,
    baseClasses,
    type,
    message
  } = useNotificationItem({ notification, removeNotification });

  const renderIcon = () => {
    switch (type) {
      case 'success':
        return <span className="text-green-500 text-lg">✓</span>;
      case 'error':
        return <span className="text-red-500 text-lg">✕</span>;
      case 'action':
        return <span className="text-blue-500 text-lg">🤝</span>;
      case 'info':
      default:
        return <span className="text-blue-500 text-lg">ℹ</span>;
    }
  };

  return (
    <div className={baseClasses}>
      <div className="flex items-center p-4">
        <div className="flex-shrink-0 mr-3 flex items-center justify-center">
          {renderIcon()}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-900 leading-tight">
            {message}
          </p>
        </div>
        {type !== 'action' && (
          <button
            onClick={handleDismiss}
            className="ml-4 flex-shrink-0 inline-flex text-gray-400 hover:text-gray-500 focus:outline-none cursor-pointer"
          >
            <span className="text-xl leading-none">&times;</span>
          </button>
        )}
      </div>

      {type === 'action' && (
        <div className="flex border-t border-gray-100 divide-x divide-gray-100">
          <button
            onClick={handleReject}
            className="w-0 flex-1 flex items-center justify-center py-3 text-sm font-semibold text-gray-600 hover:text-gray-700 hover:bg-gray-50 transition-colors cursor-pointer"
          >
            უარყოფა
          </button>
          <button
            onClick={handleAccept}
            className="w-0 flex-1 flex items-center justify-center py-3 text-sm font-semibold text-blue-600 hover:text-blue-700 hover:bg-blue-50 transition-colors cursor-pointer"
          >
            დათანხმება
          </button>
        </div>
      )}
    </div>
  );
};

export default NotificationItem;
