import React from 'react';
import useSubjectsPage from './useSubjectsPage';
import { SubjectCard } from '@/components';

export default function SubjectsPage() {
    const {
        savedSubjectsList,
        filteredSubjects,
        savedSubjectIds,
        searchQuery,
        setSearchQuery,
        loading,
        toggleSubject,
        isSavedExpanded,
        setIsSavedExpanded
    } = useSubjectsPage();

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[60vh]">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-brand-accent"></div>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-8">საგნები</h1>

            {/* YOUR SUBJECTS Section */}
            <div className="mb-10 bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <button 
                    onClick={() => setIsSavedExpanded(!isSavedExpanded)}
                    className="w-full flex items-center justify-between p-5 bg-gray-50 hover:bg-gray-100 transition-colors"
                >
                    <div className="flex items-center gap-3">
                        <h2 className="text-lg font-semibold text-gray-800">შენი საგნები</h2>
                        <span className="bg-brand-accent/10 text-brand-accent-text text-xs font-bold px-2.5 py-1 rounded-full">
                            {savedSubjectsList.length}
                        </span>
                    </div>
                    <svg 
                        className={`w-5 h-5 text-gray-500 transition-transform duration-200 ${isSavedExpanded ? 'rotate-180' : ''}`} 
                        fill="none" viewBox="0 0 24 24" stroke="currentColor"
                    >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                </button>
                
                {isSavedExpanded && (
                    <div className="p-5 border-t border-gray-100">
                        {savedSubjectsList.length === 0 ? (
                            <p className="text-gray-500 text-center py-4">ჯერ არცერთი საგანი არ გაქვთ დამატებული.</p>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                {savedSubjectsList.map(subject => (
                                    <SubjectCard 
                                        key={subject.id} 
                                        subject={subject} 
                                        isSaved={true} 
                                        onToggle={() => toggleSubject(subject)} 
                                    />
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* ALL SUBJECTS Section */}
            <div className="mb-6">
                <h2 className="text-xl font-semibold text-gray-800 mb-4">ყველა საგანი</h2>
                <div className="relative">
                    <input
                        type="text"
                        placeholder="ძიება (საგნის სახელი, ლექტორი, ჯგუფი)..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-12 pr-4 py-4 rounded-xl border border-gray-200 focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20 outline-none transition-all"
                    />
                    <svg className="w-6 h-6 text-gray-400 absolute left-4 top-1/2 -translate-y-1/2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {filteredSubjects.map(subject => (
                    <SubjectCard 
                        key={subject.id} 
                        subject={subject} 
                        isSaved={savedSubjectIds.has(subject.id)} 
                        onToggle={() => toggleSubject(subject)} 
                    />
                ))}
                {filteredSubjects.length === 0 && (
                    <div className="col-span-full text-center py-12 text-gray-500">
                        საგნები ვერ მოიძებნა
                    </div>
                )}
            </div>
        </div>
    );
}
