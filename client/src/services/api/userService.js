import axiosInstance from './axiosInstance';

export const fetchProfile = async () => {
    const response = await axiosInstance.get('/user');
    return response?.data;
};

export const updateProfile = async (formData) => {
    const response = await axiosInstance.patch('/user', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response?.data;
};