import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginWithGoogle } from '@/services/firebase';
import { useAuth } from '@/context';

const useLandingPage = () => {
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading && isAuthenticated) {
      navigate('/floors', { replace: true });
    }
  }, [isAuthenticated, loading, navigate]);

  const handleGoogleLogin = async () => {
    try {
      setErrorMsg('');
      setIsLoading(true);
      const user = await loginWithGoogle();
      console.log('Logged in as:', user.email);
      navigate('/floors');
    } catch (error) {
      console.error('Login failed:', error);
      setErrorMsg(error.message);
    } finally {
      setIsLoading(false);
    }
  };

  return { errorMsg, isLoading, handleGoogleLogin };
};

export default useLandingPage;
