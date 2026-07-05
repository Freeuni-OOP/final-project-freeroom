import { Routes, Route } from 'react-router-dom';
import { ProtectedRoute, Layout } from '@/components';
import { NotificationProvider } from '@/context/NotificationContext';
import {
    LandingPage,
    ProfilePage,
    PublicProfilePage,
    FloorsPage,
    NotFoundPage,
    PrivacyPage,
    SubjectsPage,
    CalendarPage,
    AdminReportsPage
} from '@/pages';

function App() {
    return (
        <NotificationProvider>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route element={<ProtectedRoute />}>
                    <Route element={<Layout />}>
                        <Route path="/floors" element={<FloorsPage />} />
                        <Route path="/profile" element={<ProfilePage />} />
                        <Route path="/profile/:userId" element={<PublicProfilePage />} />
                        <Route path="/admin/reports" element={<AdminReportsPage />} />
                        <Route path="/privacy" element={<PrivacyPage />} />
                        <Route path="/subjects" element={<SubjectsPage />} />
                        <Route path="/calendar" element={<CalendarPage />} />
                    </Route>
                </Route>
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </NotificationProvider>
    );
}

export default App;