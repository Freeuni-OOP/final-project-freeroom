import useNotFoundPage from './useNotFoundPage';

function NotFoundPage() {
    const { goHome } = useNotFoundPage();

    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-brand-bg px-6">
            <div className="flex w-full max-w-md flex-col items-center rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-black/5 sm:p-10">
                <div className="relative mb-8 h-28 w-28">
                    <div className="absolute inset-0 rounded-lg border-2 border-gray-200" />
                    <div className="absolute inset-2 grid grid-cols-3 grid-rows-3 gap-1.5">
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm border-2 border-dashed border-brand-green/50 bg-brand-green/10" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                    </div>
                </div>

                <p className="mb-2 text-sm font-medium tracking-wide text-brand-accent-text">გვერდი ვერ მოიძებნა</p>
                <h1 className="mb-3 text-2xl font-bold text-brand-ink">ეს ოთახი რუკაზე არ არის</h1>
                <p className="mb-8 leading-relaxed text-brand-ink/55">
                    გვერდი, რომელსაც ეძებთ, არ არსებობს ან გადაადგილდა. მოდით, დაგაბრუნებთ ნაცნობ ადგილას.
                </p>

                <button
                    onClick={goHome}
                    className="w-full rounded-xl bg-brand-accent py-3 px-8 font-semibold text-brand-accent-contrast transition-colors hover:bg-brand-accent-dark focus:outline-none focus:ring-2 focus:ring-brand-accent focus:ring-offset-2"
                >
                    მთავარ გვერდზე დაბრუნება
                </button>
            </div>
        </div>
    );
}

export default NotFoundPage;