// Copyright 2022 Catena-X
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import Login from './pages/Login';
import { useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { ProtectedRoute, AuthRoute } from './modules/ProtectedRoutes';
import Dashboard from './pages/Dashboard';

import { useLocalStorage } from './modules/LocalStorage';

declare global {
  interface Window {
    _env_?: any;
  }
}

function App() {
  const [isAuth, setIsAuth] = useLocalStorage('auth', false);
  const [isAuthError, setIsAuthError] = useState(false);
  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={<Login setIsAuth={setIsAuth} setIsAuthError={setIsAuthError} isAuthError={isAuthError} />}
        />
        <Route path="/" element={<AuthRoute isAuth={isAuth} />}></Route>
        <Route element={<ProtectedRoute isAuth={isAuth} />}>
          <Route path="/dashboard" element={<Dashboard />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
