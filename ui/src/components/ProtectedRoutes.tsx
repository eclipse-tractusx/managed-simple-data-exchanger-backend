import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

interface Props {
  isAuth: boolean;
}

const ProtectedRoute = ({ isAuth }: Props) => {
  return isAuth ? <Outlet /> : <Navigate to="/login" />;
};

export default ProtectedRoute;
