import { useCallback, useEffect, useState } from 'react';
import { listarNegociosAdmin, listarPedidosAdmin } from '../../api/adminApi';
import { leerError } from '../../api/errores';
import { eliminarPedido } from '../../api/pedidosApi';
import { listarZonas } from '../../api/zonasApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import StatusBadge from '../../components/ui/StatusBadge';
import { fechaCorta, moneda } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import PedidoFormModal from '../negocio/PedidoFormModal';

const ESTADOS = [
  ['PENDIENTE', 'Sin asignar'],
  ['ASIGNADO', 'Asignado'],
  ['EN_CAMINO', 'En camino'],
  ['ENTREGADO', 'Entregado'],
  ['NO_ENTREGADO', 'No entregado'],
];

export default function PedidosAdminPage() {
  const [q, setQ] = useState('');
  const [negocioId, setNegocioId] = useState('');
  const [estado, setEstado] = useState('');
  const [zona, setZona] = useState('');
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [negocios, setNegocios] = useState([]);
  const [zonas, setZonas] = useState([]);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    listarNegociosAdmin().then(setNegocios).catch(() => setNegocios([]));
    listarZonas().then(setZonas).catch(() => setZonas([]));
  }, []);

  // Los filtros viajan como parametros; el backend regresa solo la pagina que corresponde
  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: 10 };
      if (qFinal.trim()) params.q = qFinal.trim();
      if (negocioId) params.negocioId = negocioId;
      if (estado) params.estado = estado;
      if (zona) params.zona = zona;
      setDatos(await listarPedidosAdmin(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, qFinal, negocioId, estado, zona]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const filtrar = (setter) => (e) => {
    setter(e.target.value);
    setPage(0);
  };

  const guardado = (mensaje) => {
    setFormulario(null);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const confirmarEliminar = async () => {
    setEliminando(true);
    try {
      await eliminarPedido(aEliminar.id);
      setAviso({ tipo: 'ok', texto: `Se eliminó el pedido #${aEliminar.id}.` });
      if (datos.content.length === 1 && page > 0) setPage(page - 1);
      else cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
    } finally {
      setAEliminar(null);
      setEliminando(false);
    }
  };

  const hayFiltros = q.trim() || negocioId || estado || zona;

  return (
    <>
      <header className="cab">
        <div>
          <h1>Pedidos</h1>
          <p>Los pedidos de todos los negocios y los envíos abiertos.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ pedido: null })}>
          + Nuevo pedido
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <input placeholder="Buscar por número, cliente o dirección" aria-label="Buscar pedido" value={q}
            onChange={filtrar(setQ)} />
          <select aria-label="Filtrar por negocio" value={negocioId} onChange={filtrar(setNegocioId)}>
            <option value="">Todos los negocios</option>
            {negocios.map((n) => (
              <option key={n.id} value={n.id}>{n.nombre}</option>
            ))}
          </select>
          <select aria-label="Filtrar por estado" value={estado} onChange={filtrar(setEstado)}>
            <option value="">Todos los estados</option>
            {ESTADOS.map(([valor, texto]) => (
              <option key={valor} value={valor}>{texto}</option>
            ))}
          </select>
          <select aria-label="Filtrar por zona" value={zona} onChange={filtrar(setZona)}>
            <option value="">Todas las zonas</option>
            {zonas.map((z) => (
              <option key={z.nombre} value={z.nombre}>{z.nombre}</option>
            ))}
          </select>
        </div>
        {cargando && !datos ? <Cargando mensaje="Cargando pedidos..." /> : null}
        {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={hayFiltros ? 'Ningún pedido coincide con los filtros.' : 'Todavía no hay pedidos.'} />
          ) : (
            <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Cliente</th>
                    <th>Negocio</th>
                    <th>Zona</th>
                    <th>Estado</th>
                    <th>Repartidor</th>
                    <th>Total</th>
                    <th>Fecha</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {datos.content.map((p) => {
                    const editable = p.estado === 'PENDIENTE' || p.estado === 'ASIGNADO';
                    const borrable = p.estado !== 'EN_CAMINO' && p.estado !== 'ENTREGADO';
                    return (
                      <tr key={p.id}>
                        <td><strong>#{p.id}</strong></td>
                        <td>
                          <div className="pila">
                            <strong>{p.clienteNombre}</strong>
                            <small>{p.direccion}</small>
                          </div>
                        </td>
                        <td>{p.negocio?.nombre ?? <span className="muted">Envío abierto</span>}</td>
                        <td>{p.zona}</td>
                        <td><StatusBadge estado={p.estado} /></td>
                        <td>{p.repartidor?.nombre ?? <span className="muted">—</span>}</td>
                        <td style={{ whiteSpace: 'nowrap' }}>{moneda(p.total)}</td>
                        <td style={{ whiteSpace: 'nowrap' }}>{fechaCorta(p.creadoEn?.slice(0, 10))}</td>
                        <td>
                          <div className="acciones">
                            {editable ? (
                              <button type="button" className="btn sec sm" onClick={() => setFormulario({ pedido: p })}>
                                Editar
                              </button>
                            ) : null}
                            {borrable ? (
                              <button type="button" className="btn pel sm" onClick={() => setAEliminar(p)}>
                                Eliminar
                              </button>
                            ) : null}
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
        <PedidoFormModal pedido={formulario.pedido} zonas={zonas} negocios={negocios}
          onCerrar={() => setFormulario(null)} onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(aEliminar)}
        titulo="Eliminar pedido"
        mensaje={aEliminar ? `¿Eliminar el pedido #${aEliminar.id} de ${aEliminar.clienteNombre}? Esta acción no se puede deshacer.` : ''}
        textoConfirmar="Sí, eliminar"
        peligroso
        cargando={eliminando}
        onConfirmar={confirmarEliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
