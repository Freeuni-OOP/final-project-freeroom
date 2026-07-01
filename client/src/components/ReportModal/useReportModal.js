import { useState } from 'react';
import { reportUser } from '@/services/api/endpoints';
import { REPORT_REASON } from '@/utils';

const useReportModal = (userId) => {
    const [selectedReason, setSelectedReason] = useState(null);
    const [details, setDetails] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [error, setError] = useState('');

    const submitReport = async (reason, reasonDetails) => {
        setIsSubmitting(true);
        setError('');
        try {
            await reportUser(userId, reason, reasonDetails || null);
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

    const handleSelectReason = (reason) => {
        setSelectedReason(reason);
        if (reason !== REPORT_REASON.OTHER) {
            submitReport(reason, null);
        }
    };

    const handleSubmitOther = () => {
        if (!details.trim()) return;
        submitReport(REPORT_REASON.OTHER, details.trim());
    };

    return {
        selectedReason,
        details,
        setDetails,
        isSubmitting,
        isSubmitted,
        error,
        handleSelectReason,
        handleSubmitOther,
    };
};

export default useReportModal;