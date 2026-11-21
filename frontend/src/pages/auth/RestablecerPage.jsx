import { Link } from 'react-router-dom';
import AuthShell from './AuthShell';

export default function RestablecerPage() {
  return (
    <AuthShell>
      <h1>Restablecer contraseña</h1>
      <p className="muted">Esta pantalla se completa en la HU-06.</p>
      <Link className="enlace" to="/login">Volver a iniciar sesión</Link>
    </AuthShell>
  );
}
