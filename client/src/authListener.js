import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './services/firebase/firebaseConfig.js';

onAuthStateChanged(auth, async (user) => {
    if (user) {
        const token = await user.getIdToken();
        localStorage.setItem('token', token);
    } else {
        localStorage.removeItem('token');
    }
});