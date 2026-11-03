
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import useAuth from './useAuth';

function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location }}
      />
    );
  }

  if (
    allowedRoles?.length &&
    !allowedRoles.includes(
      String(user?.role ?? '').toUpperCase(),
    )
  ) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default ProtectedRoute;
