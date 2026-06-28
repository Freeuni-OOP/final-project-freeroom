

export default function SubjectCard({ subject, isSaved, onToggle }) {
    return (
        <div 
            onClick={onToggle}
            className={`p-4 rounded-xl border-2 transition-all cursor-pointer flex items-start gap-4 ${
                isSaved 
                    ? 'border-brand-accent bg-brand-accent/5' 
                    : 'border-transparent bg-white hover:border-gray-200 shadow-sm'
            }`}
        >
            <div className={`mt-1 shrink-0 w-6 h-6 rounded-md flex items-center justify-center border-2 transition-colors ${
                isSaved 
                    ? 'bg-brand-accent border-brand-accent text-white' 
                    : 'border-gray-300 bg-white text-transparent'
            }`}>
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                </svg>
            </div>
            
            <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-gray-900 leading-tight mb-1 truncate">{subject.title}</h3>
                <div className="flex flex-wrap gap-x-3 gap-y-1 text-sm text-gray-600">
                    {subject.lecturer && (
                        <span className="flex items-center gap-1">
                            <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                            {subject.lecturer}
                        </span>
                    )}
                    {(subject.type || subject.groupNumber) && (
                        <span className="text-gray-400">•</span>
                    )}
                    {subject.type && <span>{subject.type}</span>}
                    {subject.groupNumber && (
                        <span className="bg-gray-100 px-2 py-0.5 rounded text-xs font-medium text-gray-700">
                            {subject.groupNumber}
                        </span>
                    )}
                </div>
            </div>
        </div>
    );
}
