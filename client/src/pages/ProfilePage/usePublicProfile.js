import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth, useNotification } from '@/context';
import {
    getPublicProfile,
    getIncomingFriendRequests,
    sendFriendRequest,
    acceptFriendRequest,
    rejectFriendRequest,
} from '@/services/api/endpoints';
import { RELATIONSHIP_STATUS } from '@/utils';

const getInitial = (name) => {
    const source = name?.trim() || '';
    return source ? source[0].toUpperCase() : '?';
};