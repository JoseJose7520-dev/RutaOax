import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { rutaInicio } from '../auth/rutas';

export default function NoAutorizado() {
  const { usuario } = useAuth();
  return (
    <section className="card pagina-pendiente">
      <h1>No tienes permiso</h1>
      <p className="muted">No tienes permiso para ver esta pantalla.</p>
      <Link className="btn" to={rutaInicio(usuario)}>
        Ir a mi sección
      </Link>
    </section>
  );
}
