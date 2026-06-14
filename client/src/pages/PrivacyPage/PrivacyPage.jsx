import usePrivacyPage from './usePrivacyPage';

export default function PrivacyPage() {
  const { sections, contactEmail } = usePrivacyPage();

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-10 sm:px-6">
      <section className="rounded-2xl bg-white p-8 shadow-sm ring-1 ring-black/5 sm:p-10">
        <h1 className="text-2xl font-bold text-brand-ink">Privacy</h1>
        <p className="mt-3 text-sm leading-relaxed text-brand-ink/60">
          FreeRoom is a study room finder for students of Free University of Tbilisi and the
          Agricultural University of Georgia.
        </p>

        <div className="mt-8 space-y-6">
          {sections.map((section) => (
            <div key={section.title}>
              <h2 className="text-sm font-bold uppercase tracking-wide text-brand-accent-text">
                {section.title}
              </h2>
              <p className="mt-2 text-sm leading-relaxed text-brand-ink/70">{section.body}</p>
            </div>
          ))}
        </div>

        <p className="mt-8 border-t border-black/5 pt-6 text-sm leading-relaxed text-brand-ink/55">
          FreeRoom is a student project built for a university course. For questions, contact us at{' '}
          <a
            href={`mailto:${contactEmail}`}
            className="font-semibold text-brand-accent-text transition-colors hover:text-brand-accent-dark"
          >
            {contactEmail}
          </a>
          .
        </p>
      </section>
    </div>
  );
}
