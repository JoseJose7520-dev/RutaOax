import { useCallback, useEffect, useState } from 'react';
import { repartidoresDisponibles } from '../../api/equipoApi';
import { leerError } from '../../api/errores';
import { asignarPedidos } from '../../api/pedidosApi';
import { vehiculosDisponibles } from '../../api/vehiculosApi';
import Field from '../../components/ui/Field';
import { ErrorRed, Mensaje } from '../../components/ui/Feedback';
import { ETIQUETA_TIPO_VEHICULO, hoyIso } from '../../utils/format';

export default function AsignarPanel({ seleccionados, onAsignado }) {
  const [fecha, setFecha] = useState(hoyIso());
  const [repartidores, setRepartidores] = useState([]);
  const [vehiculos, setVehiculos] = useState([]);
  const [repartidorId, setRepartidorId] = useState('');
  const [vehiculoId, setVehiculoId] = useState('');
  const [cargando, setCargando] = useState(true);
  const [errorListas, setErrorListas] = useState('');
  const [asignando, setAsignando] = useState(false);
  const [error, setError] = useState('');

  const cargarListas = useCallback(async () => {
    setCargando(true);
    setErrorListas('');
    try {
      const [r, v] = await Promise.all([repartidoresDisponibles(fecha), vehiculosDisponibles(fecha)]);
      setRepartidores(r);
      setVehiculos(v);
      setRepartidorId((actual) => (r.some((x) => String(x.id) === actual) ? actual : ''));
      setVehiculoId((actual) => (v.some((x) => String(x.id) === actual) ? actual : ''));
    } catch (err) {
      setErrorListas(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [fecha]);

  useEffect(() => {
    cargarListas();
  }, [cargarListas]);

  const vehiculo = vehiculos.find((v) => String(v.id) === vehiculoId);
  const repartidor = repartidores.find((r) => String(r.id) === repartidorId);
  const cupo = vehiculo ? vehiculo.cupoLibre : null;
  const excede = cupo !== null && seleccionados.length > cupo;
  const fechaPasada = fecha < hoyIso();
  const puedeAsignar = seleccionados.length > 0 && repartidor && vehiculo && !excede && !fechaPasada && !asignando;

  const asignar = async () => {
    setAsignando(true);
    setError('');
    try {
      await asignarPedidos({
        pedidoIds: seleccionados,
        repartidorId: Number(repartidorId),
        vehiculoId: Number(vehiculoId),
        fecha,
      });
      onAsignado(`Se asignaron ${seleccionados.length} ${seleccionados.length === 1 ? 'pedido' : 'pedidos'} a ${repartidor.nombre} (${ETIQUETA_TIPO_VEHICULO[vehiculo.tipo]} ${vehiculo.placa}) para el ${fecha}.`);
      setRepartidorId('');
      setVehiculoId('');
      cargarListas();
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setAsignando(false);
    }
  };

  return (
    <aside className="card panel-asignar form-vertical">
      <h2>
        Asignar {seleccionados.length} {seleccionados.length === 1 ? 'pedido' : 'pedidos'}
      </h2>
      <Field label="Fecha de entrega" name="fecha" type="date" min={hoyIso()} value={fecha}
        onChange={(e) => setFecha(e.target.value)} error={fechaPasada ? 'La fecha no puede ser anterior a hoy' : ''} />
      {errorListas ? <ErrorRed mensaje={errorListas} onReintentar={cargarListas} /> : null}
      <Field as="select" label="Repartidor" name="repartidor" value={repartidorId} disabled={cargando}
        onChange={(e) => setRepartidorId(e.target.value)}>
        <option value="">{repartidores.length ? 'Elige un repartidor' : 'No hay repartidores libres ese día'}</option>
        {repartidores.map((r) => (
          <option key={r.id} value={r.id}>{r.nombre}</option>
        ))}
      </Field>
      <Field as="select" label="Vehículo" name="vehiculo" value={vehiculoId} disabled={cargando}
        onChange={(e) => setVehiculoId(e.target.value)}>
        <option value="">{vehiculos.length ? 'Elige un vehículo' : 'No hay vehículos libres ese día'}</option>
        {vehiculos.map((v) => (
          <option key={v.id} value={v.id}>
            {ETIQUETA_TIPO_VEHICULO[v.tipo]} {v.placa} · cupo {v.cupoLibre}
          </option>
        ))}
      </Field>
      {vehiculo ? (
        <div className={`aviso ${excede ? 'alerta' : ''}`}>
          <div className="cupo">
            <span>Cupo del vehículo</span>
            <strong>{cupo} pedidos</strong>
          </div>
          <div className="cupo">
            <span>Seleccionados</span>
            <strong>{seleccionados.length}</strong>
          </div>
          {excede ? <p>Seleccionaste más pedidos de los que caben en este vehículo.</p> : null}
        </div>
      ) : null}
      {seleccionados.length === 0 ? <small>Marca en la tabla los pedidos sin asignar que quieres incluir.</small> : null}
      {error ? <Mensaje tipo="error">{error}</Mensaje> : null}
      <button type="button" className="btn grande" disabled={!puedeAsignar} onClick={asignar}>
        {asignando ? 'Asignando...' : 'Asignar'}
      </button>
    </aside>
  );
}
