import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { rutaInicio } from './rutas';
import { Cargando } from '../components/ui/Feedback';

export default function RedirectInicio() {
  const { usuario, cargando } = useAuth();
  if (cargando) return <Cargando />;
  return <Navigate to={usuario ? rutaInicio(usuario) : '/login'} replace />;
}
