import { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/context';
import axiosInstance from '@/services/api/axiosInstance';

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
  const [isUploading, setIsUploading] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const hasFetched = useRef(false);

  const email = user?.email || '';
  const university = getUniversity(email);
  const fallbackName = university ? `${university}-ს სტუდენტი` : 'სტუდენტი';

  const resolvedDisplayName = displayName || fallbackName;
  const showPhoto = Boolean(photoUrl) && !photoFailed;
  const initial = getInitial(resolvedDisplayName, email);

  const handlePhotoError = () => setPhotoFailed(true);

  useEffect(() => {
    const fetchBackendProfile = async () => {
      if (!user) return;
      if (hasFetched.current) {
        setIsLoading(false);
        return;
      }
      try {
        const response = await axiosInstance.get('/user');
        if (response && response.data) {
          setBio(response.data.bio || '');
          setDisplayName(response.data.displayName || '');
          setPhotoUrl(response.data.photoUrl || '');
          hasFetched.current = true;
        }
      } catch (err) {
        console.error("Error fetching profile:", err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchBackendProfile();
  }, [user]);

  const handleFileUpload = async (file) => {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('გთხოვთ აირჩიოთ მხოლოდ სურათის ფაილები!');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      alert('სურათის ზომა არ უნდა აღემატებოდეს 5 MB-ს.');
      return;
    }

    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await axiosInstance.post('/user/upload-avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      if (response && response.data && response.data.publicUrl) {
        setPhotoUrl(response.data.publicUrl);
        setPhotoFailed(false);
      }
    } catch (error) {
      console.error('File delivery sequence interrupted:', error);
      alert('ფაილის ატვირთვა ვერ მოხერხდა.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleSaveProfile = async () => {
    if (!user) return;
    setIsSaving(true);
    try {
      const response = await axiosInstance.patch('/user', { bio, displayName, photoUrl });
      if (response && response.data) {
        alert('პროფილი წარმატებით განახლდა!');
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      alert('პროფილის განახლება ვერ მოხერხდა.');
    } finally {
      setIsSaving(false);
    }
  };

  return {
    displayName,
    setDisplayName,
    photoUrl,
    setPhotoUrl,
    resolvedDisplayName,
    email,
    university,
    showPhoto,
    initial,
    handlePhotoError,
    bio,
    setBio,
    isSaving,
    isUploading,
    isLoading,
    handleFileUpload,
    handleSaveProfile
  };
};

export default useProfilePage;