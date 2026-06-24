import useProfilePage from './useProfilePage';

export default function ProfilePage() {
    const { displayName, email, university, showPhoto, photoUrl, initial, handlePhotoError, preference, telegramLinked, preferenceLoading, handlePreferenceChange, handleTelegramLink } = useProfilePage();

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            <section className="flex flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                 {showPhoto ? (
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

                <h1 className="mt-5 text-2xl font-bold text-brand-ink">{displayName}</h1>
                {email && <p className="mt-1 text-sm text-brand-ink/55">{email}</p>}
                {university && (
                    <span className="mt-3 inline-flex items-center rounded-full bg-brand-accent/10 px-3 py-1 text-xs font-semibold text-brand-accent-text">
            {university}
          </span>
                )}
            </section>

            <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                <h2 className="mb-4 text-lg font-bold text-brand-ink">შეტყობინებები</h2>
                {preferenceLoading ? (
                    <div className="text-sm text-brand-ink/50">იტვირთება...</div>
                ) : (
                    <div className="flex flex-col gap-4">
                        <div className="flex gap-3">
                            <button
                                onClick={() => handlePreferenceChange('NONE')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === 'NONE' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                არცერთი
                            </button>
                            <button
                                onClick={() => handlePreferenceChange('EMAIL')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === 'EMAIL' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                ელ. ფოსტა
                            </button>
                            <button
                                onClick={() => handlePreferenceChange('TELEGRAM')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === 'TELEGRAM' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                Telegram
                            </button>
                        </div>
                        {preference === 'TELEGRAM' && (
                            <div className="rounded-xl border border-black/10 bg-brand-bg p-4">
                                <p className="mb-3 text-sm font-semibold text-brand-ink">Telegram-ის დაყენება</p>
                                <ol className="flex flex-col gap-2 text-sm text-brand-ink/70">
                                    <li>1. დააჭირეთ ღილაკს და გახსენით ჩვენი ბოტი</li>
                                    <li>2. დააჭირეთ Start Telegram-ში</li>
                                    <li>3. დაბრუნდით და გვერდი განახლდება</li>
                                </ol>
                                <div className="mt-3 flex items-center gap-3">
                                    <button
                                        onClick={handleTelegramLink}
                                        className="rounded-xl bg-brand-accent px-4 py-2 text-sm font-semibold text-brand-ink transition-colors hover:bg-brand-accent-dark"
                                    >
                                        ბოტის გახსნა
                                    </button>
                                    {telegramLinked && (
                                        <span className="text-sm font-semibold text-brand-green">დაკავშირებულია</span>
                                    )}
                                    {!telegramLinked && (
                                        <span className="text-sm text-brand-ink/40">არ არის დაკავშირებული</span>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </section>

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