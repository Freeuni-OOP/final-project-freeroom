const SECTIONS = [
  {
    title: 'რას ვიყენებთ',
    body: 'როდესაც თქვენი საუნივერსიტეტო Google ანგარიშით შედიხართ სისტემაში, ჩვენ ვიღებთ თქვენს სახელს, ელ.ფოსტის მისამართსა და პროფილის ფოტოს Google-ისგან. ჩვენ მათ ვიყენებთ ავტორიზაციისთვის, თქვენი საუნივერსიტეტო ელ.ფოსტის ნამდვილობის დასადასტურებლად და აპლიკაციაში თქვენი პროფილის საჩვენებლად.',
  },
  {
    title: 'წვდომა',
    body: 'სისტემაში შესვლა შეუძლიათ მხოლოდ იმ პირებს, რომლებსაც აქვთ freeuni.edu.ge ან agruni.edu.ge ელ.ფოსტის მისამართი.',
  },
  {
    title: 'რას არ ვაკეთებთ',
    body: 'ჩვენ არ ვყიდით თქვენს ინფორმაციას და არ ვუზიარებთ მას რეკლამის განმთავსებლებს.',
  },
];

const CONTACT_EMAIL = 'freeuni.freeroom@gmail.com';

const usePrivacyPage = () => {
  return { sections: SECTIONS, contactEmail: CONTACT_EMAIL };
};

export default usePrivacyPage;
