import { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/context';
import { fetchProfile, updateProfile } from '@/services/api/userService';
import { getNotificationPreference, updateNotificationPreference, generateTelegramLink } from '@/services/api/endpoints';
import { useNotification } from '@/context';
import { getUniversity, NOTIFICATION_PREFERENCE } from '@/utils';

const getInitial = (name, email) => {
  const source = name?.trim() || email?.trim() || '';
  return source ? source[0].toUpperCase() : '?';
};

const useProfilePage = () => {
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const [photoFailed, setPhotoFailed] = useState(false);
  const [preference, setPreference] = useState(NOTIFICATION_PREFERENCE.NONE);
  const [telegramLinked, setTelegramLinked] = useState(false);
  const [preferenceLoading, setPreferenceLoading] = useState(true);
  const preferenceTimer = useRef(null);

  const [bio, setBio] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [photoUrl, setPhotoUrl] = useState('');
  const [activeRoomNumber, setActiveRoomNumber] = useState(null);

  const [selectedFile, setSelectedFile] = useState(null);

  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const fetchedUserIdRef = useRef(null);

  useEffect(() => {
    let isMounted = true;

    getNotificationPreference()
        .then(res => {
          if (isMounted) {
            setPreference(res.data.preference);
            setTelegramLinked(res.data.telegramLinked);
          }
        })
        .catch((err) => console.error(err))
        .finally(() => {
          if (isMounted) setPreferenceLoading(false);
        });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    return () => {
      if (preferenceTimer.current) {
        clearTimeout(preferenceTimer.current);
      }
    };
  }, []);

  const handlePreferenceChange = (newPreference) => {
    const previousPreference = preference;
    const previousTelegramLinked = telegramLinked;
    setPreference(newPreference);
    if (newPreference !== NOTIFICATION_PREFERENCE.TELEGRAM) {
      setTelegramLinked(false);
    }
    if (preferenceTimer.current) {
      clearTimeout(preferenceTimer.current);
    }
    preferenceTimer.current = setTimeout(() => {
      updateNotificationPreference(newPreference)
          .then(res => {
            setPreference(res.data.preference);
            setTelegramLinked(res.data.telegramLinked);
          })
          .catch(() => {
            setPreference(previousPreference);
            setTelegramLinked(previousTelegramLinked);
          });
    }, 400);
  };

  const handleTelegramLink = () => {
    generateTelegramLink()
        .then(res => {
          window.open(res.data.deepLink, '_blank', 'noopener,noreferrer');
        });
  };

  const email = user?.email || '';
  const university = getUniversity(email);
  const fallbackName = university ? `${university}-ს სტუდენტი` : 'სტუდენტი';

  const resolvedDisplayName = displayName || fallbackName;
  const showPhoto = Boolean(photoUrl) && !photoFailed;
  const initial = getInitial(resolvedDisplayName, email);

  const handlePhotoError = () => setPhotoFailed(true);

  useEffect(() => {
    if (!user) return;
    let isMounted = true;

    const getBackendProfile = async () => {
      if (fetchedUserIdRef.current === user.uid) {
        if (isMounted) setIsLoading(false);
        return;
      }
      try {
        const data = await fetchProfile();
        if (data && isMounted) {
          setBio(data.bio || '');
          setDisplayName(data.displayName || '');
          setPhotoUrl(data.photoUrl || '');
          setActiveRoomNumber(data.activeRoomNumber || null);
          fetchedUserIdRef.current = user.uid;
        }
      } catch (err) {
        console.error("Error fetching profile:", err);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };
    getBackendProfile();

    return () => {
      isMounted = false;
    };
  }, [user]);

  const handleSaveProfile = async () => {
    if (!user) return;
    if (bio && bio.length > 300) {
      showNotification({ message: 'ბიოგრაფია არ უნდა აღემატებოდეს 300 სიმბოლოს.', type: 'error' });
      return;
    }
    setIsSaving(true);

    try {
      const formData = new FormData();
      if (bio) formData.append('bio', bio);
      if (displayName) formData.append('displayName', displayName);
      if (selectedFile) formData.append('file', selectedFile);

      const data = await updateProfile(formData);

      if (data) {
        setBio(data.bio || '');
        setDisplayName(data.displayName || '');
        setPhotoUrl(data.photoUrl || '');
        setActiveRoomNumber(data.activeRoomNumber || null);
        setSelectedFile(null);
        showNotification({ message: 'პროფილი წარმატებით განახლდა!', type: 'success' });
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      if (error.response?.status === 429) {
        showNotification({ message: 'ზედმეტად ბევრი მოთხოვნა გამოგზავნეთ. გთხოვთ სცადოთ 1 წუთში.', type: 'error' });
      } else {
        showNotification({ message: 'პროფილის განახლება ვერ მოხერხდა.', type: 'error' });
      }
    } finally {
      setIsSaving(false);
    }
  };

  const handleFileChange = (file) => {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      showNotification({ message: 'გთხოვთ აირჩიოთ მხოლოდ სურათის ფაილები!', type: 'error' });
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      showNotification({ message: 'სურათის ზომა არ უნდა აღემატებოდეს 5 MB-ს.', type: 'error' });
      return;
    }

    if (photoUrl && photoUrl.startsWith('blob:')) {
      URL.revokeObjectURL(photoUrl);
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
    activeRoomNumber,
    isSaving,
    isUploading: isSaving && !!selectedFile,
    isLoading,
    handleFileUpload: handleFileChange,
    handleSaveProfile,
    preference,
    telegramLinked,
    preferenceLoading,
    handlePreferenceChange,
    handleTelegramLink
  };
};

export default useProfilePage;