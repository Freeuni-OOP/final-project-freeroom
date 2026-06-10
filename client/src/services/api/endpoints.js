import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/profile');
