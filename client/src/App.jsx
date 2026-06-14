import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage, NotFoundPage, PrivacyPage } from '@/pages';
import { ProtectedRoute, Layout } from '@/components';

function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route element={<ProtectedRoute />}>
                <Route element={<Layout />}>
                    <Route path="/floors" element={<FloorsPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/privacy" element={<PrivacyPage />} />
                </Route>
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

export default App;