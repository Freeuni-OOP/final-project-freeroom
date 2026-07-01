import useReportModal from './useReportModal';
import { REPORT_REASON, REPORT_REASON_LABELS } from '@/utils';

export default function ReportModal({ userId, onClose }) {
    const {
        selectedReason,
        details,
        setDetails,
        isSubmitting,
        isSubmitted,
        error,
        handleSelectReason,
        handleSubmitOther,
    } = useReportModal(userId);

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden relative cursor-default"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 transition-colors cursor-pointer"
                >
                    ✕
                </button>

                <div className="p-6">
                    {isSubmitted ? (
                        <div className="py-6 text-center">
                            <p className="mb-2 text-lg font-bold text-brand-ink">მადლობა შეტყობინებისთვის</p>
                            <p className="text-sm text-brand-ink/60">ჩვენი გუნდი განიხილავს თქვენს მოხსენებას.</p>
                        </div>
                    ) : (
                        <>
                            <h2 className="mb-4 text-lg font-bold text-brand-ink">მომხმარებლის დარეპორტება</h2>

                            <div className="flex flex-col gap-2">
                                {Object.values(REPORT_REASON)
                                    .filter((reason) => reason !== REPORT_REASON.OTHER)
                                    .map((reason) => (
                                        <button
                                            key={reason}
                                            onClick={() => handleSelectReason(reason)}
                                            disabled={isSubmitting}
                                            className={`rounded-xl border px-4 py-3 text-left text-sm font-medium transition-colors disabled:opacity-50 ${
                                                selectedReason === reason
                                                    ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text'
                                                    : 'border-black/10 text-brand-ink/70 hover:border-brand-accent/40'
                                            }`}
                                        >
                                            {REPORT_REASON_LABELS[reason]}
                                        </button>
                                    ))}
                                <button
                                    onClick={() => handleSelectReason(REPORT_REASON.OTHER)}
                                    disabled={isSubmitting}
                                    className={`rounded-xl border px-4 py-3 text-left text-sm font-medium transition-colors disabled:opacity-50 ${
                                        selectedReason === REPORT_REASON.OTHER
                                            ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text'
                                            : 'border-black/10 text-brand-ink/70 hover:border-brand-accent/40'
                                    }`}
                                >
                                    {REPORT_REASON_LABELS[REPORT_REASON.OTHER]}
                                </button>
                            </div>

                            {selectedReason === REPORT_REASON.OTHER && (
                                <>
                                    <textarea
                                        value={details}
                                        onChange={(e) => setDetails(e.target.value)}
                                        placeholder="დაწერეთ დეტალები..."
                                        rows={3}
                                        className="mt-3 w-full resize-none rounded-xl border border-black/10 p-3 text-sm text-brand-ink focus:border-brand-accent focus:outline-none focus:ring-1 focus:ring-brand-accent"
                                    />
                                    <button
                                        onClick={handleSubmitOther}
                                        disabled={isSubmitting || !details.trim()}
                                        className="mt-3 w-full rounded-xl bg-red-500 py-3 font-semibold text-white transition-colors hover:bg-red-600 disabled:opacity-50"
                                    >
                                        {isSubmitting ? 'იგზავნება...' : 'გაგზავნა'}
                                    </button>
                                </>
                            )}

                            {error && <p className="mt-3 text-xs text-red-500">{error}</p>}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}