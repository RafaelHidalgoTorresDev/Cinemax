import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isAdmin, isUser, loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === 'ADMINISTRADOR' && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === 'USUARIO' && !isUser && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}
