import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage } from '@/pages';

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/profile" element={<ProfilePage />} />
      <Route path="/floors" element={<FloorsPage />} />
    </Routes>
  );
}

export default App;
