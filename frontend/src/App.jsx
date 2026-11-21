import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import RedirectInicio from './auth/RedirectInicio';
import RoleRoute from './auth/RoleRoute';
import AppLayout from './components/layout/AppLayout';
import PaginaPendiente from './pages/PaginaPendiente';
import PerfilPage from './pages/perfil/PerfilPage';
import LoginPage from './pages/auth/LoginPage';
import RecuperarPage from './pages/auth/RecuperarPage';
import RegistroPage from './pages/auth/RegistroPage';
import RestablecerPage from './pages/auth/RestablecerPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RedirectInicio />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/registro" element={<RegistroPage />} />
      <Route path="/recuperar" element={<RecuperarPage />} />
      <Route path="/restablecer" element={<RestablecerPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/perfil" element={<PerfilPage />} />
          <Route element={<RoleRoute roles={['NEGOCIO']} />}>
            <Route path="/negocio" element={<PaginaPendiente titulo="Panel del negocio" />} />
            <Route path="/negocio/pedidos" element={<PaginaPendiente titulo="Pedidos y asignación" />} />
            <Route path="/negocio/equipo" element={<PaginaPendiente titulo="Equipo de reparto" />} />
            <Route path="/negocio/vehiculos" element={<PaginaPendiente titulo="Vehículos" />} />
            <Route path="/negocio/mensajes" element={<PaginaPendiente titulo="Mensajes" />} />
          </Route>
          <Route element={<RoleRoute roles={['REPARTIDOR']} tipoRepartidor="EMPRESA" />}>
            <Route path="/repartidor" element={<PaginaPendiente titulo="Mi ruta de hoy" />} />
            <Route path="/repartidor/parada/:pedidoId" element={<PaginaPendiente titulo="Parada actual" />} />
            <Route path="/repartidor/historial" element={<PaginaPendiente titulo="Historial" />} />
          </Route>
          <Route element={<RoleRoute roles={['REPARTIDOR']} tipoRepartidor="INDEPENDIENTE" />}>
            <Route path="/independiente" element={<PaginaPendiente titulo="Entregas disponibles" />} />
            <Route path="/independiente/ruta" element={<PaginaPendiente titulo="Mi ruta y mapa" />} />
            <Route path="/independiente/mensajes" element={<PaginaPendiente titulo="Mensajes" />} />
          </Route>
          <Route element={<RoleRoute roles={['CLIENTE']} />}>
            <Route path="/cliente" element={<PaginaPendiente titulo="Mi pedido" />} />
            <Route path="/cliente/mensajes" element={<PaginaPendiente titulo="Mensajes" />} />
          </Route>
          <Route element={<RoleRoute roles={['ADMIN']} />}>
            <Route path="/admin/usuarios" element={<PaginaPendiente titulo="Usuarios" />} />
            <Route path="/admin/pedidos" element={<PaginaPendiente titulo="Pedidos" />} />
            <Route path="/admin/vehiculos" element={<PaginaPendiente titulo="Vehículos" />} />
            <Route path="/admin/notificaciones" element={<PaginaPendiente titulo="Notificaciones" />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
