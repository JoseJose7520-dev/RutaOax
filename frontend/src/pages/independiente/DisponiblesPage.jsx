import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listarDisponibles, tomarEntrega } from '../../api/disponiblesApi';
import { leerError } from '../../api/errores';
import { listarZonas } from '../../api/zonasApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { moneda } from '../../utils/format';

const DISTANCIAS = [
  { valor: '', texto: 'Cualquier distancia' },
  { valor: '2', texto: 'Hasta 2 km del centro' },
  { valor: '5', texto: 'Hasta 5 km del centro' },
  { valor: '10', texto: 'Hasta 10 km del centro' },
];

export default function DisponiblesPage() {
  const [zonas, setZonas] = useState([]);
  const [zona, setZona] = useState('');
  const [maxKm, setMaxKm] = useState('');
  const [pagina, setPagina] = useState(0);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [elegida, setElegida] = useState(null);
  const [tomando, setTomando] = useState(false);

  useEffect(() => {
    listarZonas().then(setZonas).catch(() => setZonas([]));
  }, []);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setDatos(await listarDisponibles({ zona: zona || undefined, maxKm: maxKm || undefined, page: pagina, size: 8 }));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [zona, maxKm, pagina]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const filtrar = (setter) => (e) => {
    setter(e.target.value);
    setPagina(0);
  };

  const tomar = async () => {
    setTomando(true);
    setAviso(null);
    try {
      const ruta = await tomarEntrega(elegida.id);
      setAviso({ tipo: 'ok', texto: `Listo, la entrega #${elegida.id} quedó en tu ruta de hoy (parada ${ruta.paradas.length}).` });
      setElegida(null);
      await cargar();
    } catch (err) {
      const e = leerError(err);
      setAviso({ tipo: 'error', texto: e.mensaje });
      setElegida(null);
      if (e.status === 409) await cargar();
    } finally {
      setTomando(false);
    }
  };

  return (
    <>
      <header className="cab">
        <div>
          <h1>Entregas disponibles</h1>
          <p>Pedidos publicados por negocios y clientes. Toma los que te convengan.</p>
        </div>
        <Link className="btn sec" to="/independiente/ruta">Ver mi ruta</Link>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card">
        <div className="barra-filtros">
          <select aria-label="Filtrar por zona" value={zona} onChange={filtrar(setZona)}>
            <option value="">Todas las zonas</option>
            {zonas.map((z) => (
              <option key={z.nombre} value={z.nombre}>{z.nombre}</option>
            ))}
          </select>
          <select aria-label="Filtrar por distancia" value={maxKm} onChange={filtrar(setMaxKm)}>
            {DISTANCIAS.map((d) => (
              <option key={d.valor} value={d.valor}>{d.texto}</option>
            ))}
          </select>
          <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>
            {cargando ? 'Actualizando...' : 'Actualizar'}
          </button>
        </div>

        {cargando && !datos ? <Cargando mensaje="Buscando entregas..." /> : null}
        {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}

        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={zona || maxKm ? 'No hay entregas con esos filtros.' : 'Por ahora no hay entregas disponibles. Vuelve a revisar en un rato.'} />
          ) : (
            <ul>
              {datos.content.map((d) => (
                <li key={d.id} className="fila">
                  <div className="pila">
                    <strong>{d.origen ?? d.negocio?.nombre ?? 'Envío'} → {d.zona}</strong>
                    <small>{d.direccion}</small>
                    <small>
                      {d.articulos} {d.articulos === 1 ? 'artículo' : 'artículos'} · a {d.distanciaKm} km del centro · Pedido #{d.id}
                    </small>
                  </div>
                  <div className="fila-g">
                    <span className="etq e-mor">{d.pagoRepartidor != null ? `Pago ${moneda(d.pagoRepartidor)}` : 'Pago por acordar'}</span>
                    <button type="button" className="btn sm" onClick={() => setElegida(d)}>Tomar</button>
                  </div>
                </li>
              ))}
            </ul>
          )
        ) : null}

        {datos && !error ? (
          <Pagination page={datos.page} totalPages={datos.totalPages} totalElements={datos.totalElements} onPage={setPagina} />
        ) : null}
      </section>

      <ConfirmDialog
        abierto={Boolean(elegida)}
        titulo="Tomar entrega"
        mensaje={elegida ? `¿Agregar el pedido #${elegida.id} (${elegida.direccion}, ${elegida.zona}) a tu ruta de hoy?` : ''}
        textoConfirmar="Sí, tomarla"
        cargando={tomando}
        onConfirmar={tomar}
        onCancelar={() => setElegida(null)}
      />
    </>
  );
}
