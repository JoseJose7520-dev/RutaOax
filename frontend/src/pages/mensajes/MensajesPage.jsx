import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { enviarMensaje, listarHilos, marcarLeido, obtenerConversacion } from '../../api/mensajesApi';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Pagination from '../../components/ui/Pagination';

const MAX = 1000;

function hora(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('es-MX', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

/** Mensajes por pedido. La usan el cliente, el negocio y el repartidor independiente. */
export default function MensajesPage({ morado = false }) {
  const [params, setParams] = useSearchParams();
  const pedidoAbierto = params.get('pedido');
  const [pagina, setPagina] = useState(0);
  const [hilos, setHilos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargarHilos = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      setHilos(await listarHilos({ page: pagina, size: 8 }));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [pagina]);

  useEffect(() => {
    cargarHilos();
  }, [cargarHilos]);

  return (
    <div className={morado ? 'mor-n' : ''}>
      <header className="cab">
        <div>
          <h1>Mensajes</h1>
          <p>Cada conversación es sobre un pedido.</p>
        </div>
        <button type="button" className="btn sec" onClick={cargarHilos} disabled={cargando}>
          {cargando ? 'Actualizando...' : 'Actualizar'}
        </button>
      </header>

      {cargando && !hilos ? <Cargando mensaje="Cargando conversaciones..." /> : null}
      {error ? <ErrorRed mensaje={error} onReintentar={cargarHilos} /> : null}

      {hilos && !error ? (
        hilos.content.length === 0 && !pedidoAbierto ? (
          <Vacio mensaje="Todavía no tienes conversaciones." />
        ) : (
          <section className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div className="chat">
              <div className="chat-lista">
                {hilos.content.map((h) => (
                  <button
                    key={h.pedidoId}
                    type="button"
                    className={`hilo ${String(h.pedidoId) === pedidoAbierto ? 'on' : ''}`}
                    onClick={() => setParams({ pedido: String(h.pedidoId) })}
                    style={{ width: '100%', textAlign: 'left', border: 0, cursor: 'pointer', font: 'inherit',
                      background: String(h.pedidoId) === pedidoAbierto ? undefined : 'transparent' }}
                  >
                    <div className="fila-g" style={{ justifyContent: 'space-between' }}>
                      <strong>{h.contraparte?.nombre ?? h.titulo}</strong>
                      {h.noLeidos > 0 ? <span className="etq e-cam">{h.noLeidos} nuevo{h.noLeidos === 1 ? '' : 's'}</span> : null}
                    </div>
                    <small>{h.titulo} · {hora(h.fecha)}</small>
                    <small className="muted" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {h.ultimoMensaje}
                    </small>
                  </button>
                ))}
                <div style={{ padding: '0 12px' }}>
                  <Pagination page={hilos.page} totalPages={hilos.totalPages} totalElements={hilos.totalElements} onPage={setPagina} />
                </div>
              </div>
              {pedidoAbierto ? (
                <Conversacion key={pedidoAbierto} pedidoId={pedidoAbierto} onCambio={cargarHilos} />
              ) : (
                <div className="chat-caja" style={{ justifyContent: 'center' }}>
                  <Vacio mensaje="Elige una conversación." />
                </div>
              )}
            </div>
          </section>
        )
      ) : null}
    </div>
  );
}

function Conversacion({ pedidoId, onCambio }) {
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [texto, setTexto] = useState('');
  const [errorTexto, setErrorTexto] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState('');
  const fin = useRef(null);

  const cargar = useCallback(async (silencioso = false) => {
    if (!silencioso) setCargando(true);
    setError('');
    try {
      const respuesta = await obtenerConversacion(pedidoId, { page: 0, size: 50 });
      setDatos(respuesta);
      if (respuesta.content.some((m) => !m.mio && !m.leido)) {
        await marcarLeido(pedidoId);
        onCambio();
      }
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [pedidoId, onCambio]);

  useEffect(() => {
    cargar();
    const intervalo = setInterval(() => cargar(true), 15000);
    return () => clearInterval(intervalo);
  }, [cargar]);

  useEffect(() => {
    fin.current?.scrollIntoView?.({ block: 'end' });
  }, [datos]);

  const validar = (valor) => {
    if (!valor.trim()) return 'Escribe un mensaje.';
    if (valor.length > MAX) return `El mensaje no puede pasar de ${MAX} caracteres.`;
    return '';
  };

  const enviar = async (e) => {
    e.preventDefault();
    const problema = validar(texto);
    setErrorTexto(problema);
    if (problema) return;
    setEnviando(true);
    setErrorEnvio('');
    try {
      await enviarMensaje(pedidoId, texto.trim());
      setTexto('');
      await cargar(true);
      onCambio();
    } catch (err) {
      const e2 = leerError(err);
      if (e2.errores?.texto) setErrorTexto(e2.errores.texto);
      else setErrorEnvio(e2.mensaje);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="chat-caja">
      <div className="chat-cab">
        <strong>Pedido #{pedidoId}</strong>
        <button type="button" className="btn sec sm" onClick={() => cargar()} disabled={cargando}>Actualizar</button>
      </div>
      <div className="chat-msgs" aria-live="polite">
        {cargando && !datos ? <Cargando mensaje="Cargando mensajes..." /> : null}
        {error ? <ErrorRed mensaje={error} onReintentar={() => cargar()} /> : null}
        {datos && !error && datos.content.length === 0 ? <Vacio mensaje="Escribe el primer mensaje sobre este pedido." /> : null}
        {datos && !error
          ? datos.content.map((m) => (
            <div key={m.id} className={`m ${m.mio ? 'yo' : ''}`} style={{ display: 'flex', flexDirection: 'column' }}>
              {!m.mio ? <small style={{ display: 'block', fontWeight: 600, color: 'inherit' }}>{m.remitente.nombre}</small> : null}
              <span style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{m.texto}</span>
              <small style={{ display: 'block', opacity: 0.75, marginTop: 4, color: 'inherit' }}>{hora(m.creadoEn)}</small>
            </div>
          ))
          : null}
        <div ref={fin} />
      </div>
      {errorEnvio ? <div style={{ padding: '0 20px' }}><Mensaje tipo="error">{errorEnvio}</Mensaje></div> : null}
      <form className="chat-env" onSubmit={enviar} noValidate>
        <div className="campo" style={{ flex: 1, margin: 0 }}>
          <label htmlFor="texto-mensaje" className="sr-only" style={{ position: 'absolute', left: -9999 }}>Mensaje</label>
          <textarea
            id="texto-mensaje"
            rows={2}
            maxLength={MAX + 50}
            placeholder="Escribe un mensaje..."
            value={texto}
            className={errorTexto ? 'invalido' : ''}
            aria-invalid={errorTexto ? 'true' : 'false'}
            aria-describedby={errorTexto ? 'texto-mensaje-error' : undefined}
            onChange={(e) => {
              setTexto(e.target.value);
              if (errorTexto) setErrorTexto(validar(e.target.value));
            }}
          />
          {errorTexto ? <span className="error" id="texto-mensaje-error" role="alert">{errorTexto}</span> : null}
          <small className="muted">{texto.length}/{MAX}</small>
        </div>
        <button type="submit" className="btn" disabled={enviando} style={{ alignSelf: 'flex-start' }}>
          {enviando ? 'Enviando...' : 'Enviar'}
        </button>
      </form>
    </div>
  );
}
