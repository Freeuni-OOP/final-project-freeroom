import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserCalendar } from '@/services/api/endpoints';

export default function useCalendarPage() {
    const [lectures, setLectures] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const goToSubjects = () => navigate('/subjects');

    useEffect(() => {
        const fetchCalendar = async () => {
            try {
                const res = await getUserCalendar();
                setLectures(res.data || []);
            } catch (err) {
                console.error("Error fetching calendar", err);
            } finally {
                setLoading(false);
            }
        };

        fetchCalendar();
    }, []);

    const groupedLectures = useMemo(() => {
        const groups = {};

        lectures.forEach(lecture => {
            if (!lecture.startAt) return;
            const date = new Date(lecture.startAt);
            const dateKey = date.toISOString().split('T')[0];

            if (!groups[dateKey]) {
                groups[dateKey] = {
                    dateObj: date,
                    lectures: []
                };
            }
            groups[dateKey].lectures.push(lecture);
        });

        const sortedDates = Object.keys(groups).sort();

        return sortedDates.map(dateKey => ({
            dateStr: dateKey,
            dateObj: groups[dateKey].dateObj,
            lectures: groups[dateKey].lectures.sort((a, b) => new Date(a.startAt) - new Date(b.startAt))
        }));
    }, [lectures]);

    const formatDateHeading = (dateObj) => {
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);

        const isToday = dateObj.toDateString() === today.toDateString();
        const isTomorrow = dateObj.toDateString() === tomorrow.toDateString();

        const months = [
            'იანვარი', 'თებერვალი', 'მარტი', 'აპრილი', 'მაისი', 'ივნისი',
            'ივლისი', 'აგვისტო', 'სექტემბერი', 'ოქტომბერი', 'ნოემბერი', 'დეკემბერი'
        ];

        const weekdays = [
            'კვირა', 'ორშაბათი', 'სამშაბათი', 'ოთხშაბათი',
            'ხუთშაბათი', 'პარასკევი', 'შაბათი'
        ];

        const day = dateObj.getDate();
        const month = months[dateObj.getMonth()];
        const weekday = weekdays[dateObj.getDay()];

        const dateStr = `${weekday}, ${day} ${month}`;

        if (isToday) return `დღეს, ${dateStr}`;
        if (isTomorrow) return `ხვალ, ${dateStr}`;
        return dateStr;
    };

    const getLectureCardData = (lecture) => {
        const formatTime = (isoString) => {
            if (!isoString) return '';
            return new Date(isoString).toLocaleTimeString('ka-GE', { hour: '2-digit', minute: '2-digit' });
        };

        const startTime = formatTime(lecture.startAt);
        const endTime = formatTime(lecture.endAt);

        const now = new Date();
        const start = new Date(lecture.startAt);
        const end = new Date(lecture.endAt);
        const isHappeningNow = now >= start && now <= end;

        return { startTime, endTime, isHappeningNow };
    };

    return {
        loading,
        groupedLectures,
        isEmpty: lectures.length === 0,
        formatDateHeading,
        getLectureCardData,
        goToSubjects
    };
}
