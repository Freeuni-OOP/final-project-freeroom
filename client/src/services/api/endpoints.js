import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/user');

export const getRoomsMap = () => axiosInstance.get('/rooms/map');

export const reserveRoom = (roomDbId, durationMinutes) => axiosInstance.post('/reserve', { roomDbId, durationMinutes });

export const cancelOccupancy = (roomId) => axiosInstance.post(`/rooms/${roomId}/cancel`);

export const syncUser = () => axiosInstance.post('/user/sync');

export const updateProfile = (bioData) => axiosInstance.post('/user/update', bioData);

export const searchLectures = (query) => {
    return axiosInstance.get(`/lectures/search?q=${query}`);
};

export const getChatMessages = (roomId) => axiosInstance.get(`/api/chat/${roomId}`).then(res => res.data);

export const sendChatMessage = (roomId, message) => axiosInstance.post('/api/chat/send', { roomId, message });

export const requestJoinRoom = (roomId) => axiosInstance.post('/api/chat/request-join', { roomId });

export const approveJoinRequest = (roomId, targetUserId) => axiosInstance.post('/api/chat/approve', { roomId, targetUserId });