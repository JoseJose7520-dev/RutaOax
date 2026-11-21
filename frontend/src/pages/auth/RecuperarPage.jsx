import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../api/authApi';
import { leerError } from '../../api/errores';
import Field from '../../components/ui/Field';
import { Mensaje } from '../../components/ui/Feedback';
import { validarEmail } from '../../utils/validaciones';
import AuthShell from './AuthShell';

export default function RecuperarPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [errorGeneral, setErrorGeneral] = useState('');
  const [enviado, setEnviado] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [tocado, setTocado] = useState(false);

  const cambiar = (e) => {
    setEmail(e.target.value);
    if (tocado) setError(validarEmail(e.target.value));
  };

  const salir = () => {
    setTocado(true);
    setError(validarEmail(email));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const mensaje = validarEmail(email);
    setTocado(true);
    setError(mensaje);
    setErrorGeneral('');
    if (mensaje) return;

    setEnviando(true);
    try {
      await forgotPassword(email.trim());
      setEnviado(true);
    } catch (err) {
      const info = leerError(err);
      setError(info.errores.email || '');
      setErrorGeneral(info.mensaje);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <AuthShell>
      <h1>Recuperar contraseña</h1>
      <p className="muted">Escribe tu correo y te enviaremos un enlace para crear una contraseña nueva.</p>
      {enviado ? (
        <>
          <Mensaje tipo="ok">
            Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña. Revisa tu bandeja de
            entrada y la carpeta de spam. El enlace vence en 30 minutos.
          </Mensaje>
          <Link className="btn grande" to="/login">Volver a iniciar sesión</Link>
        </>
      ) : (
        <form className="card" onSubmit={enviar} noValidate>
          <Field label="Correo" name="email" type="email" autoComplete="email" placeholder="tucorreo@ejemplo.com"
            value={email} onChange={cambiar} onBlur={salir} error={error} />
          {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
          <button type="submit" className="btn grande" disabled={enviando}>
            {enviando ? 'Enviando...' : 'Enviar enlace'}
          </button>
          <p className="centro">
            <Link className="enlace" to="/login">Volver a iniciar sesión</Link>
          </p>
        </form>
      )}
    </AuthShell>
  );
}
