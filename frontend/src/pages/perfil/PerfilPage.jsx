import { useCallback, useEffect, useState } from 'react';
import { leerError } from '../../api/errores';
import { cambiarPassword, getPerfil, updatePerfil } from '../../api/perfilApi';
import { useAuth } from '../../auth/AuthContext';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Field from '../../components/ui/Field';
import { Cargando, ErrorRed, Mensaje } from '../../components/ui/Feedback';
import { iniciales } from '../../utils/format';
import {
  requisitosPassword,
  validarPasswordSegura,
  validarRequerido,
  validarTelefono,
} from '../../utils/validaciones';

function validarAvatar(valor) {
  const v = String(valor ?? '').trim();
  if (!v) return '';
  return /^https?:\/\/\S+$/i.test(v) ? '' : 'El avatar debe ser una URL que empiece con http:// o https://';
}

function validarDato(campo, valor) {
  if (campo === 'nombre') return validarRequerido(valor, 'El nombre');
  if (campo === 'telefono') return validarTelefono(valor);
  return validarAvatar(valor);
}

function validarClave(campo, valor, claves) {
  if (campo === 'actual') return validarRequerido(valor, 'La contraseña actual');
  if (campo === 'nueva') return validarPasswordSegura(valor);
  if (!valor) return 'Confirma tu contraseña nueva';
  return valor === claves.nueva ? '' : 'Las contraseñas no coinciden';
}

const CLAVES_VACIAS = { actual: '', nueva: '', confirmacion: '' };

