import { useState, useEffect, useRef } from 'react';
import { searchLectures } from '@/services';
import { useAuth } from '@/context';
import { getUniversity } from '@/utils';

const DEBOUNCE_DELAY_MS = 300;
const TIME_FORMAT_LOCALE = [];

export default function useLectureSearch() {
    const { user } = useAuth();
    const [query, setQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const latestQueryRef = useRef('');
    const lastSentQueryRef = useRef('');
    const lastFetchedResultsRef = useRef([]);
    const debounceTimeoutRef = useRef(null);

    const university = getUniversity(user?.email);

    useEffect(() => {
        return () => {
            if (debounceTimeoutRef.current) {
                clearTimeout(debounceTimeoutRef.current);
            }
        };
    }, []);

    const handleSearch = (userInput) => {
        setQuery(userInput);
        latestQueryRef.current = userInput;

        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current);
        }

        if (!userInput.trim()) {
            setSearchResults([]);
            setLoading(false);
            return;
        }

        setLoading(true);

        debounceTimeoutRef.current = setTimeout(async () => {
            if (userInput === lastSentQueryRef.current) {
                setSearchResults(lastFetchedResultsRef.current);
                setLoading(false);
                return;
            }

            lastSentQueryRef.current = userInput;

            try {
                const secureQuery = encodeURIComponent(userInput);
                const response = await searchLectures(secureQuery);

                if (userInput === latestQueryRef.current) {
                    lastFetchedResultsRef.current = response.data;
                    setSearchResults(response.data);
                }
            } catch (error) {
                if (userInput === latestQueryRef.current) {
                    console.error("Error fetching lectures:", error);
                }
            } finally {
                if (userInput === latestQueryRef.current) {
                    setLoading(false);
                }
            }
        }, DEBOUNCE_DELAY_MS);
    };

    const formatTime = (timeString) => {
        if (!timeString) return '';
        const date = new Date(timeString);
        return date.toLocaleTimeString(TIME_FORMAT_LOCALE, { hour: '2-digit', minute: '2-digit', hour12: false });
    };

    return { query, searchResults, loading, handleSearch, formatTime, university };
}