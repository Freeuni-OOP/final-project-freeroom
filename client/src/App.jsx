import { Routes, Route, Link } from 'react-router-dom';
import Landing from "./Landing"
import Profile from "./Profile"

function App() {
  return (
    <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/profile" element={<Profile />} />
    </Routes>
  )
}

export default App
