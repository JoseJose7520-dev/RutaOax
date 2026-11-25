import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { esIndependiente, etiquetaRol, menuPara } from './menus';

export default function Sidebar({ abierto = false }) {
  const { usuario } = useAuth();
  const items = menuPara(usuario);
  return (
    <aside id="menu-lateral" className={`side ${esIndependiente(usuario) ? 'morado' : ''} ${abierto ? 'abierto' : ''}`}>
      <div className="marca">
        <img src="/logo.svg" alt="" width="34" height="34" />
        RutaOax
      </div>
      <p className="rol">{etiquetaRol(usuario)}</p>
      {items.map((item) => (
        <NavLink key={item.ruta} to={item.ruta} end className={({ isActive }) => (isActive ? 'on' : '')}>
          {item.texto}
        </NavLink>
      ))}
      <p className="fin">RutaOax · Oaxaca de Juárez</p>
    </aside>
  );
}
