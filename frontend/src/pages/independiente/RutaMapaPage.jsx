import 'leaflet/dist/leaflet.css';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { CircleMarker, MapContainer, Polyline, Popup, TileLayer, useMap } from 'react-leaflet';
import { Link, useNavigate } from 'react-router-dom';
import { soltarEntrega } from '../../api/disponiblesApi';
import { leerError } from '../../api/errores';
import { iniciarRecorrido, obtenerRutaHoy } from '../../api/recorridosApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import StatusBadge from '../../components/ui/StatusBadge';
import { fechaLarga, moneda } from '../../utils/format';

const CENTRO = [17.0609, -96.7253];
const COLOR = { ENTREGADO: '#2f8a57', NO_ENTREGADO: '#c0392b', EN_CAMINO: '#6c4fd1', ASIGNADO: '#8a8f99' };

/** Ajusta el mapa para que se vean todas las paradas. */
function Encuadre({ puntos }) {
  const mapa = useMap();
  useEffect(() => {
    // el contenedor puede cambiar de tamaño al cargar la página; se recalcula antes de encuadrar
    const t = setTimeout(() => {
      mapa.invalidateSize();
      if (puntos.length > 1) mapa.fitBounds(puntos, { padding: [40, 40], maxZoom: 16 });
      else if (puntos.length === 1) mapa.setView(puntos[0], 15);
    }, 50);
    return () => clearTimeout(t);
  }, [mapa, puntos]);
  return null;
}

export default function RutaMapaPage() {
  const navigate = useNavigate();
  const [ruta, setRuta] = useState(null);
  const [sinRuta, setSinRuta] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [confirmar, setConfirmar] = useState(null);
  const [trabajando, setTrabajando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setRuta(await obtenerRutaHoy());
      setSinRuta(false);
    } catch (err) {
      const e = leerError(err);
      if (e.status === 404) {
        setRuta(null);
        setSinRuta(true);
      } else {
        setError(e.mensaje);
      }
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const paradas = useMemo(() => ruta?.paradas ?? [], [ruta]);
  const puntos = useMemo(
    () => paradas.filter((p) => p.lat != null && p.lng != null).map((p) => [Number(p.lat), Number(p.lng)]),
    [paradas],
  );
  const actual = paradas.find((p) => p.estado === 'EN_CAMINO');

  const ejecutar = async () => {
    setTrabajando(true);
    setAviso(null);
    try {
      if (confirmar.tipo === 'iniciar') {
        setRuta(await iniciarRecorrido(ruta.id));
        setAviso({ tipo: 'ok', texto: 'Recorrido iniciado. Se está avisando por SMS a tus clientes que vas en camino.' });
      } else {
        await soltarEntrega(confirmar.parada.id);
        setAviso({ tipo: 'ok', texto: `Soltaste la entrega #${confirmar.parada.id}; volvió a quedar disponible.` });
        await cargar();
      }
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
    } finally {
      setTrabajando(false);
      setConfirmar(null);
    }
  };

  return (
    <div className="mor-n">
      <header className="cab">
        <div>
          <h1>Mi ruta y mapa</h1>
          <p>{fechaLarga()}</p>
        </div>
        <div className="fila-g">
          <Link className="btn sec" to="/independiente">Buscar entregas</Link>
          <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>
            {cargando ? 'Actualizando...' : 'Actualizar'}
          </button>
        </div>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}
      {cargando && !ruta && !sinRuta ? <Cargando mensaje="Cargando tu ruta..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}
      {sinRuta && !error ? (
        <Vacio mensaje="Hoy todavía no tomas entregas. Ve a Entregas disponibles para tomar alguna." />
      ) : null}

      {ruta && !error ? (
        <section className="grid g-mapa">
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <MapContainer center={CENTRO} zoom={14} style={{ height: 420, width: '100%' }} scrollWheelZoom={false}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <Encuadre puntos={puntos} />
              {puntos.length > 1 ? <Polyline positions={puntos} pathOptions={{ color: '#6c4fd1', weight: 3, dashArray: '6 6' }} /> : null}
              {paradas.filter((p) => p.lat != null).map((p) => (
                <CircleMarker
                  key={p.id}
                  center={[Number(p.lat), Number(p.lng)]}
                  radius={11}
                  pathOptions={{ color: '#fff', weight: 2, fillColor: COLOR[p.estado] ?? '#8a8f99', fillOpacity: 1 }}
                >
                  <Popup>
                    <strong>{p.ordenParada}. {p.clienteNombre}</strong>
                    <br />
                    {p.direccion}
                  </Popup>
                </CircleMarker>
              ))}
            </MapContainer>
          </div>

          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <div className="fila">
              <div className="pila">
                <strong>{ruta.entregados} de {ruta.total} entregadas</strong>
                <small>{ruta.estado === 'PLANEADO' ? 'Aún no sales' : ruta.estado === 'EN_CURSO' ? 'En ruta' : 'Ruta terminada'}</small>
              </div>
              {ruta.estado === 'PLANEADO' && ruta.total > 0 ? (
                <button type="button" className="btn" onClick={() => setConfirmar({ tipo: 'iniciar' })}>Iniciar recorrido</button>
              ) : null}
              {actual ? (
                <button type="button" className="btn" onClick={() => navigate(`/independiente/parada/${actual.id}`)}>Parada actual</button>
              ) : null}
            </div>
            <ul className="paradas">
              {paradas.map((p) => (
                <li key={p.id} className={actual?.id === p.id ? 'actual' : ''}>
                  <span className={`num ${actual?.id === p.id ? 'act' : ''}`}>{p.ordenParada}</span>
                  <div className="pila">
                    <strong>{p.clienteNombre}</strong>
                    <small>{p.direccion} · {p.zona}</small>
                    {p.pagoRepartidor != null ? <small>Pago {moneda(p.pagoRepartidor)}</small> : null}
                  </div>
                  <div className="fila-g">
                    <StatusBadge estado={p.estado} />
                    {p.estado === 'ASIGNADO' ? (
                      <button type="button" className="btn sec sm" onClick={() => setConfirmar({ tipo: 'soltar', parada: p })}>Soltar</button>
                    ) : null}
                    {p.estado === 'EN_CAMINO' ? (
                      <Link className="btn sec sm" to={`/independiente/parada/${p.id}`}>Abrir</Link>
                    ) : null}
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </section>
      ) : null}

      <ConfirmDialog
        abierto={Boolean(confirmar)}
        titulo={confirmar?.tipo === 'iniciar' ? 'Iniciar recorrido' : 'Soltar entrega'}
        mensaje={confirmar?.tipo === 'iniciar'
          ? `Vas a salir con ${ruta?.total ?? 0} entregas. Cada cliente recibirá un SMS de que vas en camino.`
          : `¿Soltar la entrega #${confirmar?.parada?.id}? Volverá a quedar disponible para otros repartidores.`}
        textoConfirmar={confirmar?.tipo === 'iniciar' ? 'Iniciar' : 'Sí, soltar'}
        peligroso={confirmar?.tipo === 'soltar'}
        cargando={trabajando}
        onConfirmar={ejecutar}
        onCancelar={() => setConfirmar(null)}
      />
    </div>
  );
}
