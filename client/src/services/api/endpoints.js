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

export const getChatMessages = (roomId, beforeId) => axiosInstance.get(beforeId ? `/chat/${roomId}?beforeId=${beforeId}` : `/chat/${roomId}`).then(res => res.data);

export const sendChatMessage = (roomId, message) => axiosInstance.post('/chat/send', { roomId, message });

export const requestJoinRoom = (roomId) => axiosInstance.post('/chat/request-join', { roomId });

export const approveJoinRequest = (roomId, targetUserId) => axiosInstance.post('/chat/approve', { roomId, targetUserId });

export const rejectJoinRequest = (roomId, targetUserId) => axiosInstance.post('/chat/reject', { roomId, targetUserId });

export const getNotificationPreference = () => axiosInstance.get('/user/notification-preference');

export const updateNotificationPreference = (preference) => axiosInstance.patch('/user/notification-preference', { preference });

export const generateTelegramLink = () => axiosInstance.post('/user/telegram-link');

export const getSavedSubjects = () => axiosInstance.get('/user/subjects');
export const addSavedSubject = (subjectId) => axiosInstance.post(`/user/subjects/${subjectId}`);
export const removeSavedSubject = (subjectId) => axiosInstance.delete(`/user/subjects/${subjectId}`);
export const getUserCalendar = () => axiosInstance.get('/user/calendar');

export const getAllSubjects = () => axiosInstance.get('/subjects');
export const searchSubjects = (query) => axiosInstance.get(`/subjects/search?q=${query}`);

export const searchUsers = (query) => axiosInstance.get(`/friends/search?q=${encodeURIComponent(query)}`);
export const getFriends = () => axiosInstance.get('/friends');

export const getIncomingFriendRequests = () => axiosInstance.get('/friends/requests/incoming');
export const sendFriendRequest = (receiverId) => axiosInstance.post('/friends/requests', { receiverId });

export const acceptFriendRequest = (requestId) => axiosInstance.patch(`/friends/requests/${requestId}/accept`);
export const rejectFriendRequest = (requestId) => axiosInstance.patch(`/friends/requests/${requestId}/reject`);

export const removeFriend = (friendId) => axiosInstance.delete(`/friends/${friendId}`);
export const cancelFriendRequest = (userId) => axiosInstance.delete(`/friends/requests/${userId}`);

export const getPublicProfile = (userId) => axiosInstance.get(`/user/${userId}/profile`);

export const reportUser = (userId, reason, details) => axiosInstance.post(`/users/${userId}/report`, { reason, details })