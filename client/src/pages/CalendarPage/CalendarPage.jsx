
import useCalendarPage from './useCalendarPage';
import { LectureCard, Loader } from '@/components';

export default function CalendarPage() {
    const { loading, groupedLectures, isEmpty, formatDateHeading, getLectureCardData, goToSubjects } = useCalendarPage();

    if (loading) {
        return <Loader />;
    }

    return (
        <div className="max-w-3xl mx-auto px-4 py-8">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
                <h1 className="text-3xl font-bold text-gray-900">კალენდარი</h1>
                <button
                    onClick={goToSubjects}
                    className="inline-flex items-center justify-center px-4 py-2 bg-brand-accent/10 text-brand-accent-text hover:bg-brand-accent hover:text-brand-accent-contrast rounded-xl font-semibold transition-colors"
                >
                    <svg className="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                    საგნების რედაქტირება
                </button>
            </div>

            {isEmpty ? (
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
                    <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
                        <svg className="w-10 h-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                    <h2 className="text-xl font-bold text-gray-800 mb-2">ცარიელი კალენდარი</h2>
                    <p className="text-gray-500 mb-6 max-w-md mx-auto">
                        თქვენ ჯერ არ დაგიმატებიათ საგნები. დაამატეთ საგნები, რათა იხილოთ თქვენი პერსონალური განრიგი.
                    </p>
                    <button
                        onClick={goToSubjects}
                        className="inline-flex items-center justify-center px-6 py-3 bg-brand-accent text-brand-accent-contrast hover:bg-brand-accent-dark rounded-xl font-bold transition-colors shadow-sm"
                    >
                        საგნების დამატება
                    </button>
                </div>
            ) : (
                <div className="space-y-10">
                    {groupedLectures.map(group => (
                        <div key={group.dateStr} className="relative">
                            <div className="sticky top-0 z-10 bg-brand-bg/90 backdrop-blur-sm py-2 mb-4">
                                <h2 className="text-lg font-bold text-gray-800 capitalize">
                                    {formatDateHeading(group.dateObj)}
                                </h2>
                            </div>

                            <div className="space-y-4">
                                {group.lectures.length === 0 ? (
                                    <div className="py-4 text-center text-gray-400 bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
                                        ამ დღეს ლექციები არ გაქვთ
                                    </div>
                                ) : (
                                    group.lectures.map((lec, idx) => (
                                        <LectureCard
                                            key={`${lec.title}-${lec.startAt}-${idx}`}
                                            lecture={lec}
                                            getLectureCardData={getLectureCardData}
                                        />
                                    ))
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}