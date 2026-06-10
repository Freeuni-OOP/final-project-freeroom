import useProfilePage from './useProfilePage';

function ProfilePage() {
  useProfilePage();

  return (
    <div>
      <h1 className="text-blue-300 text-2xl text-center">Hello from FreeRoom Profile</h1>
    </div>
  );
}

export default ProfilePage;
