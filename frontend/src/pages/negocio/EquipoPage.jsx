import { useCallback, useEffect, useState } from 'react';
import { leerError } from '../../api/errores';
import { eliminarMiembro, listarEquipo } from '../../api/equipoApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { iniciales } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import MiembroFormModal from './MiembroFormModal';

const TAMANO = 8;

function EstadoHoy({ m }) {
  if (m.estadoHoy === 'EN_RUTA') return <span className="etq e-cam">En ruta</span>;
  if (m.totalHoy > 0) return <span className="etq e-pend">Con ruta asignada</span>;
  return <span className="etq e-ok">Libre</span>;
}

export default function EquipoPage() {
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: TAMANO };
      if (qFinal.trim()) params.q = qFinal.trim();
      setDatos(await listarEquipo(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, qFinal]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const guardado = (mensaje) => {
    setFormulario(null);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const confirmarEliminar = async () => {
    setEliminando(true);
    try {
      await eliminarMiembro(aEliminar.id);
      const eraUltimo = datos.content.length === 1 && page > 0;
      setAviso({ tipo: 'ok', texto: `${aEliminar.usuario.nombre} ya no forma parte de tu equipo y no podrá iniciar sesión.` });
      setAEliminar(null);
      if (eraUltimo) setPage(page - 1);
      else cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setAEliminar(null);
    } finally {
      setEliminando(false);
    }
  };

  return (
    <>
      <header className="cab">
        <div>
          <h1>Equipo de reparto</h1>
          <p>Los repartidores de tu negocio.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ miembro: null })}>
          + Agregar repartidor
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <input placeholder="Buscar por nombre o correo" aria-label="Buscar repartidor" value={q}
            onChange={(e) => { setQ(e.target.value); setPage(0); }} />
        </div>
        {cargando && !datos ? <Cargando mensaje="Cargando equipo..." /> : null}
        {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={q.trim() ? 'Ningún repartidor coincide con la búsqueda.' : 'Todavía no tienes repartidores. Agrega el primero.'} />
          ) : (
            <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
              <table>
                <thead>
                  <tr>
                    <th>Repartidor</th>
                    <th>Vehículo de hoy</th>
                    <th>Avance de hoy</th>
                    <th>Estado</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {datos.content.map((m) => (
                    <tr key={m.id}>
                      <td>
                        <div className="fila-g">
                          <span className="av">{iniciales(m.usuario.nombre)}</span>
                          <div className="pila">
                            <strong>{m.usuario.nombre}</strong>
                            <small>{m.usuario.telefono || m.usuario.email}</small>
                          </div>
                        </div>
                      </td>
                      <td>{m.vehiculoHoy || <span className="muted">Sin vehículo</span>}</td>
                      <td>{m.totalHoy > 0 ? `${m.entregadosHoy} de ${m.totalHoy}` : <span className="muted">—</span>}</td>
                      <td><EstadoHoy m={m} /></td>
                      <td>
                        <div className="acciones">
                          <button type="button" className="btn sec sm" onClick={() => setFormulario({ miembro: m })}>Editar</button>
                          <button type="button" className="btn pel sm" onClick={() => setAEliminar(m)}>Eliminar</button>
                        </div>
                      </td>
                    </tr>
                  ))}
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
        <MiembroFormModal miembro={formulario.miembro} onCerrar={() => setFormulario(null)} onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(aEliminar)}
        titulo="Quitar del equipo"
        mensaje={aEliminar ? `¿Quitar a ${aEliminar.usuario.nombre} de tu equipo? Su cuenta se desactivará y ya no podrá iniciar sesión.` : ''}
        textoConfirmar="Sí, quitar"
        peligroso
        cargando={eliminando}
        onConfirmar={confirmarEliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
