const SECTIONS = [
  {
    title: 'What we use',
    body: 'When you sign in with your university Google account, we receive your name, email address, and profile photo from Google. We use these to sign you in, confirm you have a valid university email, and show your profile inside the app.',
  },
  {
    title: 'Access',
    body: 'Only people with a freeuni.edu.ge or agruni.edu.ge email address can sign in.',
  },
  {
    title: 'What we do not do',
    body: 'We do not sell your information or share it with advertisers.',
  },
];

const CONTACT_EMAIL = 'freeuni.freeroom@gmail.com';

const usePrivacyPage = () => {
  return { sections: SECTIONS, contactEmail: CONTACT_EMAIL };
};

export default usePrivacyPage;
