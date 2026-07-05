import useAdminReportsPage from './useAdminReportsPage';

export default function AdminReportsPage() {
    const { reports, isLoading, error } = useAdminReportsPage();

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            <h1 className="mb-6 text-2xl font-bold text-brand-ink">რეპორტები</h1>

            {isLoading && <p className="text-sm text-brand-ink/50">იტვირთება...</p>}

            {error && <p className="text-sm text-red-500">{error}</p>}

            {!isLoading && !error && reports.length === 0 && (
                <p className="text-sm text-brand-ink/50">რეპორტები არ არის.</p>
            )}

            {!isLoading && reports.length > 0 && (
                <div className="flex flex-col gap-3">
                    {reports.map((report) => (
                        <div
                            key={report.id}
                            className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-black/5"
                        >
                            <div className="flex items-center justify-between">
                                <span className="rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
                                    {report.reasonLabel}
                                </span>
                                <span className="text-xs text-brand-ink/40">
                                    {new Date(report.createdAt).toLocaleString('ka-GE')}
                                </span>
                            </div>

                            <div className="mt-3 grid grid-cols-2 gap-4 text-sm">
                                <div>
                                    <p className="text-xs font-semibold text-brand-ink/40">დარეპორტებული</p>
                                    <p className="font-medium text-brand-ink">{report.reportedUserDisplayName}</p>
                                </div>
                                <div>
                                    <p className="text-xs font-semibold text-brand-ink/40">დამრეპორტებელი</p>
                                    <p className="font-medium text-brand-ink">{report.reporterUserDisplayName}</p>
                                </div>
                            </div>

                            {report.details && (
                                <p className="mt-3 text-sm text-brand-ink/70">{report.details}</p>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}