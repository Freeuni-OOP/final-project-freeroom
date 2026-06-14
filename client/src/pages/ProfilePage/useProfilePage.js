import { useState } from 'react';
import { useAuth } from '@/context';

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

  const email = user?.email || '';
  const university = getUniversity(email);
  const fallbackName = university ? `${university}-ს სტუდენტი` : 'სტუდენტი';
  const displayName = user?.displayName?.trim() || fallbackName;

  const photoUrl = user?.photoURL || '';
  const showPhoto = Boolean(photoUrl) && !photoFailed;
  const initial = getInitial(user?.displayName, email);

  const handlePhotoError = () => setPhotoFailed(true);

  return { displayName, email, university, showPhoto, photoUrl, initial, handlePhotoError };
};

export default useProfilePage;