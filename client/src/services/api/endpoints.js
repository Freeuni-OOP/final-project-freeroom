import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/profile');

export const getRoomsMap = () => axiosInstance.get('/rooms/map');

export const reserveRoom = (roomDbId, durationMinutes) => axiosInstance.post('/reserve', { roomDbId, durationMinutes });

export const syncUser = () => axiosInstance.post('/user/sync')

export const searchLectures = (query) => {
    return axiosInstance.get(`/lectures/search?q=${query}`);
};