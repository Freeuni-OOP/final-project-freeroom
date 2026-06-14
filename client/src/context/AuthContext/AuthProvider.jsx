import { useEffect } from 'react';
import { AuthContext } from './authContext';
import { useAuthContext } from './useAuthContext';

export const AuthProvider = ({ children }) => {
  const value = useAuthContext();
  const { user } = value;

  useEffect(() => {
    if (typeof document !== 'undefined') {
      const root = document.documentElement;
      if (user?.email?.endsWith('@agruni.edu.ge')) {
        root.classList.add('theme-agruni');
        root.classList.remove('theme-freeuni');
      } else {
        root.classList.add('theme-freeuni');
        root.classList.remove('theme-agruni');
      }
    }
  }, [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
