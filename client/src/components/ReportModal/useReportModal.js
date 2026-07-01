import { useState } from 'react';
import { reportUser } from '@/services/api/endpoints';
import { REPORT_REASON } from '@/utils';

const useReportModal = (userId) => {
    const [selectedReason, setSelectedReason] = useState(null);
    const [details, setDetails] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [error, setError] = useState('');

    const handleSelectReason = (reason) => {
        setSelectedReason(reason);
        setError('');
    };

    const handleSubmit = async () => {
        if (!selectedReason) return;
        if (selectedReason === REPORT_REASON.OTHER && !details.trim()) return;

        setIsSubmitting(true);
        setError('');
        try {
            await reportUser(userId, selectedReason, selectedReason === REPORT_REASON.OTHER ? details.trim() : null);
            setIsSubmitted(true);
        } catch (err) {
            console.error(err);
            if (err.response?.status === 409) {
                setError('თქვენ უკვე დარეპორტეთ ეს მომხმარებელი.');
            } else if (err.response?.status === 400) {
                setError('არასწორი მოთხოვნა.');
            } else {
                setError('შეცდომა მოხდა, სცადეთ თავიდან.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const canSubmit =
        selectedReason !== null &&
        (selectedReason !== REPORT_REASON.OTHER || details.trim().length > 0);

    return {
        selectedReason,
        details,
        setDetails,
        isSubmitting,
        isSubmitted,
        error,
        canSubmit,
        handleSelectReason,
        handleSubmit,
    };
};

export default useReportModal;