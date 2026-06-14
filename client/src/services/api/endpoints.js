import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/profile');

export const getRoomsMap = () => axiosInstance.get('/rooms/map');
