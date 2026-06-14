import freeuniLogo from '@/assets/freeuni-logo.png';
import agruniLogo from '@/assets/agruni-logo.png';
import useNavbar from './useNavbar';

export default function Navbar() {
    const { navLinks, currentPath, goTo, handleLogout } = useNavbar();

    return (
        <header className="sticky top-0 z-20 border-b border-black/5 bg-brand-bg/90 backdrop-blur">
            <nav className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3 sm:px-6">
                <button
                    onClick={() => goTo('/floors')}
                    className="flex items-center gap-2 text-lg font-bold tracking-tight text-brand-ink"
                >
          <span className="flex items-center gap-1">
            <img src={freeuniLogo} alt="" className="h-6 w-6 object-contain" />
            <img src={agruniLogo} alt="" className="h-6 w-6 object-contain" />
          </span>
                    FreeRoom
                </button>

                <div className="flex items-center gap-1 sm:gap-2">
                    {navLinks.map((link) => (
                        <button
                            key={link.path}
                            onClick={() => goTo(link.path)}
                            className={`rounded-lg px-3 py-2 text-sm font-semibold transition-colors ${
                                currentPath === link.path
                                    ? 'bg-brand-green/10 text-brand-green'
                                    : 'text-brand-ink/70 hover:bg-black/5 hover:text-brand-ink'
                            }`}
                        >
                            {link.label}
                        </button>
                    ))}

                    <button
                        onClick={handleLogout}
                        className="ml-1 rounded-lg px-3 py-2 text-sm font-semibold text-brand-ink/70 transition-colors hover:bg-red-50 hover:text-red-600 focus:outline-none focus:ring-2 focus:ring-red-300"
                    >
                        Log out
                    </button>
                </div>
            </nav>
        </header>
    );
}