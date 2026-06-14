import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage, NotFoundPage } from '@/pages';
import { ProtectedRoute, Layout } from '@/components';

function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route element={<ProtectedRoute />}>
                <Route element={<Layout />}>
                    <Route path="/floors" element={<FloorsPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                </Route>
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

export default App;