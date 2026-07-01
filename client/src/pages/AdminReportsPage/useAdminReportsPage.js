import { useEffect, useState } from 'react';
import { getPendingReports } from '@/services/api/endpoints';
import { REPORT_REASON_LABELS } from '@/utils';

const useAdminReportsPage = () => {
    const [reports, setReports] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        let isMounted = true;
        getPendingReports()
            .then((res) => {
                if (isMounted) setReports(res.data);
            })
            .catch((err) => {
                console.error(err);
                if (isMounted) {
                    if (err.response?.status === 403) {
                        setError('წვდომა აკრძალულია.');
                    } else {
                        setError('რეპორტების ჩატვირთვა ვერ მოხერხდა.');
                    }
                }
            })
            .finally(() => {
                if (isMounted) setIsLoading(false);
            });
        return () => {
            isMounted = false;
        };
    }, []);

    const formattedReports = reports.map((r) => ({
        ...r,
        reasonLabel: REPORT_REASON_LABELS[r.reason] || r.reason,
    }));

    return { reports: formattedReports, isLoading, error };
};

export default useAdminReportsPage;