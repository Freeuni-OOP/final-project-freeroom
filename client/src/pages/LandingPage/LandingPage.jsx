import useLandingPage from './useLandingPage';

export default function LandingPage() {
  const { errorMsg, isLoading, handleGoogleLogin } = useLandingPage();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white p-10 rounded-xl shadow-lg flex flex-col items-center max-w-md w-full">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">FreeRoom</h1>
        <p className="text-gray-500 mb-8 text-center">Sign in to book and manage your university study rooms.</p>

        {errorMsg && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-6 w-full text-sm text-center">
            {errorMsg}
          </div>
        )}

        <button
          onClick={handleGoogleLogin}
          disabled={isLoading}
          className="flex items-center justify-center gap-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg w-full transition-colors disabled:bg-blue-400"
        >
          {isLoading ? 'Signing in...' : 'Sign in with Google'}
        </button>

        <p className="text-xs text-gray-400 mt-6 text-center">
          *Requires a valid @freeuni.edu.ge or @agruni.edu.ge email address.
        </p>
      </div>
    </div>
  );
}
