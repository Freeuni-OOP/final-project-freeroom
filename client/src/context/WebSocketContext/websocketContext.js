import { createContext, useContext } from 'react';
export const WebSocketContext = createContext(null);
export const useRealtime = () => {
    const ctx = useContext(WebSocketContext);
    if (!ctx) throw new Error('useRealtime must be used within WebSocketProvider');
    return ctx;
};