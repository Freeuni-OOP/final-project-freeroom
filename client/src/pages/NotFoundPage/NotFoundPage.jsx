import useNotFoundPage from './useNotFoundPage';

function NotFoundPage() {
    const { goHome } = useNotFoundPage();

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 px-6">
            <div className="bg-white rounded-2xl shadow-lg w-full max-w-md p-8 sm:p-10 flex flex-col items-center text-center">
                <div className="relative mb-8 h-28 w-28">
                    <div className="absolute inset-0 rounded-lg border-2 border-gray-200" />
                    <div className="absolute inset-2 grid grid-cols-3 grid-rows-3 gap-1.5">
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm border-2 border-dashed border-blue-300 bg-blue-50" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                        <div className="rounded-sm bg-gray-100" />
                    </div>
                </div>

                <p className="text-sm font-medium tracking-wide text-blue-600 mb-2">Page not found</p>
                <h1 className="text-2xl font-bold text-gray-800 mb-3">This room isn’t on the map</h1>
                <p className="text-gray-500 mb-8 leading-relaxed">
                    The page you’re looking for doesn’t exist or may have moved. Let’s get you back to familiar ground.
                </p>

                <button
                    onClick={goHome}
                    className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-8 rounded-lg w-full transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400 focus:ring-offset-2"
                >
                    Back to FreeRoom
                </button>
            </div>
        </div>
    );
}

export default NotFoundPage;