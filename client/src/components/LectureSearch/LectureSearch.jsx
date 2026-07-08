import useLectureSearch from './useLectureSearch';
import { UNIVERSITY } from '@/utils';

export default function LectureSearch() {
    const { query, searchResults, loading, handleSearch, formatTime, university } = useLectureSearch();

    return (
        <div
            className="p-5 bg-white rounded-lg shadow-md text-gray-800"
            onClick={(e) => e.stopPropagation()}
        >
            <h3 className="mb-4 text-xl font-semibold text-slate-800">ძებნა</h3>

            <input
                type="text"
                placeholder="ჩაწერეთ საგნის დასახელება..."
                value={query}
                onClick={(e) => e.stopPropagation()}
                onChange={(e) => handleSearch(e.target.value)}
                className="w-full p-3 border border-gray-300 rounded-md text-sm outline-none mb-4 focus:border-brand-accent"
            />

            <div className="max-h-[300px] overflow-y-auto">
                {loading && <p className="text-gray-500">იტვირთება...</p>}
                {searchResults.length === 0 && query && !loading && (
                    <p className="text-gray-400 text-center">ლექციები ვერ მოიძებნა</p>
                )}

                {searchResults.map((lecture) => (
                    <div key={lecture.id} className="p-3 border-b border-gray-100 mb-2 bg-gray-50 rounded">
                        <div className="flex justify-between items-center mb-1">
                            <span className={`text-white px-2 py-0.5 rounded text-xs font-bold ${
                                university === UNIVERSITY.AGRUNI ? 'bg-green-600' : 'bg-amber-400'
                            }`}>
                                {formatTime(lecture.startAt)} - {formatTime(lecture.endAt)}
                            </span>
                        </div>
                        <h4 className="m-0 text-slate-800 font-medium text-base">{lecture.subject?.title}</h4>
                        <div className="text-xs text-gray-500 mt-1 flex flex-wrap gap-2">
                            <span>ოთახი: <strong className="text-blue-600">{lecture.room?.roomNumber}</strong></span>
                            {lecture.subject?.type && <span className="text-gray-400">• {lecture.subject.type}</span>}
                            {lecture.subject?.groupNumber && <span className="bg-gray-200 px-1.5 py-0.5 rounded text-[10px] font-bold">{lecture.subject.groupNumber}</span>}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}