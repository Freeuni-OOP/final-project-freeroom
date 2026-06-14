const DEVELOPERS = [
  { name: '\u10D0\u10DA\u10D0\u10D3\u10D0', url: 'https://www.linkedin.com/in/luka-aladashvili/' },
  { name: '\u10D3\u10D0\u10DC\u10D4\u10DA\u10D0', url: 'https://www.linkedin.com/in/nickdanelia/' },
  { name: '\u10D6\u10D0\u10D1\u10D0\u10EE\u10D0', url: 'https://www.linkedin.com/in/giorgi-zabakhidze-210522370/' },
  { name: '\u10D9\u10D0\u10DA\u10D0', url: 'https://www.linkedin.com/in/nika-kalandadze-3a8231329/' },
  { name: '\u10E9\u10D0\u10E4\u10DD', url: 'https://www.linkedin.com/in/nikoloz-chapidze-723418404/' },
];

const CONTACT_EMAIL = 'freeuni.freeroom@gmail.com';

const useFooter = () => {
  const year = new Date().getFullYear();
  return { developers: DEVELOPERS, contactEmail: CONTACT_EMAIL, year };
};

export default useFooter;
