import { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { useAuth, useNotification } from '@/context';
import { connectStomp, disconnectStomp } from '@/services/websocket/stompClient';
import { WebSocketContext } from './websocketContext';
import { FRIEND_EVENT_TYPE } from '@/utils';

const FRIEND_EVENT_MESSAGES = {
    [FRIEND_EVENT_TYPE.REQUEST_SENT]: (a) => ({ type: 'info', message: `${a}-მა გამოგიგზავნათ მეგობრობის მოთხოვნა` }),
    [FRIEND_EVENT_TYPE.REQUEST_ACCEPTED]: (a) => ({ type: 'success', message: `${a}-მა დაეთანხმა თქვენს მოთხოვნას` }),
};

const WebSocketProvider = ({ children }) => {
    const { user } = useAuth();
    const { showNotification, addPersistentNotification, fetchNotifications } = useNotification();
    const friendListenersRef = useRef(new Set());
    const roomListenersRef = useRef(new Set());
    const stompClientRef = useRef(null);
    const [connected, setConnected] = useState(false);
    const openChatRoomIdRef = useRef(null);

    const setIsChatOpenGlobal = useCallback((roomId, isOpen) => {
        if (isOpen) {
            openChatRoomIdRef.current = roomId;
        } else if (openChatRoomIdRef.current === roomId) {
            openChatRoomIdRef.current = null;
        }
    }, []);

    const onFriendEvent = useCallback((callback) => {
        friendListenersRef.current.add(callback);
        return () => friendListenersRef.current.delete(callback);
    }, []);

    const onRoomEvent = useCallback((callback) => {
        roomListenersRef.current.add(callback);
        return () => roomListenersRef.current.delete(callback);
    }, []);

    const subscribeToProfile = useCallback((userId, callback) => {
        if (!stompClientRef.current || !userId) return () => {};

        const subscription = stompClientRef.current.subscribe(`/topic/users/${userId}/profile`, (message) => {
            callback(JSON.parse(message.body));
        });

        return () => subscription.unsubscribe();
    }, []);

    const subscribeToRoom = useCallback((roomId, onMessage, onReload) => {
        if (!stompClientRef.current || !roomId) return () => {};

        const msgSub = stompClientRef.current.subscribe(`/topic/room/${roomId}`, (message) => {
            onMessage(JSON.parse(message.body));
        });

        const reloadSub = stompClientRef.current.subscribe(`/topic/room/${roomId}/reload`, (message) => {
            onReload(message.body);
        });

        return () => {
            msgSub.unsubscribe();
            reloadSub.unsubscribe();
        };
    }, []);

    useEffect(() => {
        if (!user) {
            disconnectStomp();
            stompClientRef.current = null;
            return () => setConnected(false);
        }

        fetchNotifications();

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

                    client.subscribe(`/topic/users/${user.uid}/notifications`, (message) => {
                        const notif = JSON.parse(message.body);
                        
                        if (notif.id) {
                            addPersistentNotification(notif);
                        }

                        const handledByFriendSub = new Set(['FRIEND_REQUEST_RECEIVED', 'FRIEND_REQUEST_ACCEPTED']);
                        if (handledByFriendSub.has(notif.type)) return;

                        const chatTypes = new Set(['CHAT_JOIN_REQUEST', 'CHAT_JOIN_APPROVED', 'CHAT_JOIN_REJECTED', 'CHAT_MESSAGE']);
                        if (chatTypes.has(notif.type) && openChatRoomIdRef.current === notif.referenceId) {
                            return;
                        }

                        showNotification({ type: 'info', message: notif.message });
                    });

                    stompClientRef.current = client;
                    setConnected(true);
                },
            });
        };

        void setup();

        return () => {
            cancelled = true;
            disconnectStomp();
            stompClientRef.current = null;
            setConnected(false);
        };
    }, [user, showNotification, addPersistentNotification, fetchNotifications]);

    const value = useMemo(
        () => ({ onFriendEvent, onRoomEvent, subscribeToProfile, subscribeToRoom, connected, setIsChatOpenGlobal }),
        [onFriendEvent, onRoomEvent, subscribeToProfile, subscribeToRoom, connected, setIsChatOpenGlobal]
    );

    return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>;
};

export default WebSocketProvider;