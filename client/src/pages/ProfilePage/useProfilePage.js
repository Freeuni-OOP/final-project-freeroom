import { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/context';
import { getProfile, updateProfile } from '@/services';

const UNIVERSITY_BY_DOMAIN = {
  '@freeuni.edu.ge': 'თავისუფალი',
  '@agruni.edu.ge': 'აგრარული',
};

const getUniversity = (email) => {
  const normalized = email?.toLowerCase() || '';
  const match = Object.entries(UNIVERSITY_BY_DOMAIN).find(([domain]) => normalized.endsWith(domain));
  return match ? match[1] : null;
};

const getInitial = (name, email) => {
  const source = name?.trim() || email?.trim() || '';
  return source ? source[0].toUpperCase() : '?';
};

const useProfilePage = () => {
  const { user } = useAuth();
  const [photoFailed, setPhotoFailed] = useState(false);

  const [bio, setBio] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [photoUrl, setPhotoUrl] = useState('');

  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const hasFetched = useRef(false);

  const email = user?.email || '';
  const university = getUniversity(email);
  const fallbackName = university ? `${university}-ს სტუდენტი` : 'სტუდენტი';

  const currentDisplayName = isLoading ? fallbackName : (displayName || fallbackName);
  const currentPhotoUrl = isLoading ? '' : photoUrl;
  const showPhoto = Boolean(currentPhotoUrl) && !photoFailed;
  const initial = getInitial(currentDisplayName, email);

  const handlePhotoError = () => setPhotoFailed(true);

  useEffect(() => {
    const fetchBackendProfile = async () => {
      if (!user || hasFetched.current) return;
      try {
        const response = await getProfile();
        if (response && response.data) {
          setBio(response.data.bio || '');
          setDisplayName(response.data.displayName || '');
          setPhotoUrl(response.data.photoUrl || '');
          hasFetched.current = true;
        }
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchBackendProfile();
  }, [user]);

  const handleSaveProfile = async () => {
    if (!user) return;
    setIsSaving(true);
    try {
      const response = await updateProfile({ bio, displayName, photoUrl });
      if (response && response.data) {
        alert('პროფილი წარმატებით განახლდა!');
      }
    } catch (error) {
      console.error(error);
      alert('პროფილის განახლება ვერ მოხერხდა.');
    } finally {
      setIsSaving(false);
    }
  };

  return {
    displayName: currentDisplayName,
    setDisplayName,
    photoUrl: currentPhotoUrl,
    setPhotoUrl,
    email,
    university,
    showPhoto,
    initial,
    handlePhotoError,
    bio,
    setBio,
    isSaving,
    isLoading,
    handleSaveProfile
  };
};

export default useProfilePage;