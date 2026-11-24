import { useCallback, useEffect, useState } from 'react';
import { cambiarEstadoUsuario, eliminarUsuario, listarUsuarios } from '../../api/adminUsuariosApi';
import { leerError } from '../../api/errores';
import { useAuth } from '../../auth/AuthContext';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { fechaCorta, iniciales } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import UsuarioFormModal from './UsuarioFormModal';

const TAMANO = 10;

const ROLES = {
  ADMIN: { texto: 'Administrador', clase: 'e-mor' },
  NEGOCIO: { texto: 'Negocio', clase: 'e-cam' },
  REPARTIDOR: { texto: 'Repartidor', clase: 'e-pend' },
  CLIENTE: { texto: 'Cliente', clase: 'e-gris' },
};

function EtiquetaRol({ usuario }) {
  return usuario.roles.map((r) => {
    const info = ROLES[r] ?? { texto: r, clase: 'e-gris' };
    let texto = info.texto;
    if (r === 'REPARTIDOR' && usuario.perfil?.tipoRepartidor === 'INDEPENDIENTE') texto = 'Independiente';
    return <span key={r} className={`etq ${info.clase}`}>{texto}</span>;
  });
}

export default function UsuariosPage() {
  const { usuario: yo } = useAuth();
  const [q, setQ] = useState('');
  const [rol, setRol] = useState('');
  const [activo, setActivo] = useState('');
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [formulario, setFormulario] = useState(null);
  const [confirmar, setConfirmar] = useState(null);
  const [procesando, setProcesando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: TAMANO };
      if (qFinal.trim()) params.q = qFinal.trim();
      if (rol) params.rol = rol;
      if (activo) params.activo = activo;
      setDatos(await listarUsuarios(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, qFinal, rol, activo]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const guardado = (mensaje) => {
    setFormulario(null);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const ejecutar = async () => {
    const { accion, usuario } = confirmar;
    setProcesando(true);
    try {
      if (accion === 'eliminar') {
        await eliminarUsuario(usuario.id);
        setAviso({ tipo: 'ok', texto: `Se eliminó la cuenta de ${usuario.nombre}.` });
        if (datos.content.length === 1 && page > 0) setPage(page - 1);
        else cargar();
      } else {
        const nuevo = accion === 'activar';
        await cambiarEstadoUsuario(usuario.id, nuevo);
        setAviso({
          tipo: 'ok',
          texto: nuevo
            ? `${usuario.nombre} ya puede iniciar sesión de nuevo.`
            : `${usuario.nombre} ya no podrá iniciar sesión. Su historial se conserva.`,
        });
        cargar();
      }
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
    } finally {
      setConfirmar(null);
      setProcesando(false);
    }
  };

  const textos = {
    eliminar: {
      titulo: 'Eliminar cuenta',
      mensaje: (u) => `¿Eliminar la cuenta de ${u.nombre}? Esta acción no se puede deshacer. Si la cuenta tiene pedidos o recorridos, el sistema te pedirá desactivarla.`,
      boton: 'Sí, eliminar',
    },
    desactivar: {
      titulo: 'Desactivar cuenta',
      mensaje: (u) => `¿Desactivar a ${u.nombre}? No podrá iniciar sesión, pero su información se conserva.`,
      boton: 'Sí, desactivar',
    },
    activar: {
      titulo: 'Activar cuenta',
      mensaje: (u) => `¿Activar de nuevo a ${u.nombre}?`,
      boton: 'Activar',
    },
  };
  const t = confirmar ? textos[confirmar.accion] : null;
  const hayFiltros = q.trim() || rol || activo;

  return (
    <>
      <header className="cab">
        <div>
          <h1>Usuarios</h1>
          <p>Todas las cuentas del sistema.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ usuario: null })}>
          + Nuevo usuario
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <input placeholder="Buscar por nombre o correo" aria-label="Buscar usuario" value={q}
            onChange={(e) => { setQ(e.target.value); setPage(0); }} />
          <select aria-label="Filtrar por rol" value={rol} onChange={(e) => { setRol(e.target.value); setPage(0); }}>
            <option value="">Todos los roles</option>
            <option value="ADMIN">Administrador</option>
            <option value="NEGOCIO">Negocio</option>
            <option value="REPARTIDOR">Repartidor</option>
            <option value="CLIENTE">Cliente</option>
          </select>
          <select aria-label="Filtrar por estado" value={activo}
            onChange={(e) => { setActivo(e.target.value); setPage(0); }}>
            <option value="">Activas e inactivas</option>
            <option value="true">Solo activas</option>
            <option value="false">Solo inactivas</option>
          </select>
        </div>
        {cargando && !datos ? <Cargando mensaje="Cargando usuarios..." /> : null}
        {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={hayFiltros ? 'Ninguna cuenta coincide con los filtros.' : 'Todavía no hay cuentas registradas.'} />
          ) : (
            <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
              <table>
                <thead>
                  <tr>
                    <th>Usuario</th>
                    <th>Rol</th>
                    <th>Negocio</th>
                    <th>Alta</th>
                    <th>Estado</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {datos.content.map((u) => {
                    const soyYo = u.id === yo?.id;
                    return (
                      <tr key={u.id}>
                        <td>
                          <div className="fila-g" style={{ flexWrap: 'nowrap' }}>
                            <span className="av">{iniciales(u.nombre)}</span>
                            <div className="pila">
                              <strong>{u.nombre}{soyYo ? ' (tú)' : ''}</strong>
                              <small>{u.email}</small>
                            </div>
                          </div>
                        </td>
                        <td><div className="fila-g"><EtiquetaRol usuario={u} /></div></td>
                        <td>{u.perfil?.negocioNombre || <span className="muted">—</span>}</td>
                        <td style={{ whiteSpace: 'nowrap' }}>{fechaCorta(u.creadoEn?.slice(0, 10))}</td>
                        <td>
                          {u.activo
                            ? <span className="etq e-ok">Activa</span>
                            : <span className="etq e-no">Inactiva</span>}
                        </td>
                        <td>
                          <div className="acciones">
                            <button type="button" className="btn sec sm" onClick={() => setFormulario({ usuario: u })}>
                              Editar
                            </button>
                            {soyYo ? null : (
                              <>
                                <button type="button" className="btn sec sm"
                                  onClick={() => setConfirmar({ accion: u.activo ? 'desactivar' : 'activar', usuario: u })}>
                                  {u.activo ? 'Desactivar' : 'Activar'}
                                </button>
                                <button type="button" className="btn pel sm"
                                  onClick={() => setConfirmar({ accion: 'eliminar', usuario: u })}>
                                  Eliminar
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )
        ) : null}
      </section>

      {datos ? (
        <Pagination page={datos.page} totalPages={datos.totalPages} totalElements={datos.totalElements} onPage={setPage} />
      ) : null}

      {formulario ? (
        <UsuarioFormModal usuario={formulario.usuario} onCerrar={() => setFormulario(null)} onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(confirmar)}
        titulo={t?.titulo}
        mensaje={confirmar ? t.mensaje(confirmar.usuario) : ''}
        textoConfirmar={t?.boton}
        peligroso={confirmar?.accion !== 'activar'}
        cargando={procesando}
        onConfirmar={ejecutar}
        onCancelar={() => setConfirmar(null)}
      />
    </>
  );
}
