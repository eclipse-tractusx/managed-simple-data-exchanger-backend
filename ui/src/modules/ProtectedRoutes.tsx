import { Navigate, Outlet } from 'react-router-dom';

interface Props {
  isAuth: boolean;
}

export const ProtectedRoute = ({ isAuth }: Props) => {
  return isAuth ? <Outlet /> : <Navigate to="/" />;
};

export const AuthRoute = ({ isAuth }: Props) => {
  return isAuth ? <Navigate to="/dashboard" /> : <Navigate to="/login" />;
};
