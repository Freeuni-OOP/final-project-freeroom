import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage } from '@/pages';

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/profile" element={<ProfilePage />} />
    </Routes>
  );
}

export default App;
