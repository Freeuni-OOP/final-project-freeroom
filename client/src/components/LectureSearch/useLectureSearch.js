import { useState, useRef } from 'react';
import { searchLectures } from '@/services';

export default function useLectureSearch() {
    const [query, setQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const latestQueryRef = useRef('');

    const handleSearch = async (userInput) => {
        setQuery(userInput);
        latestQueryRef.current = userInput;

        if (!userInput.trim()) {
            setSearchResults([]);
            return;
        }

        setLoading(true);
        try {
            const secureQuery = encodeURIComponent(userInput);
            const response = await searchLectures(secureQuery);

            if (userInput === latestQueryRef.current) {
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
    };

    const formatTime = (timeString) => {
        if (!timeString) return '';
        const date = new Date(timeString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    };

    return { query, searchResults, loading, handleSearch, formatTime };
}