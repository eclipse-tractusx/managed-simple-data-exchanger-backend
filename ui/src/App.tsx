import Login from './pages/login';
import { useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoutes';
import Dashboard from './pages/Dashboard';
declare global {
  interface Window {
    _env_?: any;
  }
}

function App() {
  const [isAuth, setIsAuth] = useState(false);
  const [isAuthError, setIsAuthError] = useState(false);
  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={<Login setIsAuth={setIsAuth} setIsAuthError={setIsAuthError} isAuthError={isAuthError} />}
        />
        <Route element={<ProtectedRoute isAuth={isAuth} />}>
          <Route path="/dashboard" element={<Dashboard />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
