import { useCallback, useEffect, useState } from 'react';
import { listarNegociosAdmin, listarVehiculosAdmin } from '../../api/adminApi';
import { leerError } from '../../api/errores';
import { eliminarVehiculo } from '../../api/vehiculosApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { ETIQUETA_TIPO_VEHICULO, fechaCorta } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import VehiculoFormModal from '../negocio/VehiculoFormModal';

export default function VehiculosAdminPage() {
  const [q, setQ] = useState('');
  const [negocioId, setNegocioId] = useState('');
  const [tipo, setTipo] = useState('');
  const [estado, setEstado] = useState('');
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [negocios, setNegocios] = useState([]);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  useEffect(() => {
    listarNegociosAdmin().then(setNegocios).catch(() => setNegocios([]));
  }, []);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: 10 };
      if (qFinal.trim()) params.q = qFinal.trim();
      if (negocioId) params.negocioId = negocioId;
      if (tipo) params.tipo = tipo;
      if (estado) params.estado = estado;
      setDatos(await listarVehiculosAdmin(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, qFinal, negocioId, tipo, estado]);

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
      await eliminarVehiculo(aEliminar.id);
      setAviso({ tipo: 'ok', texto: `Se eliminó el vehículo ${aEliminar.placa}.` });
      if (datos.content.length === 1 && page > 0) setPage(page - 1);
      else cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
    } finally {
      setAEliminar(null);
      setEliminando(false);
    }
  };

  const hayFiltros = q.trim() || negocioId || tipo || estado;

  return (
    <>
      <header className="cab">
        <div>
          <h1>Vehículos</h1>
          <p>La flota de todos los negocios.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ vehiculo: null })}>
          + Registrar vehículo
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <input placeholder="Buscar por placa" aria-label="Buscar por placa" value={q} onChange={filtrar(setQ)} />
          <select aria-label="Filtrar por negocio" value={negocioId} onChange={filtrar(setNegocioId)}>
            <option value="">Todos los negocios</option>
            {negocios.map((n) => (
              <option key={n.id} value={n.id}>{n.nombre}</option>
            ))}
          </select>
          <select aria-label="Filtrar por tipo" value={tipo} onChange={filtrar(setTipo)}>
            <option value="">Todos los tipos</option>
            {Object.entries(ETIQUETA_TIPO_VEHICULO).map(([valor, texto]) => (
              <option key={valor} value={valor}>{texto}</option>
            ))}
          </select>
          <select aria-label="Filtrar por estado" value={estado} onChange={filtrar(setEstado)}>
            <option value="">Todos los estados</option>
            <option value="ACTIVO">Activos</option>
            <option value="MANTENIMIENTO">En mantenimiento</option>
          </select>
        </div>
        {cargando && !datos ? <Cargando mensaje="Cargando vehículos..." /> : null}
        {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={hayFiltros ? 'Ningún vehículo coincide con los filtros.' : 'Todavía no hay vehículos.'} />
          ) : (
            <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
              <table>
                <thead>
                  <tr>
                    <th>Placa</th>
                    <th>Tipo</th>
                    <th>Negocio</th>
                    <th>Capacidad</th>
                    <th>Estado</th>
                    <th>Hoy</th>
                    <th>Seguro</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {datos.content.map((v) => (
                    <tr key={v.id}>
                      <td><strong>{v.placa}</strong></td>
                      <td>{ETIQUETA_TIPO_VEHICULO[v.tipo] ?? v.tipo}</td>
                      <td>{v.negocio?.nombre}</td>
                      <td>{v.capacidad} paquetes</td>
                      <td>
                        {v.estado === 'ACTIVO'
                          ? <span className="etq e-ok">Activo</span>
                          : <span className="etq e-pend">Mantenimiento</span>}
                      </td>
                      <td>{v.enUsoHoy ? v.repartidorHoy : <span className="muted">Libre</span>}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        {v.seguroVence ? fechaCorta(v.seguroVence) : <span className="muted">—</span>}
                      </td>
                      <td>
                        <div className="acciones">
                          <button type="button" className="btn sec sm" onClick={() => setFormulario({ vehiculo: v })}>
                            Editar
                          </button>
                          <button type="button" className="btn pel sm" onClick={() => setAEliminar(v)}>
                            Eliminar
                          </button>
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
        <VehiculoFormModal vehiculo={formulario.vehiculo} negocios={negocios} onCerrar={() => setFormulario(null)}
          onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(aEliminar)}
        titulo="Eliminar vehículo"
        mensaje={aEliminar ? `¿Eliminar el vehículo ${aEliminar.placa} de ${aEliminar.negocio?.nombre}? Esta acción no se puede deshacer.` : ''}
        textoConfirmar="Sí, eliminar"
        peligroso
        cargando={eliminando}
        onConfirmar={confirmarEliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
