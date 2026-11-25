import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function AppLayout() {
  // En celular el menu lateral se abre con el boton de la barra superior y se cierra al navegar
  const [menuAbierto, setMenuAbierto] = useState(false);
  const { pathname } = useLocation();

  useEffect(() => {
    setMenuAbierto(false);
  }, [pathname]);

  return (
    <div className="app">
      <Sidebar abierto={menuAbierto} />
      {menuAbierto ? <div className="velo" onClick={() => setMenuAbierto(false)} aria-hidden="true" /> : null}
      <div className="main">
        <Topbar onMenu={() => setMenuAbierto((v) => !v)} menuAbierto={menuAbierto} />
        <main className="contenido">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
