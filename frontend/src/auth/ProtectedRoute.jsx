import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Cargando } from '../components/ui/Feedback';

export default function ProtectedRoute() {
  const { usuario, cargando } = useAuth();
  if (cargando) return <Cargando />;
  if (!usuario) return <Navigate to="/login" replace />;
  return <Outlet />;
}
