import { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/context';
import { fetchProfile, updateProfile } from '@/services/api/userService';

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

  const [selectedFile, setSelectedFile] = useState(null);

  const [isSaving, setIsSaving] = useState(false);
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
    const getBackendProfile = async () => {
      if (!user) return;
      if (hasFetched.current) {
        setIsLoading(false);
        return;
      }
      try {
        const data = await fetchProfile(); // Cleaner call
        if (data) {
          setBio(data.bio || '');
          setDisplayName(data.displayName || '');
          setPhotoUrl(data.photoUrl || '');
          hasFetched.current = true;
        }
      } catch (err) {
        console.error("Error fetching profile:", err);
      } finally {
        setIsLoading(false);
      }
    };
    getBackendProfile();
  }, [user]);

  const handleSaveProfile = async () => {
    if (!user) return;
    setIsSaving(true);

    try {
      const formData = new FormData();
      if (bio) formData.append('bio', bio);
      if (displayName) formData.append('displayName', displayName);
      if (selectedFile) formData.append('file', selectedFile);

      const data = await updateProfile(formData); // Cleaner call

      if (data) {
        setBio(data.bio || '');
        setDisplayName(data.displayName || '');
        setPhotoUrl(data.photoUrl || '');
        setSelectedFile(null);
        alert('პროფილი წარმატებით განახლდა!');
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      alert('პროფილის განახლება ვერ მოხერხდა.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleFileChange = (file) => {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('გთხოვთ აირჩიოთ მხოლოდ სურათის ფაილები!');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      alert('სურათის ზომა არ უნდა აღემატებოდეს 5 MB-ს.');
      return;
    }

    setSelectedFile(file);
    setPhotoFailed(false);

    setPhotoUrl(URL.createObjectURL(file));
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
    isUploading: isSaving && !!selectedFile,
    isLoading,
    handleFileUpload: handleFileChange,
    handleSaveProfile
  };
};

export default useProfilePage;