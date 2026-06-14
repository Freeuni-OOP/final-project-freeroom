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

                <p className="mb-2 text-sm font-medium tracking-wide text-brand-green">Page not found</p>
                <h1 className="mb-3 text-2xl font-bold text-brand-ink">This room is not on the map</h1>
                <p className="mb-8 leading-relaxed text-brand-ink/55">
                    The page you want does not exist or may have moved. Let us get you back to familiar ground.
                </p>

                <button
                    onClick={goHome}
                    className="w-full rounded-xl bg-brand-green py-3 px-8 font-semibold text-white transition-colors hover:bg-brand-green-dark focus:outline-none focus:ring-2 focus:ring-brand-green focus:ring-offset-2"
                >
                    Back to FreeRoom
                </button>
            </div>
        </div>
    );
}

export default NotFoundPage;