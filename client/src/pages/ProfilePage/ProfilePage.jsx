import useProfilePage from './useProfilePage';
import { FriendsPanel } from '@/components';
import { NOTIFICATION_PREFERENCE, UNIVERSITY } from '@/utils';

export default function ProfilePage() {
    const {
        displayName,
        setDisplayName,
        photoUrl,
        setPhotoUrl,
        resolvedDisplayName,
        email,
        university,
        showPhoto,
        initial,
        handlePhotoError,
        bio,
        setBio,
        activeRoomNumber,
        isSaving,
        isUploading,
        isLoading,
        handleFileUpload,
        handleSaveProfile,
        preference,
        telegramLinked,
        preferenceLoading,
        handlePreferenceChange,
        handleTelegramLink,
        occupancyVisibility,
        handleVisibilityChange
    } = useProfilePage();

    return (
        <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
            <section className="flex flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                {isLoading ? (
                    <div className="h-24 w-24 rounded-full bg-black/5 animate-pulse ring-4 ring-black/5" />
                ) : showPhoto ? (
                    <img
                        src={photoUrl}
                        alt={resolvedDisplayName}
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
                        <h1 className="mt-5 text-2xl font-bold text-brand-ink">{resolvedDisplayName}</h1>
                        {email && <p className="mt-1 text-sm text-brand-ink/55">{email}</p>}
                        {university && (
                            <span className="mt-3 inline-flex items-center rounded-full bg-brand-accent/10 px-3 py-1 text-xs font-semibold text-brand-accent-text">
                                {university}
                            </span>
                        )}
                        {activeRoomNumber && (
                            <span className="mt-2 inline-flex items-center rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700 ring-1 ring-inset ring-green-600/20">
                                ოთახი: {activeRoomNumber}
                            </span>
                        )}
                    </>
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
                                onClick={() => handlePreferenceChange(NOTIFICATION_PREFERENCE.NONE)}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === NOTIFICATION_PREFERENCE.NONE ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                არცერთი
                            </button>
                            <button
                                onClick={() => handlePreferenceChange(NOTIFICATION_PREFERENCE.EMAIL)}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === NOTIFICATION_PREFERENCE.EMAIL ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                ელ. ფოსტა
                            </button>
                            <button
                                onClick={() => handlePreferenceChange(NOTIFICATION_PREFERENCE.TELEGRAM)}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${preference === NOTIFICATION_PREFERENCE.TELEGRAM ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                Telegram
                            </button>
                        </div>
                        {preference === NOTIFICATION_PREFERENCE.TELEGRAM && (
                            <div className="rounded-xl border border-black/10 bg-brand-bg p-4">
                                <p className="mb-3 text-sm font-semibold text-brand-ink">Telegram-ის დაყენება</p>
                                <ol className="flex flex-col gap-2 text-sm text-brand-ink/70">
                                    <li>1. დააჭირეთ ღილაკს და გახსენით ჩვენი ბოტი</li>
                                    <li>2. Telegram-ში დააჭირეთ ღილაკს Start</li>
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
                <h2 className="mb-4 text-lg font-bold text-brand-ink">ჯავშნის ხილვადობა</h2>
                <div className="flex flex-col gap-4">
                    <p className="text-sm text-brand-ink/60 mb-2">
                        აირჩიეთ ვის შეეძლება თქვენი დაჯავშნილი ოთახის ნახვა
                    </p>
                    {isLoading ? (
                        <div className="text-sm text-brand-ink/50 animate-pulse py-3">იტვირთება...</div>
                    ) : (
                        <div className="flex gap-3">
                            <button
                                onClick={() => handleVisibilityChange('NONE')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${occupancyVisibility === 'NONE' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                არავინ
                            </button>
                            <button
                                onClick={() => handleVisibilityChange('FRIENDS')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${occupancyVisibility === 'FRIENDS' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                მეგობრები
                            </button>
                            <button
                                onClick={() => handleVisibilityChange('PUBLIC')}
                                className={`flex-1 rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${occupancyVisibility === 'PUBLIC' ? 'border-brand-accent bg-brand-accent/10 text-brand-accent-text' : 'border-black/10 text-brand-ink/60 hover:border-brand-accent/40'}`}
                            >
                                ყველა
                            </button>
                        </div>
                    )}
                </div>
            </section>

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
                            <label className="text-sm font-semibold text-brand-ink/70">პროფილის სურათი</label>

                            <div
                                onDragOver={(e) => e.preventDefault()}
                                onDrop={(e) => {
                                    e.preventDefault();
                                    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                                        handleFileUpload(e.dataTransfer.files[0]);
                                    }
                                }}
                                onClick={() => document.getElementById('avatar-file-input').click()}
                                className="flex flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed border-black/10 p-6 text-center cursor-pointer transition hover:border-brand-accent/50 hover:bg-black/[0.01]"
                            >
                                {isUploading ? (
                                    <p className="text-sm font-medium text-brand-ink/50 animate-pulse">სურათი იტვირთება...</p>
                                ) : (
                                    <>
                                        <p className="text-sm font-medium text-brand-ink/70">
                                            ჩააგდეთ ფაილი აქ ან <span className="text-brand-accent font-semibold underline">აირჩიეთ გალერეიდან</span>
                                        </p>
                                        <p className="text-xs text-brand-ink/40">PNG, JPG, ან WEBP (მაქს. 5MB)</p>
                                    </>
                                )}
                            </div>

                            <input
                                id="avatar-file-input"
                                type="file"
                                accept="image/*"
                                onChange={(e) => {
                                    if (e.target.files && e.target.files[0]) {
                                        handleFileUpload(e.target.files[0]);
                                    }
                                }}
                                className="hidden"
                            />

                            <input
                                type="text"
                                value={photoUrl}
                                onChange={(e) => setPhotoUrl(e.target.value)}
                                className="mt-2 w-full rounded-xl border border-black/10 p-3 text-xs text-brand-ink/50 bg-black/[0.01] focus:outline-none"
                                placeholder="ან ჩასვით სურათის პირდაპირი ბმული (URL) ხელით"
                            />
                        </div>

                        <div className="flex flex-col gap-2 w-full">
                            <div className="flex justify-between items-center">
                                <label className="text-sm font-semibold text-brand-ink/70">ჩემს შესახებ</label>
                                <span className={`text-xs ${bio.length > 300 ? 'text-red-500 font-semibold' : 'text-brand-ink/40'}`}>
                                    {bio.length}/300
                                </span>
                            </div>
                            <textarea
                                value={bio}
                                onChange={(e) => setBio(e.target.value)}
                                placeholder="მოყევი რამე შენს შესახებ..."
                                rows={3}
                                className="w-full rounded-xl border border-black/10 p-3 text-sm text-brand-ink focus:border-brand-accent focus:outline-none focus:ring-1 focus:ring-brand-accent resize-none"
                            />
                        </div>

                        <div className="flex justify-end mt-2">
                            <button
                                onClick={handleSaveProfile}
                                disabled={isSaving || isUploading}
                                className={`rounded-xl px-6 py-2 text-sm font-semibold transition disabled:opacity-50 ${university === UNIVERSITY.FREEUNI
                                    ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                                    : university === UNIVERSITY.AGRUNI
                                        ? 'bg-green-600 hover:bg-green-700 text-white'
                                        : 'bg-brand-ink hover:bg-brand-ink/90 text-white'
                                    }`}
                            >
                                {isSaving ? 'ინახება...' : 'ცვლილებების შენახვა'}
                            </button>
                        </div>
                    </div>
                )}
            </section>

            <section className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-black/5 sm:p-8">
                <h2 className="mb-6 text-lg font-bold text-brand-ink">მეგობრები</h2>
                <FriendsPanel />
            </section>
        </div>
    );
}