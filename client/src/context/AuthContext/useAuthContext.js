import { useEffect, useMemo, useState } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { auth, isAllowedEmail, logout } from '@/services/firebase';

export const useAuthContext = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    return onAuthStateChanged(auth, (firebaseUser) => {
      if (firebaseUser && isAllowedEmail(firebaseUser.email)) {
        setUser(firebaseUser);
      } else {
        setUser(null);
        if (firebaseUser) {
          logout().catch(() => {});
        }
      }
      setLoading(false);
    });
  }, []);

  return useMemo(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      logout,
    }),
    [user, loading]
  );
};
