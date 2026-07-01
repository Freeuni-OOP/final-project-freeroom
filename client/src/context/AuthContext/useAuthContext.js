import { useEffect, useMemo, useState } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { auth, isAllowedEmail, logout } from '@/services/firebase';
import { getProfile } from '@/services/api/endpoints';

export const useAuthContext = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    return onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser && isAllowedEmail(firebaseUser.email)) {
        setUser(firebaseUser);
        try {
          const res = await getProfile();
          setIsAdmin(Boolean(res.data.isAdmin));
        } catch (err) {
          setIsAdmin(false);
          console.error(err);
        }
      } else {
        setUser(null);
        setIsAdmin(false);
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
      isAdmin,
      loading,
      isAuthenticated: Boolean(user),
      logout,
    }),
    [user, isAdmin, loading]
  );
};
