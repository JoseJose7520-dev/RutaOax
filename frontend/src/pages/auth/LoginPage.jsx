import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { useAuth } from '../../auth/AuthContext';
import { rutaInicio } from '../../auth/rutas';
import Field from '../../components/ui/Field';
import { Cargando, Mensaje } from '../../components/ui/Feedback';
import { validarEmail, validarRequerido } from '../../utils/validaciones';
import AuthShell from './AuthShell';

function validar(campo, valor) {
  if (campo === 'email') return validarEmail(valor);
  return validarRequerido(valor, 'La contraseña');
}

export default function LoginPage() {
  const { usuario, cargando, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [datos, setDatos] = useState({ email: '', password: '' });
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (cargando) return <Cargando />;
  if (usuario) return <Navigate to={rutaInicio(usuario)} replace />;

  const cambiar = (e) => {
    const { name, value } = e.target;
    setDatos((d) => ({ ...d, [name]: value }));
    if (tocados[name]) setErrores((er) => ({ ...er, [name]: validar(name, value) }));
  };

  const salir = (e) => {
    const { name, value } = e.target;
    setTocados((t) => ({ ...t, [name]: true }));
    setErrores((er) => ({ ...er, [name]: validar(name, value) }));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const nuevos = { email: validar('email', datos.email), password: validar('password', datos.password) };
    setTocados({ email: true, password: true });
    setErrores(nuevos);
    setErrorGeneral('');
    if (nuevos.email || nuevos.password) return;

    setEnviando(true);
    try {
      const u = await login(datos.email.trim(), datos.password);
      navigate(rutaInicio(u), { replace: true });
    } catch (err) {
      const info = leerError(err);
      setErrores((er) => ({ ...er, ...info.errores }));
      setErrorGeneral(info.mensaje);
      setEnviando(false);
    }
  };

  return (
    <AuthShell>
      <h1>Entrar a RutaOax</h1>
      <p className="muted">Usa tu correo y tu contraseña.</p>
      {location.state?.registroOk ? <Mensaje tipo="ok">Tu cuenta se creó correctamente. Ya puedes iniciar sesión.</Mensaje> : null}
      <form className="card" onSubmit={enviar} noValidate>
        <Field label="Correo" name="email" type="email" autoComplete="email" placeholder="tucorreo@ejemplo.com"
          value={datos.email} onChange={cambiar} onBlur={salir} error={errores.email} />
        <Field label="Contraseña" name="password" type="password" autoComplete="current-password" placeholder="••••••••"
          value={datos.password} onChange={cambiar} onBlur={salir} error={errores.password} />
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
        <button type="submit" className="btn grande" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
        <p className="centro">
          <Link className="enlace" to="/recuperar">¿Olvidaste tu contraseña?</Link>
        </p>
      </form>
      <p className="centro muted">
        ¿Aún no tienes cuenta? <Link className="enlace" to="/registro">Crear una cuenta</Link>
      </p>
    </AuthShell>
  );
}
