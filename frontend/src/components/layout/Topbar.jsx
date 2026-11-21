import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { fechaLarga, iniciales } from '../../utils/format';

export default function Topbar() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [abierto, setAbierto] = useState(false);
  const [saliendo, setSaliendo] = useState(false);
  const contenedor = useRef(null);

  useEffect(() => {
    const alHacerClic = (e) => {
      if (contenedor.current && !contenedor.current.contains(e.target)) setAbierto(false);
    };
    document.addEventListener('mousedown', alHacerClic);
    return () => document.removeEventListener('mousedown', alHacerClic);
  }, []);

  const cerrarSesion = async () => {
    setSaliendo(true);
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className="top">
      <span className="muted">{fechaLarga()}</span>
      <div className="menu-usuario" ref={contenedor}>
        <button type="button" className="yo" onClick={() => setAbierto((v) => !v)} aria-haspopup="menu" aria-expanded={abierto}>
          <span className="av">
            {usuario?.avatarUrl ? <img src={usuario.avatarUrl} alt="" /> : iniciales(usuario?.nombre)}
          </span>
          {usuario?.nombre}
        </button>
        {abierto ? (
          <div className="lista" role="menu">
            <div className="datos">
              <strong>{usuario?.nombre}</strong>
              <small>{usuario?.email}</small>
            </div>
            <Link to="/perfil" role="menuitem" onClick={() => setAbierto(false)}>
              Mi perfil
            </Link>
            <button type="button" role="menuitem" onClick={cerrarSesion} disabled={saliendo}>
              {saliendo ? 'Cerrando sesión...' : 'Cerrar sesión'}
            </button>
          </div>
        ) : null}
      </div>
    </header>
  );
}
