import { useState, useEffect, useMemo } from 'react';
import { getAllSubjects, getSavedSubjects, addSavedSubject, removeSavedSubject } from '@/services/api/endpoints';

export default function useSubjectsPage() {
    const [allSubjects, setAllSubjects] = useState([]);
    const [savedSubjectIds, setSavedSubjectIds] = useState(new Set());
    const [savedSubjectsList, setSavedSubjectsList] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [isSavedExpanded, setIsSavedExpanded] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [allRes, savedRes] = await Promise.all([
                    getAllSubjects(),
                    getSavedSubjects()
                ]);

                setAllSubjects(allRes.data || []);

                const savedList = savedRes.data || [];
                setSavedSubjectsList(savedList);
                setSavedSubjectIds(new Set(savedList.map(s => s.id)));
            } catch (err) {
                console.error("Error fetching subjects", err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const toggleSubject = async (subject) => {
        const isSaved = savedSubjectIds.has(subject.id);
        try {
            if (isSaved) {
                setSavedSubjectIds(prev => {
                    const next = new Set(prev);
                    next.delete(subject.id);
                    return next;
                });
                setSavedSubjectsList(prev => prev.filter(s => s.id !== subject.id));
                await removeSavedSubject(subject.id);
            } else {
                setSavedSubjectIds(prev => new Set(prev).add(subject.id));
                setSavedSubjectsList(prev => [...prev, subject]);
                await addSavedSubject(subject.id);
            }
        } catch (err) {
            console.error("Error toggling subject", err);
        }
    };

    const filteredSubjects = useMemo(() => {
        if (!searchQuery.trim()) return allSubjects;
        const lowerQ = searchQuery.toLowerCase();
        return allSubjects.filter(s =>
            s.title?.toLowerCase().includes(lowerQ) ||
            s.lecturer?.toLowerCase().includes(lowerQ) ||
            s.groupNumber?.toLowerCase().includes(lowerQ)
        );
    }, [allSubjects, searchQuery]);

    return {
        savedSubjectsList,
        filteredSubjects,
        savedSubjectIds,
        searchQuery,
        setSearchQuery,
        loading,
        toggleSubject,
        isSavedExpanded,
        setIsSavedExpanded
    };
}
