import { Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import NoAutorizado from '../pages/NoAutorizado';

export default function RoleRoute({ roles, tipoRepartidor }) {
  const { usuario } = useAuth();
  const rolOk = roles.some((r) => usuario?.roles?.includes(r));
  const tipoOk = !tipoRepartidor || usuario?.perfil?.tipoRepartidor === tipoRepartidor;
  return rolOk && tipoOk ? <Outlet /> : <NoAutorizado />;
}
