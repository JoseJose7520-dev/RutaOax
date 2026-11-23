import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { marcarEstado, obtenerRutaHoy } from '../../api/recorridosApi';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Field from '../../components/ui/Field';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Modal from '../../components/ui/Modal';
import StatusBadge from '../../components/ui/StatusBadge';
import { moneda } from '../../utils/format';

/** base: '/repartidor' para el repartidor de empresa, '/independiente/ruta' para el independiente. */
export default function ParadaPage({ base = '/repartidor', paradaBase = '/repartidor/parada' }) {
  const { pedidoId } = useParams();
  const navigate = useNavigate();
  const [ruta, setRuta] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [errorAccion, setErrorAccion] = useState('');
  const [confirmarEntrega, setConfirmarEntrega] = useState(false);
  const [modalNoEntregado, setModalNoEntregado] = useState(false);
  const [motivo, setMotivo] = useState('');
  const [errorMotivo, setErrorMotivo] = useState('');
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setRuta(await obtenerRutaHoy());
    } catch (err) {
      const e = leerError(err);
      setError(e.status === 404 ? 'Hoy no tienes entregas asignadas.' : e.mensaje);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const paradas = ruta?.paradas ?? [];
  const indice = paradas.findIndex((p) => String(p.id) === String(pedidoId));
  const parada = indice >= 0 ? paradas[indice] : null;
  const siguiente = paradas.find((p, i) => i > indice && p.estado === 'EN_CAMINO')
    ?? paradas.find((p, i) => i < indice && p.estado === 'EN_CAMINO');

  const despuesDeMarcar = () => {
    if (siguiente) {
      navigate(`${paradaBase}/${siguiente.id}`);
    } else {
      navigate(base);
    }
  };

  const enviar = async (datos) => {
    setGuardando(true);
    setErrorAccion('');
    try {
      await marcarEstado(parada.id, datos);
      setConfirmarEntrega(false);
      setModalNoEntregado(false);
      setMotivo('');
      despuesDeMarcar();
      await cargar();
    } catch (err) {
      setErrorAccion(leerError(err).mensaje);
      setConfirmarEntrega(false);
      setModalNoEntregado(false);
    } finally {
      setGuardando(false);
    }
  };

  const validarMotivo = (texto) => {
    const limpio = texto.trim();
    if (limpio.length < 5) return 'Escribe brevemente qué pasó (mínimo 5 caracteres).';
    if (limpio.length > 200) return 'El motivo no puede pasar de 200 caracteres.';
    return '';
  };

  const confirmarNoEntregado = () => {
    const mensaje = validarMotivo(motivo);
    setErrorMotivo(mensaje);
    if (!mensaje) enviar({ estado: 'NO_ENTREGADO', motivo: motivo.trim() });
  };

  if (cargando && !ruta) return <Cargando mensaje="Cargando la parada..." />;
  if (error) {
    return (
      <div className="movil">
        <ErrorRed mensaje={error} onReintentar={cargar} />
        <Link className="btn sec" to={base} style={{ marginTop: 12 }}>Volver a mi ruta</Link>
      </div>
    );
  }
  if (!parada) {
    return (
      <div className="movil">
        <Vacio mensaje="Esta parada no está en tu ruta de hoy." />
        <Link className="btn sec" to={base} style={{ marginTop: 12 }}>Volver a mi ruta</Link>
      </div>
    );
  }

  const mapa = `https://www.google.com/maps/dir/?api=1&destination=${parada.lat},${parada.lng}`;
  const enCamino = parada.estado === 'EN_CAMINO';

  return (
    <div className="movil" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Link to={base} className="enlace">← Mi ruta</Link>

      {errorAccion ? <Mensaje tipo="error">{errorAccion}</Mensaje> : null}

      <section className="foco-parada">
        <div className="fila-g" style={{ justifyContent: 'space-between' }}>
          <small>Parada {parada.ordenParada} de {paradas.length}</small>
          <StatusBadge estado={parada.estado} />
        </div>
        <h1>{parada.clienteNombre}</h1>
        <div className="pila">
          <strong>{parada.direccion}</strong>
          <small>{parada.zona}</small>
        </div>
        <small>
          {parada.articulos} {parada.articulos === 1 ? 'artículo' : 'artículos'} · {moneda(parada.total)} · Pedido #{parada.id}
        </small>
        <div className="fila-g">
          {parada.clienteTelefono ? (
            <a className="btn sec" href={`tel:${parada.clienteTelefono}`}>Llamar</a>
          ) : null}
          <a className="btn sec" href={mapa} target="_blank" rel="noopener noreferrer">Cómo llegar</a>
        </div>
      </section>

      {parada.notas ? <p className="nota-caja"><strong>Notas:</strong> {parada.notas}</p> : null}

      {enCamino ? (
        <>
          <button type="button" className="btn grande" onClick={() => setConfirmarEntrega(true)} disabled={guardando}>
            Marcar como entregado
          </button>
          <button type="button" className="btn sec" onClick={() => setModalNoEntregado(true)} disabled={guardando}>
            No pude entregar
          </button>
        </>
      ) : parada.estado === 'ASIGNADO' ? (
        <p className="aviso alerta">Inicia tu recorrido desde "Mi ruta" para poder marcar esta entrega.</p>
      ) : (
        <p className="aviso">Esta parada ya está cerrada.</p>
      )}

      {siguiente ? (
        <article className="card">
          <small>Siguiente parada</small>
          <div className="fila">
            <div className="pila">
              <strong>{siguiente.clienteNombre}</strong>
              <small>{siguiente.direccion} · {siguiente.zona}</small>
            </div>
            <Link className="btn sec sm" to={`${paradaBase}/${siguiente.id}`}>Ver</Link>
          </div>
        </article>
      ) : null}

      <ConfirmDialog
        abierto={confirmarEntrega}
        titulo="Confirmar entrega"
        mensaje={`¿Entregaste el pedido #${parada.id} a ${parada.clienteNombre}?`}
        textoConfirmar="Sí, entregado"
        cargando={guardando}
        onConfirmar={() => enviar({ estado: 'ENTREGADO' })}
        onCancelar={() => setConfirmarEntrega(false)}
      />

      <Modal
        abierto={modalNoEntregado}
        titulo="No pude entregar"
        onCerrar={guardando ? undefined : () => setModalNoEntregado(false)}
        pie={
          <>
            <button type="button" className="btn sec" onClick={() => setModalNoEntregado(false)} disabled={guardando}>
              Cancelar
            </button>
            <button type="button" className="btn peligro" onClick={confirmarNoEntregado} disabled={guardando}>
              {guardando ? 'Guardando...' : 'Marcar no entregado'}
            </button>
          </>
        }
      >
        <Field
          as="textarea"
          label="¿Qué pasó?"
          name="motivo"
          rows={3}
          maxLength={200}
          value={motivo}
          placeholder="Por ejemplo: no había nadie en el domicilio"
          error={errorMotivo}
          onChange={(e) => {
            setMotivo(e.target.value);
            if (errorMotivo) setErrorMotivo(validarMotivo(e.target.value));
          }}
          onBlur={() => setErrorMotivo(validarMotivo(motivo))}
        />
      </Modal>
    </div>
  );
}
