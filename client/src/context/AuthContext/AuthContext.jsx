import { AuthContext } from './authContext';
import { useAuthContext } from './useAuthContext';

export const AuthProvider = ({ children }) => {
  const value = useAuthContext();

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
