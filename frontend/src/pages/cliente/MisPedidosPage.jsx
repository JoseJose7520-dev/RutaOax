import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { listarPedidos, obtenerSeguimiento } from '../../api/pedidosApi';
import { Cargando, ErrorRed, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import StatusBadge from '../../components/ui/StatusBadge';
import { moneda } from '../../utils/format';

const TEXTO_PASO = {
  RECIBIDO: 'Pedido recibido',
  ASIGNADO: 'Asignado a un repartidor',
  EN_CAMINO: 'En camino',
  ENTREGADO: 'Entregado',
  NO_ENTREGADO: 'No se pudo entregar',
};

function fechaHora(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('es-MX', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

export default function MisPedidosPage() {
  const [params, setParams] = useSearchParams();
  const seleccionado = params.get('pedido');
  const [pagina, setPagina] = useState(0);
  const [lista, setLista] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setLista(await listarPedidos({ page: pagina, size: 5 }));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [pagina]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    if (!seleccionado && lista?.content.length) {
      setParams({ pedido: String(lista.content[0].id) }, { replace: true });
    }
  }, [lista, seleccionado, setParams]);

  return (
    <>
      <header className="cab">
        <div>
          <h1>Mis pedidos</h1>
          <p>Revisa en qué va cada uno de tus envíos.</p>
        </div>
        <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>
          {cargando ? 'Actualizando...' : 'Actualizar'}
        </button>
      </header>

      {cargando && !lista ? <Cargando mensaje="Cargando tus pedidos..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}

      {lista && !error ? (
        lista.content.length === 0 ? (
          <Vacio mensaje="Todavía no tienes pedidos. Cuando un negocio registre uno con tu correo, aparecerá aquí." />
        ) : (
          <section className="grid g-mapa">
            <div className="card">
              <h2>Tus pedidos</h2>
              <ul>
                {lista.content.map((p) => (
                  <li key={p.id} className="fila">
                    <button
                      type="button"
                      className="pila"
                      onClick={() => setParams({ pedido: String(p.id) })}
                      style={{ background: 'none', border: 0, padding: 0, textAlign: 'left', cursor: 'pointer', font: 'inherit' }}
                      aria-pressed={String(p.id) === seleccionado}
                    >
                      <strong style={{ color: String(p.id) === seleccionado ? 'var(--azul)' : undefined }}>
                        Pedido #{p.id}
                      </strong>
                      <small>{p.negocio?.nombre ?? p.origen ?? 'Envío abierto'} · {moneda(p.total)}</small>
                    </button>
                    <StatusBadge estado={p.estado} />
                  </li>
                ))}
              </ul>
              <Pagination
                page={lista.page}
                totalPages={lista.totalPages}
                totalElements={lista.totalElements}
                onPage={setPagina}
              />
            </div>

            {seleccionado ? <Seguimiento pedidoId={seleccionado} pedido={lista.content.find((p) => String(p.id) === seleccionado)} /> : null}
          </section>
        )
      ) : null}
    </>
  );
}

function Seguimiento({ pedidoId, pedido }) {
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setDatos(await obtenerSeguimiento(pedidoId));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [pedidoId]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  if (cargando && !datos) return <div className="card"><Cargando mensaje="Cargando seguimiento..." /></div>;
  if (error) return <div className="card"><ErrorRed mensaje={error} onReintentar={cargar} /></div>;
  if (!datos) return null;

  const ahora = datos.pasos.findIndex((p) => !p.hecho);

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div className="fila">
        <div className="pila">
          <h2>Pedido #{datos.pedidoId}</h2>
          <small>{datos.negocio?.nombre ?? 'Envío abierto'}</small>
        </div>
        <StatusBadge estado={datos.estado} />
      </div>

      {pedido ? (
        <p className="nota-caja">
          {pedido.direccion} · {pedido.zona}
          <br />
          {pedido.articulos} {pedido.articulos === 1 ? 'artículo' : 'artículos'} · {moneda(pedido.total)}
        </p>
      ) : null}

      {datos.repartidor ? (
        <p>
          Lo lleva <strong>{datos.repartidor.nombre}</strong>
          {datos.repartidor.tipo === 'INDEPENDIENTE' ? ' (repartidor independiente)' : ''}.
          {datos.estado === 'EN_CAMINO' || datos.estado === 'ASIGNADO'
            ? datos.paradasAntes > 0
              ? datos.paradasAntes === 1
                ? ' Falta 1 entrega antes de la tuya.'
                : ` Faltan ${datos.paradasAntes} entregas antes de la tuya.`
              : ' Tu pedido es la siguiente entrega.'
            : ''}
        </p>
      ) : (
        <p className="muted">Todavía no tiene repartidor asignado.</p>
      )}

      <div className="linea">
        {datos.pasos.map((paso, i) => (
          <div key={paso.clave} className={`paso ${paso.hecho ? 'hecho' : i === ahora ? 'ahora' : ''}`}>
            <span className="punto">{paso.hecho ? '✓' : i + 1}</span>
            <div className="pila">
              <strong>{TEXTO_PASO[paso.clave] ?? paso.clave}</strong>
              {paso.fecha ? <small>{fechaHora(paso.fecha)}</small> : null}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
