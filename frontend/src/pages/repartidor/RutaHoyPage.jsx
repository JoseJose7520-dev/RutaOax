import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { iniciarRecorrido, obtenerRutaHoy } from '../../api/recorridosApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import StatusBadge from '../../components/ui/StatusBadge';
import { ETIQUETA_TIPO_VEHICULO, fechaLarga } from '../../utils/format';

const TERMINADOS = ['ENTREGADO', 'NO_ENTREGADO'];

export default function RutaHoyPage() {
  const navigate = useNavigate();
  const [ruta, setRuta] = useState(null);
  const [sinRuta, setSinRuta] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [confirmar, setConfirmar] = useState(false);
  const [iniciando, setIniciando] = useState(false);
  const [errorAccion, setErrorAccion] = useState('');

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

  const iniciar = async () => {
    setIniciando(true);
    setErrorAccion('');
    try {
      setRuta(await iniciarRecorrido(ruta.id));
      setConfirmar(false);
    } catch (err) {
      setErrorAccion(leerError(err).mensaje);
      setConfirmar(false);
    } finally {
      setIniciando(false);
    }
  };

  const actual = ruta?.paradas.find((p) => p.estado === 'EN_CAMINO');
  const porcentaje = ruta && ruta.total > 0 ? Math.round((ruta.entregados / ruta.total) * 100) : 0;
  const vehiculo = ruta?.vehiculo
    ? `${ETIQUETA_TIPO_VEHICULO[ruta.vehiculo.tipo] ?? ruta.vehiculo.tipo} ${ruta.vehiculo.placa}`
    : 'Sin vehículo';

  return (
    <>
      <header className="cab">
        <div>
          <h1>Mi ruta de hoy</h1>
          <p>{fechaLarga()}</p>
        </div>
        <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>
          {cargando ? 'Actualizando...' : 'Actualizar'}
        </button>
      </header>

      {cargando && !ruta && !sinRuta ? <Cargando mensaje="Cargando tu ruta..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}
      {sinRuta && !error ? <Vacio mensaje="Hoy no tienes entregas asignadas." /> : null}
      {errorAccion ? <Mensaje tipo="error">{errorAccion}</Mensaje> : null}

      {ruta && !error ? (
        <>
          <section className="card">
            <div className="fila">
              <div className="pila">
                <strong>{vehiculo}</strong>
                <small>{ruta.entregados} de {ruta.total} entregados</small>
              </div>
              <StatusBadgeRecorrido estado={ruta.estado} />
            </div>
            <div className="barra" style={{ marginTop: 8 }}><b style={{ width: `${porcentaje}%` }} /></div>
            <div className="acciones" style={{ marginTop: 16 }}>
              {ruta.estado === 'PLANEADO' ? (
                <button type="button" className="btn" onClick={() => setConfirmar(true)}>
                  Iniciar recorrido
                </button>
              ) : null}
              {actual ? (
                <button type="button" className="btn" onClick={() => navigate(`/repartidor/parada/${actual.id}`)}>
                  Ir a la parada actual
                </button>
              ) : null}
            </div>
            {ruta.estado === 'FINALIZADO' ? (
              <p className="aviso" style={{ marginTop: 12 }}>Terminaste tu recorrido de hoy.</p>
            ) : null}
          </section>

          <section className="card" style={{ padding: 0 }}>
            <ul className="paradas">
              {ruta.paradas.map((p) => {
                const hecha = TERMINADOS.includes(p.estado);
                const esActual = actual?.id === p.id;
                return (
                  <li key={p.id} className={hecha ? 'hecha' : esActual ? 'actual' : ''}>
                    <span className={`num ${esActual ? 'act' : hecha ? 'hecho' : ''}`}>{p.ordenParada}</span>
                    <div className="pila">
                      <strong>{p.clienteNombre}</strong>
                      <small>{p.direccion} · {p.zona}</small>
                      <small>{p.articulos} {p.articulos === 1 ? 'artículo' : 'artículos'} · Pedido #{p.id}</small>
                    </div>
                    <div className="fila-g">
                      <StatusBadge estado={p.estado} />
                      {p.estado === 'EN_CAMINO' ? (
                        <Link className="btn sec sm" to={`/repartidor/parada/${p.id}`}>Abrir</Link>
                      ) : null}
                    </div>
                  </li>
                );
              })}
            </ul>
          </section>
        </>
      ) : null}

      <ConfirmDialog
        abierto={confirmar}
        titulo="Iniciar recorrido"
        mensaje={`Vas a salir con ${ruta?.total ?? 0} entregas. Tus pedidos pasarán a "En camino".`}
        textoConfirmar="Iniciar"
        cargando={iniciando}
        onConfirmar={iniciar}
        onCancelar={() => setConfirmar(false)}
      />
    </>
  );
}

function StatusBadgeRecorrido({ estado }) {
  const datos = {
    PLANEADO: ['e-pend', 'Por iniciar'],
    EN_CURSO: ['e-cam', 'En curso'],
    FINALIZADO: ['e-ok', 'Terminado'],
  }[estado] ?? ['e-gris', estado];
  return <span className={`etq ${datos[0]}`}>{datos[1]}</span>;
}
