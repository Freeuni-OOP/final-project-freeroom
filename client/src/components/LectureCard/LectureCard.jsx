import React from 'react';

export default function LectureCard({ lecture, getLectureCardData }) {
    const { startTime, endTime, isHappeningNow } = getLectureCardData(lecture);

    return (
        <div className={`flex gap-4 p-5 rounded-2xl transition-all ${
            isHappeningNow 
                ? 'bg-white border-2 border-brand-accent shadow-[0_4px_20px_var(--brand-accent-glow)]' 
                : 'bg-white border border-gray-100 shadow-sm hover:border-brand-accent/30 hover:shadow-md'
        }`}>
            {/* Time Column */}
            <div className="shrink-0 w-20 flex flex-col items-center justify-center border-r border-gray-100 pr-4">
                <span className={`text-lg font-bold ${isHappeningNow ? 'text-brand-accent-text' : 'text-gray-900'}`}>
                    {startTime}
                </span>
                <span className="text-xs font-semibold text-gray-400 mt-1">
                    {endTime}
                </span>
            </div>
            
            {/* Details Column */}
            <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2 mb-1">
                    <h3 className="font-bold text-gray-900 text-lg leading-tight truncate">
                        {lecture.title}
                    </h3>
                    {isHappeningNow && (
                        <span className="shrink-0 flex items-center gap-1.5 text-xs font-bold text-brand-accent-text bg-brand-accent/10 px-2 py-1 rounded-md animate-pulse">
                            <span className="w-1.5 h-1.5 rounded-full bg-brand-accent"></span>
                            ახლა
                        </span>
                    )}
                </div>
                
                <div className="flex flex-wrap items-center gap-x-4 gap-y-2 mt-2 text-sm text-gray-600">
                    {lecture.roomNumber && (
                        <div className="flex items-center gap-1.5">
                            <div className="p-1 rounded-md bg-gray-100 text-gray-500">
                                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                                </svg>
                            </div>
                            <span className="font-medium text-gray-800">აუდ. {lecture.roomNumber}</span>
                        </div>
                    )}
                    
                    {lecture.organizer && (
                        <div className="flex items-center gap-1.5">
                            <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                            <span>{lecture.organizer}</span>
                        </div>
                    )}
                    
                    {(lecture.type || lecture.groupNumber) && (
                        <div className="flex flex-wrap gap-2">
                            {lecture.type && (
                                <span className="text-gray-500">{lecture.type}</span>
                            )}
                            {lecture.groupNumber && (
                                <span className="bg-gray-100 px-2 py-0.5 rounded text-xs font-medium text-gray-700">
                                    {lecture.groupNumber}
                                </span>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