export default function PerfilPage() {
  const { refrescar } = useAuth();
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState('');
  const [email, setEmail] = useState('');
  const [datos, setDatos] = useState({ nombre: '', telefono: '', avatarUrl: '' });
  const [tocadosDatos, setTocadosDatos] = useState({});
  const [erroresDatos, setErroresDatos] = useState({});
  const [okDatos, setOkDatos] = useState('');
  const [errorDatos, setErrorDatos] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [claves, setClaves] = useState(CLAVES_VACIAS);
  const [tocadosClaves, setTocadosClaves] = useState({});
  const [erroresClaves, setErroresClaves] = useState({});
  const [okClave, setOkClave] = useState('');
  const [errorClave, setErrorClave] = useState('');
  const [confirmando, setConfirmando] = useState(false);
  const [cambiando, setCambiando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setErrorCarga('');
    try {
      const p = await getPerfil();
      setEmail(p.email);
      setDatos({ nombre: p.nombre || '', telefono: p.telefono || '', avatarUrl: p.avatarUrl || '' });
    } catch (err) {
      setErrorCarga(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  if (cargando) return <Cargando mensaje="Cargando tu perfil..." />;
  if (errorCarga) return <ErrorRed mensaje={errorCarga} onReintentar={cargar} />;

  const cambiarDato = (e) => {
    const { name, value } = e.target;
    setDatos((d) => ({ ...d, [name]: value }));
    setOkDatos('');
    if (tocadosDatos[name]) setErroresDatos((er) => ({ ...er, [name]: validarDato(name, value) }));
  };

  const salirDato = (e) => {
    const { name, value } = e.target;
    setTocadosDatos((t) => ({ ...t, [name]: true }));
    setErroresDatos((er) => ({ ...er, [name]: validarDato(name, value) }));
  };

  const guardarDatos = async (e) => {
    e.preventDefault();
    const nuevos = {
      nombre: validarDato('nombre', datos.nombre),
      telefono: validarDato('telefono', datos.telefono),
      avatarUrl: validarDato('avatarUrl', datos.avatarUrl),
    };
    setTocadosDatos({ nombre: true, telefono: true, avatarUrl: true });
    setErroresDatos(nuevos);
    setOkDatos('');
    setErrorDatos('');
    if (Object.values(nuevos).some(Boolean)) return;

    setGuardando(true);
    try {
      await updatePerfil({
        nombre: datos.nombre.trim(),
        telefono: datos.telefono.trim() || null,
        avatarUrl: datos.avatarUrl.trim() || null,
      });
      await refrescar();
      setOkDatos('Tus datos se guardaron correctamente.');
    } catch (err) {
      const info = leerError(err);
      setErroresDatos((er) => ({ ...er, ...info.errores }));
      setErrorDatos(info.mensaje);
    } finally {
      setGuardando(false);
    }
  };

  const cambiarClave = (e) => {
    const { name, value } = e.target;
    const siguiente = { ...claves, [name]: value };
    setClaves(siguiente);
    setOkClave('');
    setErroresClaves((er) => {
      const nuevo = { ...er };
      if (tocadosClaves[name]) nuevo[name] = validarClave(name, value, siguiente);
      if (name === 'nueva' && tocadosClaves.confirmacion) {
        nuevo.confirmacion = validarClave('confirmacion', siguiente.confirmacion, siguiente);
      }
      return nuevo;
    });
  };

  const salirClave = (e) => {
    const { name, value } = e.target;
    setTocadosClaves((t) => ({ ...t, [name]: true }));
    setErroresClaves((er) => ({ ...er, [name]: validarClave(name, value, claves) }));
  };

  const pedirConfirmacion = (e) => {
    e.preventDefault();
    const nuevos = {
      actual: validarClave('actual', claves.actual, claves),
      nueva: validarClave('nueva', claves.nueva, claves),
      confirmacion: validarClave('confirmacion', claves.confirmacion, claves),
    };
    setTocadosClaves({ actual: true, nueva: true, confirmacion: true });
    setErroresClaves(nuevos);
    setOkClave('');
    setErrorClave('');
    if (Object.values(nuevos).some(Boolean)) return;
    setConfirmando(true);
  };

  const confirmarCambio = async () => {
    setCambiando(true);
    try {
      await cambiarPassword({ actual: claves.actual, nueva: claves.nueva });
      setClaves(CLAVES_VACIAS);
      setTocadosClaves({});
      setErroresClaves({});
      setOkClave('Tu contraseña se cambió correctamente.');
    } catch (err) {
      const info = leerError(err);
      setErroresClaves((er) => ({
        ...er,
        ...(info.errores.nueva ? { nueva: info.errores.nueva } : {}),
        ...(info.status === 409 && info.mensaje.includes('actual') ? { actual: info.mensaje } : {}),
      }));
      setErrorClave(info.mensaje);
    } finally {
      setCambiando(false);
      setConfirmando(false);
    }
  };

  return (
    <>
      <header className="cab">
        <div>
          <h1>Mi perfil</h1>
          <p>Actualiza tus datos y tu contraseña.</p>
        </div>
      </header>
      <section className="grid g2">
        <form className="card" onSubmit={guardarDatos} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <h2>Mis datos</h2>
          <div className="fila-g">
            <span className="av">
              {datos.avatarUrl && !erroresDatos.avatarUrl ? <img src={datos.avatarUrl} alt="" /> : iniciales(datos.nombre)}
            </span>
            <small>Vista previa de tu avatar</small>
          </div>
          <Field label="Correo" name="email" type="email" value={email} readOnly disabled />
          <Field label="Nombre completo" name="nombre" value={datos.nombre} onChange={cambiarDato} onBlur={salirDato}
            error={erroresDatos.nombre} />
          <Field label="Teléfono (10 dígitos, opcional)" name="telefono" type="tel" inputMode="numeric" maxLength={10}
            value={datos.telefono} onChange={cambiarDato} onBlur={salirDato} error={erroresDatos.telefono} />
          <Field label="Avatar (enlace a una imagen, opcional)" name="avatarUrl" placeholder="https://..."
            value={datos.avatarUrl} onChange={cambiarDato} onBlur={salirDato} error={erroresDatos.avatarUrl} />
          {okDatos ? <Mensaje tipo="ok">{okDatos}</Mensaje> : null}
          {errorDatos ? <Mensaje tipo="error">{errorDatos}</Mensaje> : null}
          <button type="submit" className="btn" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Guardar cambios'}
          </button>
        </form>

        <form className="card" onSubmit={pedirConfirmacion} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <h2>Cambiar contraseña</h2>
          <Field label="Contraseña actual" name="actual" type="password" autoComplete="current-password"
            value={claves.actual} onChange={cambiarClave} onBlur={salirClave} error={erroresClaves.actual} />
          <Field label="Contraseña nueva" name="nueva" type="password" autoComplete="new-password"
            value={claves.nueva} onChange={cambiarClave} onBlur={salirClave} error={erroresClaves.nueva} />
          <ul className="requisitos" aria-label="Requisitos de la contraseña">
            {requisitosPassword(claves.nueva).map((r) => (
              <li key={r.texto} className={r.cumple ? 'ok' : ''}>
                {r.cumple ? '✓' : '○'} {r.texto}
              </li>
            ))}
          </ul>
          <Field label="Confirma la contraseña nueva" name="confirmacion" type="password" autoComplete="new-password"
            value={claves.confirmacion} onChange={cambiarClave} onBlur={salirClave} error={erroresClaves.confirmacion} />
          {okClave ? <Mensaje tipo="ok">{okClave}</Mensaje> : null}
          {errorClave ? <Mensaje tipo="error">{errorClave}</Mensaje> : null}
          <button type="submit" className="btn sec" disabled={cambiando}>Cambiar contraseña</button>
        </form>
      </section>
      <ConfirmDialog
        abierto={confirmando}
        titulo="Cambiar contraseña"
        mensaje="¿Seguro que quieres cambiar tu contraseña? La usarás la próxima vez que inicies sesión."
        textoConfirmar="Sí, cambiarla"
        cargando={cambiando}
        onConfirmar={confirmarCambio}
        onCancelar={() => setConfirmando(false)}
      />
    </>
  );
}
