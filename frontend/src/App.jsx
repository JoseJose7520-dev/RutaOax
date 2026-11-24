import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import RedirectInicio from './auth/RedirectInicio';
import RoleRoute from './auth/RoleRoute';
import AppLayout from './components/layout/AppLayout';
import PaginaPendiente from './pages/PaginaPendiente';
import UsuariosPage from './pages/admin/UsuariosPage';
import EquipoPage from './pages/negocio/EquipoPage';
import PanelPage from './pages/negocio/PanelPage';
import PedidosPage from './pages/negocio/PedidosPage';
import VehiculosPage from './pages/negocio/VehiculosPage';
import MisPedidosPage from './pages/cliente/MisPedidosPage';
import DisponiblesPage from './pages/independiente/DisponiblesPage';
import RutaMapaPage from './pages/independiente/RutaMapaPage';
import MensajesPage from './pages/mensajes/MensajesPage';
import PerfilPage from './pages/perfil/PerfilPage';
import HistorialPage from './pages/repartidor/HistorialPage';
import ParadaPage from './pages/repartidor/ParadaPage';
import RutaHoyPage from './pages/repartidor/RutaHoyPage';
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
            <Route path="/negocio" element={<PanelPage />} />
            <Route path="/negocio/pedidos" element={<PedidosPage />} />
            <Route path="/negocio/equipo" element={<EquipoPage />} />
            <Route path="/negocio/vehiculos" element={<VehiculosPage />} />
            <Route path="/negocio/mensajes" element={<MensajesPage />} />
          </Route>
          <Route element={<RoleRoute roles={['REPARTIDOR']} tipoRepartidor="EMPRESA" />}>
            <Route path="/repartidor" element={<RutaHoyPage />} />
            <Route path="/repartidor/parada/:pedidoId" element={<ParadaPage />} />
            <Route path="/repartidor/historial" element={<HistorialPage />} />
          </Route>
          <Route element={<RoleRoute roles={['REPARTIDOR']} tipoRepartidor="INDEPENDIENTE" />}>
            <Route path="/independiente" element={<DisponiblesPage />} />
            <Route path="/independiente/ruta" element={<RutaMapaPage />} />
            <Route path="/independiente/parada/:pedidoId" element={<ParadaPage base="/independiente/ruta" paradaBase="/independiente/parada" />} />
            <Route path="/independiente/mensajes" element={<MensajesPage morado />} />
          </Route>
          <Route element={<RoleRoute roles={['CLIENTE']} />}>
            <Route path="/cliente" element={<MisPedidosPage />} />
            <Route path="/cliente/mensajes" element={<MensajesPage />} />
          </Route>
          <Route element={<RoleRoute roles={['ADMIN']} />}>
            <Route path="/admin/usuarios" element={<UsuariosPage />} />
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
