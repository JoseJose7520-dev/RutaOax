import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { desasignarPedido, eliminarPedido, listarPedidos, publicarPedido, reasignarPedido } from '../../api/pedidosApi';
import { listarZonas } from '../../api/zonasApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import StatusBadge from '../../components/ui/StatusBadge';
import { moneda } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import AsignarPanel from './AsignarPanel';
import PedidoFormModal from './PedidoFormModal';

const ESTADOS = ['PENDIENTE', 'ASIGNADO', 'EN_CAMINO', 'ENTREGADO', 'NO_ENTREGADO'];

export default function PedidosPage() {
  const [params] = useSearchParams();
  const [q, setQ] = useState('');
  const [estado, setEstado] = useState(() => (ESTADOS.includes(params.get('estado')) ? params.get('estado') : ''));
  const [zona, setZona] = useState('');
  const [modalidad, setModalidad] = useState('');
  const [tamano, setTamano] = useState(10);
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [zonas, setZonas] = useState([]);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [seleccion, setSeleccion] = useState([]);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [aDesasignar, setADesasignar] = useState(null);
  const [aPublicar, setAPublicar] = useState(null);
  const [aReasignar, setAReasignar] = useState(null);
  const [procesando, setProcesando] = useState(false);

  useEffect(() => {
    listarZonas().then(setZonas).catch(() => setZonas([]));
  }, []);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: tamano };
      if (qFinal.trim()) params.q = qFinal.trim();
      if (estado) params.estado = estado;
      if (zona) params.zona = zona;
      if (modalidad) params.modalidad = modalidad;
      setDatos(await listarPedidos(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, tamano, qFinal, estado, zona, modalidad]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const filtrar = (setter) => (e) => {
    setter(e.target.value);
    setPage(0);
  };

  const pendientesPagina = datos ? datos.content.filter((p) => p.estado === 'PENDIENTE') : [];
  const todosMarcados = pendientesPagina.length > 0 && pendientesPagina.every((p) => seleccion.includes(p.id));

  const alternar = (id) => setSeleccion((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));
  const alternarTodos = () => {
    const ids = pendientesPagina.map((p) => p.id);
    setSeleccion((s) => (todosMarcados ? s.filter((x) => !ids.includes(x)) : [...new Set([...s, ...ids])]));
  };

  const guardado = (mensaje) => {
    setFormulario(null);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const asignado = (mensaje) => {
    setSeleccion([]);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const confirmarEliminar = async () => {
    setProcesando(true);
    try {
      await eliminarPedido(aEliminar.id);
      const eraUltimo = datos.content.length === 1 && page > 0;
      setSeleccion((s) => s.filter((x) => x !== aEliminar.id));
      setAviso({ tipo: 'ok', texto: `Se eliminó el pedido #${aEliminar.id}.` });
      setAEliminar(null);
      if (eraUltimo) setPage(page - 1);
      else cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setAEliminar(null);
    } finally {
      setProcesando(false);
    }
  };

  const confirmarDesasignar = async () => {
    setProcesando(true);
    try {
      await desasignarPedido(aDesasignar.id);
      setAviso({ tipo: 'ok', texto: `El pedido #${aDesasignar.id} volvió a estar sin asignar.` });
      setADesasignar(null);
      cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setADesasignar(null);
    } finally {
      setProcesando(false);
    }
  };

  const confirmarPublicar = async () => {
    setProcesando(true);
    try {
      await publicarPedido(aPublicar.id);
      setAviso({ tipo: 'ok', texto: `El pedido #${aPublicar.id} ya está publicado para repartidores independientes.` });
      setAPublicar(null);
      cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setAPublicar(null);
    } finally {
      setProcesando(false);
    }
  };

  const confirmarReasignar = async () => {
    setProcesando(true);
    try {
      await reasignarPedido(aReasignar.id);
      setAviso({ tipo: 'ok', texto: `El pedido #${aReasignar.id} volvió a estar sin asignar. Ya puedes asignarlo de nuevo.` });
      setAReasignar(null);
      cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setAReasignar(null);
    } finally {
      setProcesando(false);
    }
  };

  const hayFiltros = Boolean(q.trim() || estado || zona || modalidad);

  return (
    <>
      <header className="cab">
        <div>
          <h1>Pedidos y asignación</h1>
          <p>Elige pedidos sin asignar y asígnalos a un repartidor con su vehículo.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ pedido: null })}>
          + Nuevo pedido
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="grid g-mapa">
        <div className="card plano">
          <div className="barra-filtros">
            <input placeholder="Buscar folio, cliente o dirección" aria-label="Buscar pedidos" value={q}
              onChange={filtrar(setQ)} />
            <select aria-label="Filtrar por estado" value={estado} onChange={filtrar(setEstado)}>
              <option value="">Todos los estados</option>
              <option value="PENDIENTE">Sin asignar</option>
              <option value="ASIGNADO">Asignado</option>
              <option value="EN_CAMINO">En camino</option>
              <option value="ENTREGADO">Entregado</option>
              <option value="NO_ENTREGADO">No entregado</option>
            </select>
            <select aria-label="Filtrar por zona" value={zona} onChange={filtrar(setZona)}>
              <option value="">Todas las zonas</option>
              {zonas.map((z) => (
                <option key={z.nombre} value={z.nombre}>{z.nombre}</option>
              ))}
            </select>
            <select aria-label="Filtrar por tipo" value={modalidad} onChange={filtrar(setModalidad)}>
              <option value="">Todos los tipos</option>
              <option value="NEGOCIO">De mi equipo</option>
              <option value="ABIERTO">Publicados</option>
            </select>
          </div>

          {cargando && !datos ? <Cargando mensaje="Cargando pedidos..." /> : null}
          {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
          {datos && !error ? (
            datos.content.length === 0 ? (
              <Vacio mensaje={hayFiltros ? 'Ningún pedido coincide con los filtros.' : 'Todavía no tienes pedidos. Crea el primero.'} />
            ) : (
              <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
                <table>
                  <thead>
                    <tr>
                      <th className="col-check">
                        <input type="checkbox" aria-label="Marcar todos los pedidos sin asignar de esta página"
                          checked={todosMarcados} disabled={pendientesPagina.length === 0} onChange={alternarTodos} />
                      </th>
                      <th>Folio</th>
                      <th>Cliente y destino</th>
                      <th>Estado</th>
                      <th>Asignado a</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {datos.content.map((p) => (
                      <tr key={p.id}>
                        <td className="col-check">
                          <input type="checkbox" aria-label={`Marcar el pedido ${p.id}`} disabled={p.estado !== 'PENDIENTE'}
                            checked={seleccion.includes(p.id)} onChange={() => alternar(p.id)} />
                        </td>
                        <td>#{p.id}</td>
                        <td>
                          <strong>{p.clienteNombre}</strong>
                          <small>{p.direccion} · {p.zona}</small>
                          <small>{p.articulos} {p.articulos === 1 ? 'artículo' : 'artículos'} · {moneda(p.total)}
                            {p.modalidad === 'ABIERTO' ? ' · Publicado' : ''}</small>
                        </td>
                        <td><StatusBadge estado={p.estado} /></td>
                        <td>{p.repartidor ? p.repartidor.nombre : <span className="muted">—</span>}</td>
                        <td>
                          <div className="acciones">
                            {p.estado === 'PENDIENTE' && p.modalidad !== 'ABIERTO' ? (
                              <button type="button" className="btn sec sm" onClick={() => setAPublicar(p)}>Publicar</button>
                            ) : null}
                            {p.estado === 'NO_ENTREGADO' ? (
                              <button type="button" className="btn sec sm" onClick={() => setAReasignar(p)}>Reasignar</button>
                            ) : null}
                            {p.estado === 'ASIGNADO' ? (
                              <button type="button" className="btn sec sm" onClick={() => setADesasignar(p)}>Desasignar</button>
                            ) : null}
                            {p.estado === 'PENDIENTE' || p.estado === 'ASIGNADO' ? (
                              <button type="button" className="btn sec sm" onClick={() => setFormulario({ pedido: p })}>Editar</button>
                            ) : null}
                            {p.estado !== 'EN_CAMINO' && p.estado !== 'ENTREGADO' ? (
                              <button type="button" className="btn pel sm" onClick={() => setAEliminar(p)}>Eliminar</button>
                            ) : null}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          ) : null}
          <div style={{ padding: '0 20px 8px' }}>
            {datos ? (
              <div className="fila" style={{ borderBottom: 0 }}>
                <label className="fila-g">
                  <small>Por página</small>
                  <select aria-label="Pedidos por página" value={tamano} style={{ width: 'auto' }}
                    onChange={(e) => { setTamano(Number(e.target.value)); setPage(0); }}>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={40}>40</option>
                  </select>
                </label>
                <Pagination page={datos.page} totalPages={datos.totalPages} totalElements={datos.totalElements} onPage={setPage} />
              </div>
            ) : null}
          </div>
        </div>

        <AsignarPanel seleccionados={seleccion} onAsignado={asignado} />
      </section>

      {formulario ? (
        <PedidoFormModal pedido={formulario.pedido} zonas={zonas} onCerrar={() => setFormulario(null)} onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(aEliminar)}
        titulo="Eliminar pedido"
        mensaje={aEliminar ? `¿Eliminar el pedido #${aEliminar.id} de ${aEliminar.clienteNombre}? Esta acción no se puede deshacer.` : ''}
        textoConfirmar="Sí, eliminar"
        peligroso
        cargando={procesando}
        onConfirmar={confirmarEliminar}
        onCancelar={() => setAEliminar(null)}
      />
      <ConfirmDialog
        abierto={Boolean(aPublicar)}
        titulo="Publicar para independientes"
        mensaje={aPublicar ? `¿Publicar el pedido #${aPublicar.id}? Quedará visible para repartidores independientes, que podrán tomarlo. Mientras nadie lo tome, también puedes asignarlo a tu equipo.` : ''}
        textoConfirmar="Sí, publicar"
        cargando={procesando}
        onConfirmar={confirmarPublicar}
        onCancelar={() => setAPublicar(null)}
      />
      <ConfirmDialog
        abierto={Boolean(aReasignar)}
        titulo="Reasignar pedido"
        mensaje={aReasignar ? `El pedido #${aReasignar.id} no se pudo entregar. ¿Volver a ponerlo como sin asignar para asignarlo de nuevo?` : ''}
        textoConfirmar="Sí, reasignar"
        cargando={procesando}
        onConfirmar={confirmarReasignar}
        onCancelar={() => setAReasignar(null)}
      />
      <ConfirmDialog
        abierto={Boolean(aDesasignar)}
        titulo="Desasignar pedido"
        mensaje={aDesasignar ? `¿Quitar el pedido #${aDesasignar.id} de su recorrido? Volverá a estar sin asignar.` : ''}
        textoConfirmar="Sí, desasignar"
        cargando={procesando}
        onConfirmar={confirmarDesasignar}
        onCancelar={() => setADesasignar(null)}
      />
    </>
  );
}
