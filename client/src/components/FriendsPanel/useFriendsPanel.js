import { useAuth } from '@/context';
import { getUniversity } from '@/utils';
import { useState, useEffect } from 'react';
import { useNotification } from '@/context';
import { RELATIONSHIP_STATUS } from '@/utils';
import useDebounce from '@/hooks/useDebounce';
import { useRealtime } from '@/context';
import {
    getFriends,
    getIncomingFriendRequests,
    searchUsers,
    sendFriendRequest,
    acceptFriendRequest,
    rejectFriendRequest,
} from '@/services/api/endpoints';

const formatTime = (isoString) =>
    new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

const useFriendsPanel = () => {
    const { user } = useAuth();
    const university = getUniversity(user?.email);
    const { onFriendEvent } = useRealtime();
    const [activeTab, setActiveTab] = useState('friends');
    const [requestSubTab, setRequestSubTab] = useState('send');
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [friends, setFriends] = useState([]);
    const [incomingRequests, setIncomingRequests] = useState([]);
    const [isLoadingFriends, setIsLoadingFriends] = useState(false);
    const [isLoadingRequests, setIsLoadingRequests] = useState(false);
    const [isSearching, setIsSearching] = useState(false);
    const [actionPending, setActionPending] = useState(new Set());
    const { showNotification } = useNotification();

    const debouncedQuery = useDebounce(searchQuery, 400);

    const addPending = (id) =>
        setActionPending((prev) => new Set([...prev, id]));

    const removePending = (id) =>
        setActionPending((prev) => {
            const next = new Set(prev);
            next.delete(id);
            return next;
        });

    const loadFriends = async () => {
        setIsLoadingFriends(true);
        try {
            const res = await getFriends();
            setFriends(res.data);
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoadingFriends(false);
        }
    };

    const loadIncomingRequests = async () => {
        setIsLoadingRequests(true);
        try {
            const res = await getIncomingFriendRequests();
            setIncomingRequests(res.data);
        } catch (e) {
            console.error(e);
        } finally {
            setIsLoadingRequests(false);
        }
    };

    const runSearch = async (query) => {
        setIsSearching(true);
        try {
            const res = await searchUsers(query);
            setSearchResults(res.data);
        } catch (e) {
            console.error(e);
            setSearchResults([]);
        } finally {
            setIsSearching(false);
        }
    };

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadFriends();
        void loadIncomingRequests();
    }, []);

    useEffect(() => {
        if (debouncedQuery.trim().length >= 2) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            void runSearch(debouncedQuery.trim());
        } else {
            setSearchResults([]);
        }
    }, [debouncedQuery]);

    useEffect(() => {
        const unsubscribe = onFriendEvent((event) => {
            const patchSearch = (status) =>
                setSearchResults((prev) =>
                    prev.map((u) => (u.id === event.actorId ? { ...u, relationshipStatus: status } : u))
                );

            switch (event.type) {
                case 'REQUEST_SENT':
                    void loadIncomingRequests();
                    patchSearch(RELATIONSHIP_STATUS.PENDING_RECEIVED);
                    break;
                case 'REQUEST_ACCEPTED':
                    void loadFriends();
                    void loadIncomingRequests();
                    patchSearch(RELATIONSHIP_STATUS.FRIENDS);
                    break;
                case 'REQUEST_REJECTED':
                    patchSearch(RELATIONSHIP_STATUS.NONE);
                    break;
                case 'REQUEST_CANCELLED':
                    void loadIncomingRequests();
                    patchSearch(RELATIONSHIP_STATUS.NONE);
                    break;
                case 'FRIEND_REMOVED':
                    void loadFriends();
                    void loadIncomingRequests();
                    patchSearch(RELATIONSHIP_STATUS.NONE);
                    break;
                default:
                    break;
            }
        });
        return unsubscribe;
    }, [onFriendEvent]);

    const handleSendRequest = async (receiverId) => {
        addPending(receiverId);
        try {
            await sendFriendRequest(receiverId);
            setSearchResults((prev) =>
                prev.map((u) =>
                    u.id === receiverId ? { ...u, relationshipStatus: RELATIONSHIP_STATUS.PENDING_SENT } : u
                )
            );
            showNotification({ message: 'მეგობრობის მოთხოვნა გაგზავნილია', type: 'success' });
        } catch (e) {
            console.error(e);
            showNotification({ message: 'მოთხოვნის გაგზავნა ვერ მოხერხდა.', type: 'error' });
        } finally {
            removePending(receiverId);
        }
    };

    const handleAccept = async (requestId) => {
        addPending(requestId);
        try {
            await acceptFriendRequest(requestId);
            setIncomingRequests((prev) =>
                prev.filter((r) => r.requestId !== requestId)
            );
            await loadFriends();
            showNotification({ message: 'თქვენ ახლა მეგობრები ხართ.', type: 'success' });
        } catch (e) {
            console.error(e);
            showNotification({ message: 'დადასტურება ვერ მოხერხდა.', type: 'error'})
        } finally {
            removePending(requestId);
        }
    };

    const handleAcceptFromSearch = async (senderId) => {
        const request = incomingRequests.find((r) => r.senderId === senderId);
        if (!request) return;
        addPending(senderId);
        try {
            await acceptFriendRequest(request.requestId);
            setIncomingRequests((prev) =>
                prev.filter((r) => r.requestId !== request.requestId)
            );
            setSearchResults((prev) =>
                prev.map((u) =>
                    u.id === senderId ? { ...u, relationshipStatus: RELATIONSHIP_STATUS.FRIENDS } : u
                )
            );
            await loadFriends();
            showNotification({ message: 'თქვენ ახლა მეგობრები ხართ.', type: 'success' });
        } catch (e) {
            console.error(e);
            showNotification({ message: 'დადასტურება ვერ მოხერხდა.', type: 'error'})
        } finally {
            removePending(senderId);
        }
    };

    const handleReject = async (requestId) => {
        addPending(requestId);
        try {
            await rejectFriendRequest(requestId);
            setIncomingRequests((prev) =>
                prev.filter((r) => r.requestId !== requestId)
            );
            showNotification({ message: 'მეგობრობის მოთხოვნა უარყოფილია', type: 'success' });
        } catch (e) {
            console.error(e);
            showNotification({ message: 'უარყოფა ვერ მოხერხდა.', type: 'error'})
        } finally {
            removePending(requestId);
        }
    };

    const formattedFriends = friends.map((f) => ({
        ...f,
        occupancyLabel:
            f.hasActiveOccupancy && f.currentOccupancy
                ? `ოთახი ${f.currentOccupancy.roomNumber} · ${f.currentOccupancy.floorNumber} სართული · ${formatTime(f.currentOccupancy.expectedEndAt)}-მდე`
                : null,
    }));

    const hasSearched =
        debouncedQuery.trim().length >= 2 && !isSearching;

    return {
        activeTab,
        setActiveTab,
        requestSubTab,
        setRequestSubTab,
        searchQuery,
        setSearchQuery,
        searchResults,
        friends: formattedFriends,
        incomingRequests,
        isLoadingFriends,
        isLoadingRequests,
        isSearching,
        hasSearched,
        actionPending,
        handleSendRequest,
        handleAccept,
        handleAcceptFromSearch,
        handleReject,
        university,
    };
};

export default useFriendsPanel;