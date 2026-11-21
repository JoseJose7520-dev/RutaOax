import { useCallback, useEffect, useState } from 'react';
import { leerError } from '../../api/errores';
import { eliminarVehiculo, listarVehiculos } from '../../api/vehiculosApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';
import { diasHasta, ETIQUETA_TIPO_VEHICULO, fechaCorta } from '../../utils/format';
import useDebounce from '../../utils/useDebounce';
import VehiculoFormModal from './VehiculoFormModal';

const TAMANO = 6;

function EstadoVehiculo({ v }) {
  if (v.estado === 'MANTENIMIENTO') return <span className="etq e-pend">En mantenimiento</span>;
  if (v.enUsoHoy) return <span className="etq e-cam">En uso hoy</span>;
  return <span className="etq e-ok">Disponible</span>;
}

function AvisoSeguro({ fecha }) {
  const dias = diasHasta(fecha);
  if (dias === null) return null;
  if (dias < 0) return <div className="aviso alerta">Seguro vencido el {fechaCorta(fecha)}</div>;
  if (dias <= 7) {
    return (
      <div className="aviso alerta">
        {dias === 0 ? 'El seguro vence hoy' : `El seguro vence en ${dias} ${dias === 1 ? 'día' : 'días'}`}
      </div>
    );
  }
  return <small>Seguro vigente hasta {fechaCorta(fecha)}</small>;
}

export default function VehiculosPage() {
  const [q, setQ] = useState('');
  const [tipo, setTipo] = useState('');
  const [estado, setEstado] = useState('');
  const [page, setPage] = useState(0);
  const qFinal = useDebounce(q, 400);

  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [formulario, setFormulario] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: TAMANO };
      if (qFinal.trim()) params.q = qFinal.trim();
      if (tipo) params.tipo = tipo;
      if (estado) params.estado = estado;
      setDatos(await listarVehiculos(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, qFinal, tipo, estado]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const hayFiltros = Boolean(q.trim() || tipo || estado);

  const guardado = (mensaje) => {
    setFormulario(null);
    setAviso({ tipo: 'ok', texto: mensaje });
    cargar();
  };

  const confirmarEliminar = async () => {
    setEliminando(true);
    try {
      await eliminarVehiculo(aEliminar.id);
      const eraUltimo = datos.content.length === 1 && page > 0;
      setAviso({ tipo: 'ok', texto: `Se eliminó el vehículo ${aEliminar.placa}.` });
      setAEliminar(null);
      if (eraUltimo) setPage(page - 1);
      else cargar();
    } catch (err) {
      setAviso({ tipo: 'error', texto: leerError(err).mensaje });
      setAEliminar(null);
    } finally {
      setEliminando(false);
    }
  };

  return (
    <>
      <header className="cab">
        <div>
          <h1>Vehículos</h1>
          <p>Tu flota, su disponibilidad y quién la usa hoy.</p>
        </div>
        <button type="button" className="btn" onClick={() => setFormulario({ vehiculo: null })}>
          + Registrar vehículo
        </button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <input placeholder="Buscar por placa" aria-label="Buscar por placa" value={q}
            onChange={(e) => { setQ(e.target.value); setPage(0); }} />
          <select aria-label="Filtrar por tipo" value={tipo} onChange={(e) => { setTipo(e.target.value); setPage(0); }}>
            <option value="">Todos los tipos</option>
            {Object.entries(ETIQUETA_TIPO_VEHICULO).map(([valor, texto]) => (
              <option key={valor} value={valor}>{texto}</option>
            ))}
          </select>
          <select aria-label="Filtrar por estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPage(0); }}>
            <option value="">Todos los estados</option>
            <option value="ACTIVO">Activos</option>
            <option value="MANTENIMIENTO">En mantenimiento</option>
          </select>
        </div>
      </section>

      {cargando && !datos ? <Cargando mensaje="Cargando vehículos..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargar} /> : null}

      {datos && !error ? (
        datos.content.length === 0 ? (
          <Vacio mensaje={hayFiltros ? 'Ningún vehículo coincide con los filtros.' : 'Todavía no tienes vehículos. Registra el primero.'} />
        ) : (
          <section className="grid g3" style={cargando ? { opacity: 0.6 } : undefined}>
            {datos.content.map((v) => (
              <article key={v.id} className="card pila" style={{ gap: 10 }}>
                <div className="fila-g">
                  <strong style={{ fontSize: '1.1rem' }}>{ETIQUETA_TIPO_VEHICULO[v.tipo]}</strong>
                  <EstadoVehiculo v={v} />
                </div>
                <p className="muted">Placa {v.placa} · capacidad {v.capacidad} paquetes</p>
                {v.enUsoHoy ? <small>Hoy: {v.repartidorHoy}</small> : <small>Sin recorrido hoy</small>}
                <AvisoSeguro fecha={v.seguroVence} />
                <div className="fila-g">
                  <button type="button" className="btn sec sm" onClick={() => setFormulario({ vehiculo: v })}>Editar</button>
                  <button type="button" className="btn pel sm" onClick={() => setAEliminar(v)}>Eliminar</button>
                </div>
              </article>
            ))}
          </section>
        )
      ) : null}

      {datos ? (
        <Pagination page={datos.page} totalPages={datos.totalPages} totalElements={datos.totalElements} onPage={setPage} />
      ) : null}

      {formulario ? (
        <VehiculoFormModal vehiculo={formulario.vehiculo} onCerrar={() => setFormulario(null)} onGuardado={guardado} />
      ) : null}

      <ConfirmDialog
        abierto={Boolean(aEliminar)}
        titulo="Eliminar vehículo"
        mensaje={aEliminar ? `¿Eliminar el vehículo ${aEliminar.placa}? Esta acción no se puede deshacer.` : ''}
        textoConfirmar="Sí, eliminar"
        peligroso
        cargando={eliminando}
        onConfirmar={confirmarEliminar}
        onCancelar={() => setAEliminar(null)}
      />
    </>
  );
}
