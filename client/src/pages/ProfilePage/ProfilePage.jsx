import useProfilePage from './useProfilePage';

export default function ProfilePage() {
    const { displayName, email, university, showPhoto, photoUrl, initial, handlePhotoError } = useProfilePage();

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            <section className="flex flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                {showPhoto ? (
                    <img
                        src={photoUrl}
                        alt={displayName}
                        onError={handlePhotoError}
                        referrerPolicy="no-referrer"
                        className="h-24 w-24 rounded-full object-cover ring-4 ring-brand-green/15"
                    />
                ) : (
                    <div className="flex h-24 w-24 items-center justify-center rounded-full bg-brand-green/10 text-3xl font-bold text-brand-green ring-4 ring-brand-green/15">
                        {initial}
                    </div>
                )}

                <h1 className="mt-5 text-2xl font-bold text-brand-ink">{displayName}</h1>
                {email && <p className="mt-1 text-sm text-brand-ink/55">{email}</p>}
                {university && (
                    <span className="mt-3 inline-flex items-center rounded-full bg-brand-green/10 px-3 py-1 text-xs font-semibold text-brand-green">
            {university}
          </span>
                )}
            </section>

            <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-lg font-bold text-brand-ink">Friends</h2>
                    <span className="rounded-full bg-brand-gold/15 px-3 py-1 text-xs font-semibold text-[#8a6d00]">
            Coming soon
          </span>
                </div>
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-black/10 py-10 text-center">
                    <p className="text-sm font-medium text-brand-ink/60">No friends yet</p>
                    <p className="mt-1 text-xs text-brand-ink/40">
                        Connect with classmates to see who is studying nearby.
                    </p>
                </div>
            </section>
        </div>
    );
}