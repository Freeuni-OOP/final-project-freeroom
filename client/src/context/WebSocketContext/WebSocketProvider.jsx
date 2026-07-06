import { useEffect, useRef, useMemo, useCallback } from 'react';
import { useAuth, useNotification } from '@/context';
import { connectStomp, disconnectStomp } from '@/services/websocket/stompClient';
import { WebSocketContext } from './websocketContext';
import { FRIEND_EVENT_TYPE } from '@/utils';

const FRIEND_EVENT_MESSAGES = {
    [FRIEND_EVENT_TYPE.REQUEST_SENT]: (a) => ({ type: 'info', message: `${a} გამოგიგზავნათ მეგობრობის მოთხოვნა` }),
    [FRIEND_EVENT_TYPE.REQUEST_ACCEPTED]: (a) => ({ type: 'success', message: `${a} დაეთანხმა თქვენს მოთხოვნას` }),
};

const WebSocketProvider = ({ children }) => {
    const { user } = useAuth();
    const { showNotification } = useNotification();
    const listenersRef = useRef(new Set());

    const onFriendEvent = useCallback((callback) => {
        listenersRef.current.add(callback);
        return () => listenersRef.current.delete(callback);
    }, []);

    useEffect(() => {
        if (!user) {
            disconnectStomp();
            return;
        }

        let cancelled = false;

        const setup = async () => {
            await connectStomp({
                onConnect: (client) => {
                    if (cancelled) return;
                    client.subscribe(`/topic/users/${user.uid}/friends`, (message) => {
                        const event = JSON.parse(message.body);
                        listenersRef.current.forEach((cb) => cb(event));

                        const toast = FRIEND_EVENT_MESSAGES[event.type]?.(event.actorDisplayName);
                        if (toast) showNotification(toast);
                    });
                },
            });
        };

        void setup();

        return () => {
            cancelled = true;
            disconnectStomp();
        };
    }, [user, showNotification]);

    const value = useMemo(() => ({ onFriendEvent }), [onFriendEvent]);

    return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>;
};

export default WebSocketProvider;