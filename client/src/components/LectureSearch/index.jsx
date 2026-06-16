import { useState } from 'react';
import { searchLectures } from '@/services';

export default function LectureSearch() {
    const [query, setQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (userInput) => {
        setQuery(userInput);
        if (!userInput.trim()) {
            setSearchResults([]);
            return;
        }

        setLoading(true);
        try {
            const secureQuery = encodeURIComponent(userInput);

            const response = await searchLectures(secureQuery);

            setSearchResults(response.data);
        } catch (error) {
            console.error("Error fetching lectures:", error);
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (timeString) => {
        if (!timeString) return '';
        const date = new Date(timeString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    };

    return (
        <div style={{
            padding: '20px',
            background: '#ffffff',
            borderRadius: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            color: '#333333'
        }}>
            <h3 style={{ marginBottom: '15px', color: '#2c3e50' }}>ლექციების ძებნა</h3>

            <input
                type="text"
                placeholder="ჩაწერეთ საგნის დასახელება..."
                value={query}
                onChange={(e) => handleSearch(e.target.value)}
                style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '6px',
                    border: '1px solid #cccccc',
                    fontSize: '14px',
                    marginBottom: '15px',
                    outline: 'none'
                }}
            />

            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {loading && <p style={{ color: '#666' }}>იტვირთება...</p>}
                {searchResults.length === 0 && query && !loading && (
                    <p style={{ color: '#999', textAlign: 'center' }}>ლექციები ვერ მოიძებნა</p>
                )}

                {searchResults.map((lecture) => (
                    <div
                        key={lecture.id}
                        style={{
                            padding: '12px',
                            borderBottom: '1px solid #eeeeee',
                            marginBottom: '8px',
                            background: '#f9f9f9',
                            borderRadius: '4px'
                        }}
                    >
                        <div style={{ display: 'flex', justifyContent: 'between', alignItems: 'center', marginBottom: '5px' }}>
              <span style={{
                  background: '#f5b041',
                  color: '#fff',
                  padding: '2px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold'
              }}>
                {formatTime(lecture.startAt)} - {formatTime(lecture.endAt)}
              </span>
                        </div>
                        <h4 style={{ margin: '0 0 5px 0', color: '#2c3e50', fontSize: '15px' }}>{lecture.title}</h4>
                        <div style={{ fontSize: '13px', color: '#666' }}>
                            ოთახი: <strong style={{ color: '#2980b9' }}>{lecture.room?.roomNumber || lecture.roomId}</strong>
                            {lecture.description && ` | ${lecture.description}`}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}