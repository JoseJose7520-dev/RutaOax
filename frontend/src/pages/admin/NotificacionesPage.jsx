import { useCallback, useEffect, useState } from 'react';
import { enviarNotificacionPrueba, listarNotificaciones } from '../../api/adminApi';
import { leerError } from '../../api/errores';
import Field from '../../components/ui/Field';
import { Cargando, ErrorRed, Mensaje, Vacio } from '../../components/ui/Feedback';
import Modal from '../../components/ui/Modal';
import Pagination from '../../components/ui/Pagination';
import { validarEmail } from '../../utils/validaciones';

const CANALES = { EMAIL: 'Correo', SMS: 'SMS', WHATSAPP: 'WhatsApp' };

function fechaHora(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleString('es-MX', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

function validarDestino(canal, valor) {
  const v = String(valor).trim();
  if (!v) return canal === 'EMAIL' ? 'Escribe el correo de destino' : 'Escribe el teléfono de destino';
  if (canal === 'EMAIL') return validarEmail(v);
  return /^\d{10}$/.test(v) ? '' : 'El teléfono debe tener 10 dígitos';
}

function PruebaModal({ onCerrar, onEnviado }) {
  const [canal, setCanal] = useState('EMAIL');
  const [destino, setDestino] = useState('');
  const [error, setError] = useState('');
  const [tocado, setTocado] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState('');
  const [enviando, setEnviando] = useState(false);

  const enviar = async (e) => {
    e.preventDefault();
    const err = validarDestino(canal, destino);
    setTocado(true);
    setError(err);
    setErrorGeneral('');
    if (err) return;
    setEnviando(true);
    try {
      const r = await enviarNotificacionPrueba({ canal, destino: destino.trim() });
      onEnviado(r);
    } catch (ex) {
      const info = leerError(ex);
      if (info.errores?.destino) setError(info.errores.destino);
      setErrorGeneral(info.mensaje);
      setEnviando(false);
    }
  };

  return (
    <Modal
      abierto
      titulo="Enviar mensaje de prueba"
      onCerrar={enviando ? undefined : onCerrar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCerrar} disabled={enviando}>Cancelar</button>
          <button type="submit" form="form-prueba" className="btn" disabled={enviando}>
            {enviando ? 'Enviando...' : 'Enviar'}
          </button>
        </>
      }
    >
      <form id="form-prueba" className="form-vertical" onSubmit={enviar} noValidate>
        <p className="muted">Sirve para comprobar que el correo, el SMS y el WhatsApp están bien configurados.</p>
        <Field as="select" label="Canal" name="canal" value={canal}
          onChange={(e) => { setCanal(e.target.value); if (tocado) setError(validarDestino(e.target.value, destino)); }}>
          <option value="EMAIL">Correo</option>
          <option value="SMS">SMS</option>
          <option value="WHATSAPP">WhatsApp</option>
        </Field>
        <Field
          label={canal === 'EMAIL' ? 'Correo de destino' : 'Teléfono de destino (10 dígitos)'}
          name="destino"
          type={canal === 'EMAIL' ? 'email' : 'tel'}
          inputMode={canal === 'EMAIL' ? undefined : 'numeric'}
          value={destino}
          onChange={(e) => { setDestino(e.target.value); if (tocado) setError(validarDestino(canal, e.target.value)); }}
          onBlur={() => { setTocado(true); setError(validarDestino(canal, destino)); }}
          error={error}
        />
        {canal === 'WHATSAPP' ? (
          <small>El número debe haber escrito antes al número de WhatsApp de RutaOax (ventana de 24 horas de Meta).</small>
        ) : null}
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
      </form>
    </Modal>
  );
}

export default function NotificacionesPage() {
  const [canal, setCanal] = useState('');
  const [estado, setEstado] = useState('');
  const [page, setPage] = useState(0);
  const [datos, setDatos] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState(null);
  const [prueba, setPrueba] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    try {
      const params = { page, size: 10 };
      if (canal) params.canal = canal;
      if (estado) params.estado = estado;
      setDatos(await listarNotificaciones(params));
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, [page, canal, estado]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const enviado = (r) => {
    setPrueba(false);
    setAviso(r.estado === 'ENVIADO'
      ? { tipo: 'ok', texto: `Mensaje de prueba enviado. ${r.detalle ?? ''}` }
      : { tipo: 'error', texto: `No se pudo enviar: ${r.detalle ?? 'revisa la configuración'}` });
    setTimeout(cargar, 1500);
  };

  return (
    <>
      <header className="cab">
        <div>
          <h1>Notificaciones</h1>
          <p>Historial de correos, SMS y WhatsApp que envía el sistema.</p>
        </div>
        <button type="button" className="btn" onClick={() => setPrueba(true)}>Enviar prueba</button>
      </header>

      {aviso ? <Mensaje tipo={aviso.tipo}>{aviso.texto}</Mensaje> : null}

      <section className="card plano">
        <div className="barra-filtros">
          <select aria-label="Filtrar por canal" value={canal} onChange={(e) => { setCanal(e.target.value); setPage(0); }}>
            <option value="">Todos los canales</option>
            <option value="EMAIL">Correo</option>
            <option value="SMS">SMS</option>
            <option value="WHATSAPP">WhatsApp</option>
          </select>
          <select aria-label="Filtrar por estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPage(0); }}>
            <option value="">Enviadas y fallidas</option>
            <option value="ENVIADO">Enviadas</option>
            <option value="FALLIDO">Fallidas</option>
          </select>
          <button type="button" className="btn sec" onClick={cargar} disabled={cargando}>Actualizar</button>
        </div>
        {cargando && !datos ? <Cargando mensaje="Cargando notificaciones..." /> : null}
        {error ? <div style={{ padding: 16 }}><ErrorRed mensaje={error} onReintentar={cargar} /></div> : null}
        {datos && !error ? (
          datos.content.length === 0 ? (
            <Vacio mensaje={canal || estado ? 'Ninguna notificación coincide con los filtros.' : 'Todavía no se ha enviado ninguna notificación.'} />
          ) : (
            <div className="tabla-scroll" style={cargando ? { opacity: 0.6 } : undefined}>
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Canal</th>
                    <th>Destino</th>
                    <th>Evento</th>
                    <th>Estado</th>
                    <th>Detalle</th>
                  </tr>
                </thead>
                <tbody>
                  {datos.content.map((n) => (
                    <tr key={n.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>{fechaHora(n.creadoEn)}</td>
                      <td>{CANALES[n.canal] ?? n.canal}</td>
                      <td>{n.destino}</td>
                      <td>{n.evento}</td>
                      <td>
                        {n.estado === 'ENVIADO'
                          ? <span className="etq e-ok">Enviado</span>
                          : <span className="etq e-no">Fallido</span>}
                      </td>
                      <td><small>{n.detalle}</small></td>
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

      {prueba ? <PruebaModal onCerrar={() => setPrueba(false)} onEnviado={enviado} /> : null}
    </>
  );
}
