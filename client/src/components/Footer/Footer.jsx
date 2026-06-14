import useFooter from './useFooter';

export default function Footer() {
    const { developers, contactEmail, year } = useFooter();

    return (
        <footer className="border-t border-black/5 bg-brand-bg">
            <div className="mx-auto flex max-w-5xl flex-col items-center gap-4 px-4 py-8 text-center sm:px-6">
                <div className="flex flex-col items-center gap-2">
                    <span className="text-xs font-semibold uppercase tracking-widest text-brand-ink/40">
                        Made by
                    </span>
                    <div className="flex flex-wrap items-center justify-center gap-x-4 gap-y-2">
                        {developers.map((dev) => (
                            <a
                                key={dev.url}
                                href={dev.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="footer-dev text-sm font-semibold text-brand-gold transition-all"
                            >
                                {dev.name}
                            </a>
                        ))}
                    </div>
                </div>

                <a
                    href={`mailto:${contactEmail}`}
                    className="text-sm text-brand-ink/60 transition-colors hover:text-brand-green"
                >
                    {contactEmail}
                </a>

                <p className="text-xs text-brand-ink/40">{'\u00A9'} {year} FreeRoom</p>
            </div>
        </footer>
    );
}