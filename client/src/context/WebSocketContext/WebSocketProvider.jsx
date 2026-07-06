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
    const friendListenersRef = useRef(new Set());
    const roomListenersRef = useRef(new Set());

    const onFriendEvent = useCallback((callback) => {
        friendListenersRef.current.add(callback);
        return () => friendListenersRef.current.delete(callback);
    }, []);

    const onRoomEvent = useCallback((callback) => {
        roomListenersRef.current.add(callback);
        return () => roomListenersRef.current.delete(callback);
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
                        friendListenersRef.current.forEach((cb) => cb(event));

                        const toast = FRIEND_EVENT_MESSAGES[event.type]?.(event.actorDisplayName);
                        if (toast) showNotification(toast);
                    });

                    client.subscribe('/topic/rooms', (message) => {
                        const event = JSON.parse(message.body);
                        roomListenersRef.current.forEach((cb) => cb(event));
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

    const value = useMemo(() => ({ onFriendEvent, onRoomEvent }), [onFriendEvent, onRoomEvent]);

    return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>;
};

export default WebSocketProvider;