import { useCallback, useEffect, useState } from 'react';
import { leerError } from '../../api/errores';
import { listarRecorridos } from '../../api/recorridosApi';
import { Cargando, ErrorRed, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { ETIQUETA_TIPO_VEHICULO, fechaCorta } from '../../utils/format';

export default function HistorialPage() {
  const [pagina, setPagina] = useState(0);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setDatos(await listarRecorridos({ estado: 'FINALIZADO', page: pagina, size: 10 }));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [pagina]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  return (
    <>
      <header className="cab">
        <div>
          <h1>Historial</h1>
          <p>Tus recorridos terminados.</p>
        </div>
      </header>

      {cargando && !datos ? <Cargando mensaje="Cargando tu historial..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}

      {datos && !error ? (
        <section className="card">
          {datos.content.length === 0 ? (
            <Vacio mensaje="Todavía no tienes recorridos terminados." />
          ) : (
            <ul>
              {datos.content.map((r) => {
                const noEntregados = r.paradas.filter((p) => p.estado === 'NO_ENTREGADO').length;
                return (
                  <li key={r.id} className="fila">
                    <div className="pila">
                      <strong>{fechaCorta(r.fecha)}</strong>
                      <small>
                        {r.vehiculo
                          ? `${ETIQUETA_TIPO_VEHICULO[r.vehiculo.tipo] ?? r.vehiculo.tipo} ${r.vehiculo.placa}`
                          : 'Sin vehículo'}
                      </small>
                    </div>
                    <div className="fila-g">
                      <span className="etq e-ok">{r.entregados} entregados</span>
                      {noEntregados > 0 ? <span className="etq e-no">{noEntregados} sin entregar</span> : null}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
          <Pagination
            page={datos.page}
            totalPages={datos.totalPages}
            totalElements={datos.totalElements}
            onPage={setPagina}
          />
        </section>
      ) : null}
    </>
  );
}
