import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginWithGoogle } from '@/services/firebase';

const useLandingPage = () => {
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleGoogleLogin = async () => {
    try {
      setErrorMsg('');
      setIsLoading(true);
      const user = await loginWithGoogle();
      console.log('Logged in as:', user.email);
      navigate('/profile');
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
