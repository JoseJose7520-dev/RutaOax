import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { obtenerResumen } from '../../api/negocioApi';
import { Cargando, ErrorRed, Vacio } from '../../components/ui/Feedback';
import { iniciales } from '../../utils/format';

const ENLACES_ATENCION = {
  NO_ENTREGADO: { ruta: '/negocio/pedidos?estado=NO_ENTREGADO', texto: 'Reasignar' },
  MENSAJE: { ruta: '/negocio/mensajes', texto: 'Abrir' },
  SEGURO: { ruta: '/negocio/vehiculos', texto: 'Ver' },
};

export default function PanelPage() {
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setDatos(await obtenerResumen());
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  return (
    <>
      <header className="cab">
        <div>
          <h1>Hoy en tu operación</h1>
          <p>Lo que necesita tu atención primero.</p>
        </div>
        <div className="fila-g">
          <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>
            {cargando ? 'Actualizando...' : 'Actualizar'}
          </button>
          <Link className="btn" to="/negocio/pedidos">Asignar pedidos</Link>
        </div>
      </header>

      {cargando && !datos ? <Cargando mensaje="Cargando tu panel..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}

      {datos && !error ? (
        <>
          <section className="grid g4">
            <article className="card kpi foco">
              <p>Sin asignar</p>
              <strong>{datos.sinAsignar}</strong>
            </article>
            <article className="card kpi">
              <p>En camino</p>
              <strong>{datos.enCamino}</strong>
            </article>
            <article className="card kpi">
              <p>Entregados hoy</p>
              <strong>{datos.entregadosHoy}</strong>
            </article>
            <article className="card kpi">
              <p>Mensajes nuevos</p>
              <strong>{datos.mensajesNuevos}</strong>
            </article>
          </section>

          <section className="grid g-mapa">
            <div className="card">
              <h2>Repartidores hoy</h2>
              {datos.repartidoresHoy.length === 0 ? (
                <Vacio mensaje="Hoy ninguno de tus repartidores tiene una ruta asignada." />
              ) : (
                <ul>
                  {datos.repartidoresHoy.map((r) => {
                    const porcentaje = r.total > 0 ? Math.round((r.entregados / r.total) * 100) : 0;
                    return (
                      <li key={r.repartidorId} className="fila">
                        <div className="fila-g">
                          <span className="av">{iniciales(r.nombre)}</span>
                          <div className="pila">
                            <strong>{r.nombre}</strong>
                            <small>{r.vehiculo || 'Sin vehículo'}</small>
                          </div>
                        </div>
                        <div style={{ width: 200 }}>
                          <div className="barra"><b style={{ width: `${porcentaje}%` }} /></div>
                          <small>{r.entregados} de {r.total} entregados</small>
                        </div>
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>

            <div className="card">
              <h2>Requiere atención</h2>
              {datos.atencion.length === 0 ? (
                <Vacio mensaje="Todo en orden: no hay nada pendiente por atender." />
              ) : (
                <ul>
                  {datos.atencion.map((a) => {
                    const enlace = ENLACES_ATENCION[a.tipo];
                    return (
                      <li key={`${a.tipo}-${a.referenciaId}`} className="fila">
                        <div className="pila">
                          <strong>{a.titulo}</strong>
                          <small>{a.detalle}</small>
                        </div>
                        {enlace ? <Link className="btn sec sm" to={enlace.ruta}>{enlace.texto}</Link> : null}
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>
          </section>
        </>
      ) : null}
    </>
  );
}
