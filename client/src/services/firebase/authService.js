import { GoogleAuthProvider, signInWithPopup, signOut } from 'firebase/auth';
import { auth } from './firebaseConfig';
import {syncUser} from '../api/endpoints';

const ALLOWED_DOMAINS = ['@freeuni.edu.ge', '@agruni.edu.ge'];

export const isAllowedEmail = (email) => {
  const normalized = email?.toLowerCase();
  return Boolean(normalized && ALLOWED_DOMAINS.some((domain) => normalized.endsWith(domain)));
};

export const loginWithGoogle = async () => {
  const provider = new GoogleAuthProvider();
  const result = await signInWithPopup(auth, provider);

  if (isAllowedEmail(result.user.email)) {
    await syncUser();
    return result.user;
  }

  await signOut(auth).catch(() => {});
  throw new Error('Access restricted: Please log in using a valid FreeUni or Agruni institutional email account.');
};

export const logout = () => signOut(auth);