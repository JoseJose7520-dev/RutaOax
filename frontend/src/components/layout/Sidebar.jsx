import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { esIndependiente, etiquetaRol, menuPara } from './menus';

export default function Sidebar() {
  const { usuario } = useAuth();
  const items = menuPara(usuario);
  return (
    <aside className={`side ${esIndependiente(usuario) ? 'morado' : ''}`}>
      <div className="marca">
        <i>R</i>RutaOax
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
