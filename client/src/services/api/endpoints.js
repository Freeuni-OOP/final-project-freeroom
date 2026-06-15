import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/profile');

export const getRoomsMap = () => axiosInstance.get('/rooms/map');

export const reserveRoom = (roomId, durationMinutes) => axiosInstance.post('/reserve', { roomId, durationMinutes });