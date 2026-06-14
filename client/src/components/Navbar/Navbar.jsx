import freeuniLogo from '@/assets/freeuni-logo.png';
import agruniLogo from '@/assets/agruni-logo.png';
import useNavbar from './useNavbar';

export default function Navbar() {
    const { navLinks, currentPath, goTo, handleLogout, isMenuOpen, toggleMenu, closeMenu } = useNavbar();

    return (
        <>
            <header className="sticky top-0 z-20 border-b border-black/5 bg-brand-bg/90 backdrop-blur">
                <nav className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3 sm:px-6 relative z-30">
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

                    {/* Desktop menu */}
                    <div className="hidden sm:flex items-center gap-1 sm:gap-2">
                        {navLinks.map((link) => (
                            <button
                                key={link.path}
                                onClick={() => goTo(link.path)}
                                className={`rounded-lg px-3 py-2 text-sm font-semibold transition-colors ${
                                    currentPath === link.path
                                        ? 'bg-brand-accent/10 text-brand-accent-text'
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
                            გამოსვლა
                        </button>
                    </div>

                    {/* Mobile Burger Icon */}
                    <button 
                        className="flex sm:hidden flex-col justify-center items-center w-8 h-8 space-y-1.5 focus:outline-none"
                        onClick={toggleMenu}
                        aria-label="Toggle Menu"
                    >
                        <span className={`block w-6 h-0.5 bg-brand-ink transition-transform duration-300 ${isMenuOpen ? 'rotate-45 translate-y-2' : ''}`} />
                        <span className={`block w-6 h-0.5 bg-brand-ink transition-opacity duration-300 ${isMenuOpen ? 'opacity-0' : 'opacity-100'}`} />
                        <span className={`block w-6 h-0.5 bg-brand-ink transition-transform duration-300 ${isMenuOpen ? '-rotate-45 -translate-y-2' : ''}`} />
                    </button>
                </nav>
            </header>

            {/* Mobile Menu Overlay */}
            <div 
                className={`sm:hidden fixed inset-0 z-50 ${
                    isMenuOpen ? 'pointer-events-auto' : 'pointer-events-none'
                }`}
            >
                {/* Backdrop */}
                <div 
                    className={`absolute inset-0 cursor-pointer transition-all duration-300 ${
                        isMenuOpen ? 'bg-black/20 backdrop-blur-sm opacity-100' : 'bg-black/0 backdrop-blur-none opacity-0'
                    }`} 
                    onClick={closeMenu}
                />
                
                {/* Sliding Menu */}
                <div 
                    className={`absolute top-0 right-0 h-full w-[60%] bg-brand-bg shadow-xl border-l border-black/5 p-4 flex flex-col gap-2 transform transition-transform duration-300 ${
                        isMenuOpen ? 'translate-x-0' : 'translate-x-full'
                    }`}
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="flex justify-end mb-2">
                        <button 
                            className="flex justify-center items-center w-8 h-8 focus:outline-none text-brand-ink"
                            onClick={closeMenu}
                            aria-label="Close Menu"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    {navLinks.map((link) => (
                        <button
                            key={link.path}
                            onClick={() => goTo(link.path)}
                            className={`rounded-lg px-4 py-3 text-left text-base font-semibold transition-colors ${
                                currentPath === link.path
                                    ? 'bg-brand-accent/10 text-brand-accent-text'
                                    : 'text-brand-ink/70 hover:bg-black/5 hover:text-brand-ink'
                            }`}
                        >
                            {link.label}
                        </button>
                    ))}
                    <div className="mt-auto pt-4 border-t border-black/5">
                        <button
                            onClick={handleLogout}
                            className="w-full rounded-lg px-4 py-3 text-left text-base font-semibold text-brand-ink/70 transition-colors hover:bg-red-50 hover:text-red-600"
                        >
                            გამოსვლა
                        </button>
                    </div>
                </div>
            </div>
        </>
    );
}