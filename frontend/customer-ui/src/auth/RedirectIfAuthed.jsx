import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

export default function RedirectIfAuthed({ children, to = '/dashboard' }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <Navigate to={to} replace /> : children;
}
