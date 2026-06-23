import useProfilePage from './useProfilePage';

export default function ProfilePage() {
    const {
        displayName,
        setDisplayName,
        photoUrl,
        setPhotoUrl,
        email,
        university,
        showPhoto,
        initial,
        handlePhotoError,
        bio,
        setBio,
        isSaving,
        isLoading,
        handleSaveProfile
    } = useProfilePage();

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            {/* Identity Card */}
            <section className="flex flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                {isLoading ? (
                    <div className="h-24 w-24 rounded-full bg-black/5 animate-pulse ring-4 ring-black/5" />
                ) : showPhoto ? (
                    <img
                        src={photoUrl}
                        alt={displayName}
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
                        <div className="h-4 w-36 rounded-md bg-black/5" />
                        <div className="mt-3 h-6 w-28 rounded-full bg-black/5" />
                    </div>
                ) : (
                    <>
                        <h1 className="mt-5 text-2xl font-bold text-brand-ink">{displayName}</h1>
                        {email && <p className="mt-1 text-sm text-brand-ink/55">{email}</p>}
                        {university && (
                            <span className="mt-3 inline-flex items-center rounded-full bg-brand-accent/10 px-3 py-1 text-xs font-semibold text-brand-accent-text">
                                {university}
                            </span>
                        )}
                    </>
                )}
            </section>

            {/* Profile Settings Section */}
            <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                <h2 className="text-lg font-bold text-brand-ink mb-6">პროფილის რედაქტირება</h2>

                {isLoading ? (
                    <div className="animate-pulse text-sm text-brand-ink/40 italic py-3">
                        ინფორმაცია იტვირთება...
                    </div>
                ) : (
                    <div className="flex flex-col gap-5">
                        <div className="flex flex-col gap-2 w-full">
                            <label className="text-sm font-semibold text-brand-ink/70">სახელი და გვარი</label>
                            <input
                                type="text"
                                value={displayName}
                                onChange={(e) => setDisplayName(e.target.value)}
                                className="w-full rounded-xl border border-black/10 p-3 text-sm text-brand-ink focus:border-brand-accent focus:outline-none focus:ring-1 focus:ring-brand-accent"
                                placeholder="შეიყვანეთ სახელი და გვარი"
                            />
                        </div>

                        <div className="flex flex-col gap-2 w-full">
                            <label className="text-sm font-semibold text-brand-ink/70">პროფილის სურათის URL</label>
                            <input
                                type="text"
                                value={photoUrl}
                                onChange={(e) => setPhotoUrl(e.target.value)}
                                className="w-full rounded-xl border border-black/10 p-3 text-sm text-brand-ink focus:border-brand-accent focus:outline-none focus:ring-1 focus:ring-brand-accent"
                                placeholder="https://example.com/image.png"
                            />
                        </div>

                        <div className="flex flex-col gap-2 w-full">
                            <label className="text-sm font-semibold text-brand-ink/70">ჩემს შესახებ</label>
                            <textarea
                                value={bio}
                                onChange={(e) => setBio(e.target.value)}
                                placeholder="მოყევი რამე შენს შესახებ..."
                                maxLength={300}
                                rows={3}
                                className="w-full rounded-xl border border-black/10 p-3 text-sm text-brand-ink focus:border-brand-accent focus:outline-none focus:ring-1 focus:ring-brand-accent resize-none"
                            />
                        </div>

                        <div className="flex justify-end mt-2">
                            <button
                                onClick={handleSaveProfile}
                                disabled={isSaving}
                                className="rounded-xl bg-brand-ink px-6 py-2 text-sm font-semibold text-white transition hover:bg-brand-ink/90 disabled:opacity-50"
                            >
                                {isSaving ? 'ინახება...' : 'ცვლილებების შენახვა'}
                            </button>
                        </div>
                    </div>
                )}
            </section>

            {/* Friends Card */}
            <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-lg font-bold text-brand-ink">მეგობრები</h2>
                    <span className="rounded-full bg-brand-accent/15 px-3 py-1 text-xs font-semibold text-brand-accent-text">
                        მალე დაემატება
                    </span>
                </div>
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-black/10 py-10 text-center">
                    <p className="text-sm font-medium text-brand-ink/60">მეგობრები ჯერ არ გყავთ</p>
                    <p className="mt-1 text-xs text-brand-ink/40">
                        დაუკავშირდით ჯგუფელებს, რათა ნახოთ ვინ სწავლობს ახლომახლო.
                    </p>
                </div>
            </section>
        </div>
    );
}