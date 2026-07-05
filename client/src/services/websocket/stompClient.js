import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { auth } from '@/services/firebase';

let stompClient = null;

export const connectStomp = async ({ onConnect }) => {
    const user = auth.currentUser;
    if (!user) return null;

    const token = await user.getIdToken();

    const client = new Client({
        webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws?token=${token}`),
        reconnectDelay: 0,
        onConnect: () => onConnect?.(client),
        onStompError: (frame) => console.error('STOMP error', frame),
    });

    client.activate();
    stompClient = client;
    return client;
};

export const disconnectStomp = () => {
    stompClient?.deactivate();
    stompClient = null;
};