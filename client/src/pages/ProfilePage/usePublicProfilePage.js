import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useNotification } from '@/context';
import { RELATIONSHIP_STATUS } from '@/utils';
import { useAuth } from '@/context';
import { getUniversity } from '@/utils';
import {
    getPublicProfile,
    getIncomingFriendRequests,
    sendFriendRequest,
    acceptFriendRequest,
    rejectFriendRequest,
    removeFriend,
    cancelFriendRequest,
} from '@/services/api/endpoints';

const getInitial = (name) => {
    const source = name?.trim() || '';
    return source ? source[0].toUpperCase() : '?';
};

const usePublicProfilePage = () => {
    const { user } = useAuth();
    const university = getUniversity(user?.email);
    const { userId } = useParams();
    const navigate = useNavigate();
    const { showNotification } = useNotification();

    const [profile, setProfile] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [notFound, setNotFound] = useState(false);
    const [photoFailed, setPhotoFailed] = useState(false);
    const [actionPending, setActionPending] = useState(false);
    const [requestId, setRequestId] = useState(null);

    const fetchedUserIdRef = useRef(null);

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isReportOpen, setIsReportOpen] = useState(false);

    useEffect(() => {
        if(!userId || fetchedUserIdRef.current === userId) return;

        let isMounted = true;
        setIsLoading(true);
        setNotFound(false);
        setPhotoFailed(false);

        getPublicProfile(userId)
            .then(async (res) => {
                if(!isMounted) return;
                const data = res.data;

                if(data.relationshipStatus === RELATIONSHIP_STATUS.SELF) {
                    navigate('/profile', { replace: true });
                    return;
                }

                setProfile(data);
                fetchedUserIdRef.current = userId;

                if(data.relationshipStatus === RELATIONSHIP_STATUS.PENDING_RECEIVED) {
                    try {
                        const incomingRes = await getIncomingFriendRequests();
                        const match = incomingRes.data.find((r) => r.senderId === data.id);
                        if (isMounted && match)
                            setRequestId(match.requestId);

                    } catch(e) {
                        console.log(e);
                    }
                }

            })
            .catch((err) => {
                if(!isMounted) return;
                if(err.response?.status === 404) {
                    setNotFound(true);
                } else {
                    console.log(err);
                    showNotification({ message: 'პროფილის ჩატვირთვა ვერ მოხერხდა.', type: 'error' });
                }
            })
            .finally(() => {
                if (isMounted) setIsLoading(false);
            })

        return () => {
            isMounted = false;
        };
    }, [userId, navigate, showNotification]);


    const handleSendRequest = async () => {
        if(!profile) return;
        setActionPending(true);
        try {
            await sendFriendRequest(profile.id);
            setProfile( (prev) => ({...prev, relationshipStatus: RELATIONSHIP_STATUS.PENDING_SENT}));
            showNotification({ message: 'მეგობრობის მოთხოვნა გაგზავნილია', type: 'success' });
        } catch(e) {
            console.error(e);
            showNotification({ message: 'მოთხოვნის გაგზავნა ვერ მოხერხდა.', type: 'error' });
        } finally {
            setActionPending(false);
        }
    };

    const handleAccept = async () => {
        if(!profile || !requestId) return;
        setActionPending(true);
        try {
            await acceptFriendRequest(requestId);
            setProfile((prev) => ({...prev, relationshipStatus: RELATIONSHIP_STATUS.FRIENDS}))
            showNotification({ message: 'თქვენ ახლა მეგობრები ხართ.', type: 'success' });
        } catch(e) {
            console.error(e);
            showNotification({ message: 'დადასტურება ვერ მოხერხდა.', type: 'error'})
        } finally {
            setActionPending(false);
        }
    };

    const handleReject = async () => {
        if(!profile || !requestId) return;
        setActionPending(true);
        try {
            await rejectFriendRequest(requestId);
            setProfile((prev) => ({...prev, relationshipStatus: RELATIONSHIP_STATUS.NONE}))
            showNotification({ message: 'მეგობრობის მოთხოვნა უარყოფილია', type: 'success' });
        } catch(e) {
            console.error(e);
            showNotification({ message: 'უარყოფა ვერ მოხერხდა.', type: 'error'})
        } finally {
            setActionPending(false);
        }
    };

    const handleUnfriend = async () => {
        if(!profile) return;
        setActionPending(true);
        try {
            await removeFriend(profile.id);
            setProfile((prev) => ({...prev, relationshipStatus: RELATIONSHIP_STATUS.NONE}))
            showNotification({ message: 'მეგობრობა გაუქმებულია', type: 'success' });
        } catch(e) {
            console.error(e);
            showNotification({ message: 'მეგობრობის გაუქმება ვერ მოხერხდა.', type: 'error'})
        } finally {
            setActionPending(false);
        }
    };

    const handleCancelRequest = async () => {
        if(!profile || !requestId) return;
        setActionPending(true);
        try {
            await cancelFriendRequest(profile.id);
            setProfile((prev) => ({...prev, relationshipStatus: RELATIONSHIP_STATUS.NONE}))
            showNotification({ message: 'მეგობრობის მოთხოვნა გაუქმებულია', type: 'success' });
        } catch(e) {
            console.error(e);
            showNotification({ message: 'მეგობრობის მოთხოვნის გაუქმება ვერ მოხერხდა.', type: 'error'})
        } finally {
            setActionPending(false);
        }
    };

    const toggleMenu = () => setIsMenuOpen((prev) => !prev);
    const closeMenu = () => setIsMenuOpen(false);
    const openReport = () => {
        setIsMenuOpen(false);
        setIsReportOpen(true);
    };
    const closeReport = () => setIsReportOpen(false);

    return {
        profile,
        isLoading,
        notFound,
        showPhoto: Boolean(profile?.photoUrl) && !photoFailed,
        initial: getInitial(profile?.displayName),
        handlePhotoError: () => setPhotoFailed(true),
        actionPending,
        canRequest: profile?.relationshipStatus === RELATIONSHIP_STATUS.NONE,
        isPendingSent: profile?.relationshipStatus === RELATIONSHIP_STATUS.PENDING_SENT,
        isPendingReceived: profile?.relationshipStatus === RELATIONSHIP_STATUS.PENDING_RECEIVED,
        isFriends: profile?.relationshipStatus === RELATIONSHIP_STATUS.FRIENDS,
        handleSendRequest,
        handleAccept,
        handleReject,
        handleUnfriend,
        handleCancelRequest,
        university,
        isMenuOpen,
        isReportOpen,
        toggleMenu,
        closeMenu,
        openReport,
        closeReport,
    };
};

export default usePublicProfilePage;