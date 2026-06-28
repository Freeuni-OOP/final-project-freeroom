
import freeuniLogo from '@/assets/freeuni-logo.png';
import agruniLogo from '@/assets/agruni-logo.png';

export default function Loader({ fullScreen = false }) {
    const containerClasses = fullScreen
        ? "fixed inset-0 z-50 flex items-center justify-center bg-brand-bg"
        : "flex items-center justify-center min-h-[60vh] w-full";

    return (
        <div className={containerClasses}>
            <div className="flex flex-col items-center gap-6">
                <div className="flex items-center gap-4 relative">
                    <div className="absolute inset-0 bg-brand-accent/20 blur-xl rounded-full animate-pulse"></div>

                    <img
                        src={freeuniLogo}
                        alt="Freeuni Logo"
                        className="h-16 w-16 object-contain drop-shadow-md animate-bounce"
                        style={{ animationDelay: '0ms', animationDuration: '1.5s' }}
                    />
                    <img
                        src={agruniLogo}
                        alt="Agruni Logo"
                        className="h-16 w-16 object-contain drop-shadow-md animate-bounce"
                        style={{ animationDelay: '150ms', animationDuration: '1.5s' }}
                    />
                </div>

                <div className="flex flex-col items-center gap-2">
                    <span className="text-2xl font-bold tracking-tight text-brand-ink">
                        FreeRoom
                    </span>
                    <div className="flex gap-1.5">
                        <div className="w-2.5 h-2.5 rounded-full bg-brand-accent animate-bounce" style={{ animationDelay: '0ms' }}></div>
                        <div className="w-2.5 h-2.5 rounded-full bg-brand-accent animate-bounce" style={{ animationDelay: '150ms' }}></div>
                        <div className="w-2.5 h-2.5 rounded-full bg-brand-accent animate-bounce" style={{ animationDelay: '300ms' }}></div>
                    </div>
                </div>
            </div>
        </div>
    );
}
