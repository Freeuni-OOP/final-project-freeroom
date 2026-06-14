const DEVELOPERS = [
  { name: 'ალადა', url: 'https://www.linkedin.com/in/luka-aladashvili/' },
  { name: 'დანელა', url: 'https://www.linkedin.com/in/nickdanelia/' },
  { name: 'ზაბახა', url: 'https://www.linkedin.com/in/giorgi-zabakhidze-210522370/' },
  { name: 'კალა', url: 'https://www.linkedin.com/in/nika-kalandadze-3a8231329/' },
  { name: 'ჩაფო', url: 'https://www.linkedin.com/in/nikoloz-chapidze-723418404/' },
];

const CONTACT_EMAIL = 'freeuni.freeroom@gmail.com';

const useFooter = () => {
  const year = new Date().getFullYear();
  return { developers: DEVELOPERS, contactEmail: CONTACT_EMAIL, year };
};

export default useFooter;
