import { useState, useRef } from 'react';
import { searchLectures } from '@/services';

export default function useLectureSearch() {
    const [query, setQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const latestQueryRef = useRef('');
    const lastSentQueryRef = useRef('');
    const lastFetchedResultsRef = useRef([]);
    const debounceTimeoutRef = useRef(null);

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
        }, 300);
    };

    const formatTime = (timeString) => {
        if (!timeString) return '';
        const date = new Date(timeString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    };

    return { query, searchResults, loading, handleSearch, formatTime };
}