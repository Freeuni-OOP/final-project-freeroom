import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage, NotFoundPage } from '@/pages';
import { ProtectedRoute } from '@/components';

function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route element={<ProtectedRoute />}>
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/floors" element={<FloorsPage />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

export default App;