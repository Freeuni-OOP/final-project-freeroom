import NotFoundPage from '@/pages/NotFoundPage';
import usePublicProfilePage from './usePublicProfilePage.js';
import {UNIVERSITY} from "@/utils";

export default function PublicProfilePage() {
    const {
        profile,
        isLoading,
        notFound,
        showPhoto,
        initial,
        handlePhotoError,
        actionPending,
        canRequest,
        isPendingSent,
        isPendingReceived,
        isFriends,
        handleSendRequest,
        handleAccept,
        handleReject,
        handleUnfriend,
        handleCancelRequest,
        university,
    } = usePublicProfilePage();

    if (notFound) {
        return <NotFoundPage />;
    }

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            <section className="flex flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                {isLoading ? (
                    <div className="h-24 w-24 rounded-full bg-black/5 animate-pulse ring-4 ring-black/5" />
                ) : showPhoto ? (
                    <img
                        src={profile.photoUrl}
                        alt={profile.displayName}
                        onError={handlePhotoError}
                        referrerPolicy="no-referrer"
                        className="h-24 w-24 rounded-full object-cover ring-4 ring-brand-accent/15"
                    />
                ) : (
                    <div className="flex h-24 w-24 items-center justify-center rounded-full bg-brand-accent/10 text-3xl font-bold text-brand-accent-text ring-4 ring-brand-accent/15">
                        {initial}
                    </div>
                )}

                {isLoading ? (
                    <div className="mt-5 flex flex-col items-center gap-2 w-full animate-pulse">
                        <div className="h-7 w-48 rounded-lg bg-black/5" />
                        <div className="mt-3 h-9 w-32 rounded-xl bg-black/5" />
                    </div>
                ) : (
                    <>
                        <h1 className="mt-5 text-2xl font-bold text-brand-ink">{profile.displayName}</h1>

                        <div className="mt-4">
                            {isFriends && (
                                <div className="flex flex-col items-center gap-3">
                                    <span className="inline-flex items-center rounded-full bg-brand-green/10 px-4 py-2 text-sm font-semibold text-brand-green">
                                        მეგობარი
                                    </span>
                                    <button
                                        onClick={handleUnfriend}
                                        disabled={actionPending}
                                        className="rounded-xl bg-red-50 px-6 py-2 text-sm font-semibold text-red-600 transition hover:bg-red-100 disabled:opacity-50"
                                    >
                                        {actionPending ? 'მიმდინარეობს...' : 'მეგობრობის გაუქმება'}
                                    </button>
                                </div>
                            )}

                            {canRequest && (
                                <button
                                    onClick={handleSendRequest}
                                    disabled={actionPending}
                                    className={`rounded-xl px-6 py-2 text-sm font-semibold transition disabled:opacity-50 ${
                                        university === UNIVERSITY.FREEUNI
                                            ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                                            : university === UNIVERSITY.AGRUNI
                                                ? 'bg-green-600 hover:bg-green-700 text-white'
                                                : 'bg-brand-ink hover:bg-brand-ink/90 text-white'
                                    }`}
                                >
                                    {actionPending ? 'იგზავნება...' : 'მეგობრობის მოთხოვნა'}
                                </button>
                            )}

                            {isPendingSent && (
                                <div className="flex flex-col items-center gap-3">
                                    <span className="inline-flex items-center rounded-full bg-black/5 px-4 py-2 text-sm font-semibold text-brand-ink/50">
                                        მეგობრობის მოთხოვნა გაგზავნილია
                                    </span>
                                    <button
                                        onClick={handleCancelRequest}
                                        disabled={actionPending}
                                        className="rounded-xl bg-red-50 px-6 py-2 text-sm font-semibold text-red-600 transition hover:bg-red-100 disabled:opacity-50"
                                    >
                                        {actionPending ? 'მიმდინარეობს...' : 'მოთხოვნის გაუქმება'}
                                    </button>
                                </div>
                            )}

                            {isPendingReceived && (
                                <div className="flex gap-3">
                                    <button
                                        onClick={handleAccept}
                                        disabled={actionPending}
                                        className={`rounded-xl px-6 py-2 text-sm font-semibold transition disabled:opacity-50 ${
                                            university === UNIVERSITY.FREEUNI
                                                ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                                                : university === UNIVERSITY.AGRUNI
                                                    ? 'bg-green-600 hover:bg-green-700 text-white'
                                                    : 'bg-brand-ink hover:bg-brand-ink/90 text-white'
                                        }`}
                                    >
                                        დადასტურება
                                    </button>
                                    <button
                                        onClick={handleReject}
                                        disabled={actionPending}
                                        className="rounded-xl border border-black/10 px-6 py-2 text-sm font-semibold text-brand-ink/60 transition hover:border-brand-accent/40 disabled:opacity-50"
                                    >
                                        უარყოფა
                                    </button>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </section>

            {!isLoading && profile?.bio && (
                <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                    <h2 className="mb-4 text-lg font-bold text-brand-ink">ჩემს შესახებ</h2>
                    <p className="whitespace-pre-wrap text-sm text-brand-ink/70">{profile.bio}</p>
                </section>
            )}
        </div>
    );
}