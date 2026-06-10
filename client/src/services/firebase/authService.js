import { GoogleAuthProvider, signInWithPopup, signOut } from 'firebase/auth';
import { auth } from './firebaseConfig';

const ALLOWED_DOMAINS = ['@freeuni.edu.ge', '@agruni.edu.ge'];

export const loginWithGoogle = async () => {
  const provider = new GoogleAuthProvider();
  const result = await signInWithPopup(auth, provider);
  const email = result.user.email?.toLowerCase();

  if (email && ALLOWED_DOMAINS.some((domain) => email.endsWith(domain))) {
    return result.user;
  }

  await signOut(auth).catch(() => {});
  throw new Error('Access restricted: Please log in using a valid FreeUni or Agruni institutional email account.');
};
