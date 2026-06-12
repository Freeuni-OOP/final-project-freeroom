import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage } from '@/pages';
import { ProtectedRoute } from '@/components';

function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route element={<ProtectedRoute />}>
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/floors" element={<FloorsPage />} />
            </Route>
        </Routes>
    );
}

export default App;