import freeuniLogo from '@/assets/freeuni-logo.png';
import agruniLogo from '@/assets/agruni-logo.png';
import useLandingPage from './useLandingPage';

export default function LandingPage() {
    const { errorMsg, isLoading, handleGoogleLogin } = useLandingPage();

    return (
        <div className="relative flex min-h-screen flex-col bg-brand-bg">
            <div
                aria-hidden="true"
                className="pointer-events-none absolute inset-0"
                style={{
                    background:
                        'radial-gradient(60% 55% at 50% 30%, rgba(26,122,76,0.10) 0%, rgba(26,122,76,0) 70%)',
                }}
            />

            <header className="relative z-10 flex items-center justify-between px-6 py-5 sm:px-10">
                <span className="text-lg font-bold tracking-tight text-brand-ink">FreeRoom</span>
                <div className="flex items-center gap-2.5">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white shadow-sm ring-1 ring-black/5">
                        <img src={freeuniLogo} alt="Free University of Tbilisi" className="h-7 w-7 object-contain" />
                    </div>
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white shadow-sm ring-1 ring-black/5">
                        <img src={agruniLogo} alt="Agricultural University of Georgia" className="h-7 w-7 object-contain" />
                    </div>
                </div>
            </header>

            <main className="relative z-10 flex flex-1 flex-col items-center justify-center px-5 pb-16">
                <span className="mb-5 inline-flex items-center rounded-full bg-brand-accent/15 px-3.5 py-1.5 text-xs font-semibold tracking-wide text-brand-accent-text">
                    კამპუსის ოთახები
                </span>

                <h1 className="mb-4 text-center text-6xl font-bold tracking-tight text-brand-ink sm:text-7xl">
                    FreeRoom
                </h1>
                <p className="mb-10 max-w-md text-center text-lg leading-relaxed text-brand-ink/60">
                    იპოვე თავისუფალი ოთახი და დაჯავშნე
                </p>

                <div className="w-full max-w-sm">
                    {errorMsg && (
                        <div className="mb-5 w-full rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-center text-sm text-red-700">
                            {errorMsg}
                        </div>
                    )}

                    <button
                        onClick={handleGoogleLogin}
                        disabled={isLoading}
                        className="flex w-full items-center justify-center rounded-xl bg-brand-accent py-4 text-base font-semibold text-brand-accent-contrast shadow-sm transition-colors hover:bg-brand-accent-dark focus:outline-none focus:ring-2 focus:ring-brand-accent focus:ring-offset-2 focus:ring-offset-brand-bg disabled:opacity-60"
                    >
                        {isLoading ? 'სისტემაში შესვლა…' : 'Google-ით გაგრძელება'}
                    </button>

                    <p className="mt-4 text-center text-xs text-brand-ink/45">
                        გამოიყენეთ თქვენი @freeuni.edu.ge ან @agruni.edu.ge ელ.ფოსტა.
                    </p>
                </div>
            </main>
        </div>
    );
}