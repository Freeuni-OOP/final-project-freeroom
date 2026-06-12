import { Routes, Route } from 'react-router-dom';
import { LandingPage, ProfilePage, FloorsPage } from '@/pages';
import { ProtectedRoute } from '@/components';

function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route
                path="/profile"
                element={
                    <ProtectedRoute>
                        <ProfilePage />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/floors"
                element={
                    <ProtectedRoute>
                        <FloorsPage />
                    </ProtectedRoute>
                }
            />
        </Routes>
    );
}

export default App;