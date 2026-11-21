import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../../api/authApi';
import { leerError } from '../../api/errores';
import Field from '../../components/ui/Field';
import { Mensaje } from '../../components/ui/Feedback';
import { requisitosPassword, validarPasswordSegura } from '../../utils/validaciones';
import AuthShell from './AuthShell';

function validar(campo, valor, datos) {
  if (campo === 'password') return validarPasswordSegura(valor);
  if (!valor) return 'Confirma tu contraseña';
  return valor === datos.password ? '' : 'Las contraseñas no coinciden';
}

export default function RestablecerPage() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const [datos, setDatos] = useState({ password: '', confirmacion: '' });
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [enlaceVencido, setEnlaceVencido] = useState(false);
  const [listo, setListo] = useState(false);
  const [enviando, setEnviando] = useState(false);

  if (!token) {
    return (
      <AuthShell>
        <h1>Restablecer contraseña</h1>
        <Mensaje tipo="error">El enlace de recuperación no es válido.</Mensaje>
        <Link className="btn grande" to="/recuperar">Pedir un enlace nuevo</Link>
      </AuthShell>
    );
  }

  if (listo) {
    return (
      <AuthShell>
        <h1>Contraseña actualizada</h1>
        <Mensaje tipo="ok">Tu contraseña se cambió correctamente. Ya puedes iniciar sesión con la nueva.</Mensaje>
        <Link className="btn grande" to="/login">Iniciar sesión</Link>
      </AuthShell>
    );
  }

  const cambiar = (e) => {
    const { name, value } = e.target;
    const siguiente = { ...datos, [name]: value };
    setDatos(siguiente);
    setErrores((er) => {
      const nuevo = { ...er };
      if (tocados[name]) nuevo[name] = validar(name, value, siguiente);
      if (name === 'password' && tocados.confirmacion) nuevo.confirmacion = validar('confirmacion', siguiente.confirmacion, siguiente);
      return nuevo;
    });
  };

  const salir = (e) => {
    const { name, value } = e.target;
    setTocados((t) => ({ ...t, [name]: true }));
    setErrores((er) => ({ ...er, [name]: validar(name, value, datos) }));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const nuevos = {
      password: validar('password', datos.password, datos),
      confirmacion: validar('confirmacion', datos.confirmacion, datos),
    };
    setTocados({ password: true, confirmacion: true });
    setErrores(nuevos);
    setErrorGeneral('');
    if (nuevos.password || nuevos.confirmacion) return;

    setEnviando(true);
    try {
      await resetPassword(token, datos.password);
      setListo(true);
    } catch (err) {
      const info = leerError(err);
      if (info.status === 409) {
        setEnlaceVencido(true);
      } else {
        setErrores((er) => ({ ...er, ...info.errores }));
      }
      setErrorGeneral(info.mensaje);
      setEnviando(false);
    }
  };

  return (
    <AuthShell>
      <h1>Crea tu contraseña nueva</h1>
      <p className="muted">Elige una contraseña segura que no uses en otros sitios.</p>
      <form className="card" onSubmit={enviar} noValidate>
        <Field label="Contraseña nueva" name="password" type="password" autoComplete="new-password"
          value={datos.password} onChange={cambiar} onBlur={salir} error={errores.password} />
        <ul className="requisitos" aria-label="Requisitos de la contraseña">
          {requisitosPassword(datos.password).map((r) => (
            <li key={r.texto} className={r.cumple ? 'ok' : ''}>
              {r.cumple ? '✓' : '○'} {r.texto}
            </li>
          ))}
        </ul>
        <Field label="Confirma la contraseña" name="confirmacion" type="password" autoComplete="new-password"
          value={datos.confirmacion} onChange={cambiar} onBlur={salir} error={errores.confirmacion} />
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
        {enlaceVencido ? (
          <Link className="btn sec" to="/recuperar">Pedir un enlace nuevo</Link>
        ) : (
          <button type="submit" className="btn grande" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar contraseña'}
          </button>
        )}
      </form>
    </AuthShell>
  );
}
