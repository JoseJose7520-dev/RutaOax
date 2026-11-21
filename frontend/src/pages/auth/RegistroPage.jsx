import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { leerError } from '../../api/errores';
import { useAuth } from '../../auth/AuthContext';
import { rutaInicio } from '../../auth/rutas';
import Field from '../../components/ui/Field';
import { Cargando, Mensaje } from '../../components/ui/Feedback';
import {
  requisitosPassword,
  validarEmail,
  validarPasswordSegura,
  validarRequerido,
  validarTelefono,
} from '../../utils/validaciones';
import AuthShell from './AuthShell';

const INICIAL = { nombre: '', email: '', telefono: '', password: '', tipoCuenta: 'CLIENTE', nombreNegocio: '' };

function validar(campo, valor, datos) {
  switch (campo) {
    case 'nombre':
      return validarRequerido(valor, 'El nombre');
    case 'email':
      return validarEmail(valor);
    case 'telefono':
      return validarTelefono(valor);
    case 'password':
      return validarPasswordSegura(valor);
    case 'nombreNegocio':
      return datos.tipoCuenta === 'NEGOCIO' ? validarRequerido(valor, 'El nombre del negocio') : '';
    default:
      return '';
  }
}

export default function RegistroPage() {
  const { usuario, cargando, registrar } = useAuth();
  const navigate = useNavigate();
  const [datos, setDatos] = useState(INICIAL);
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (cargando) return <Cargando />;
  if (usuario) return <Navigate to={rutaInicio(usuario)} replace />;

  const cambiar = (e) => {
    const { name, value } = e.target;
    const siguiente = { ...datos, [name]: value };
    setDatos(siguiente);
    if (tocados[name]) setErrores((er) => ({ ...er, [name]: validar(name, value, siguiente) }));
    if (name === 'tipoCuenta') setErrores((er) => ({ ...er, nombreNegocio: '' }));
  };

  const salir = (e) => {
    const { name, value } = e.target;
    setTocados((t) => ({ ...t, [name]: true }));
    setErrores((er) => ({ ...er, [name]: validar(name, value, datos) }));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const campos = ['nombre', 'email', 'telefono', 'password', 'nombreNegocio'];
    const nuevos = {};
    campos.forEach((c) => {
      nuevos[c] = validar(c, datos[c], datos);
    });
    setTocados(Object.fromEntries(campos.map((c) => [c, true])));
    setErrores(nuevos);
    setErrorGeneral('');
    if (Object.values(nuevos).some(Boolean)) return;

    setEnviando(true);
    try {
      await registrar({
        nombre: datos.nombre.trim(),
        email: datos.email.trim(),
        password: datos.password,
        telefono: datos.telefono.trim() || null,
        tipoCuenta: datos.tipoCuenta,
        nombreNegocio: datos.tipoCuenta === 'NEGOCIO' ? datos.nombreNegocio.trim() : null,
      });
      navigate('/login', { replace: true, state: { registroOk: true } });
    } catch (err) {
      const info = leerError(err);
      setErrores((er) => ({ ...er, ...info.errores }));
      setErrorGeneral(info.mensaje);
      setEnviando(false);
    }
  };

  return (
    <AuthShell>
      <h1>Crear una cuenta</h1>
      <p className="muted">Elige cómo vas a usar RutaOax.</p>
      <form className="card" onSubmit={enviar} noValidate>
        <Field as="select" label="Tipo de cuenta" name="tipoCuenta" value={datos.tipoCuenta} onChange={cambiar}
          error={errores.tipoCuenta}>
          <option value="CLIENTE">Quiero seguir mis pedidos</option>
          <option value="NEGOCIO">Tengo un negocio</option>
          <option value="INDEPENDIENTE">Soy repartidor independiente</option>
        </Field>
        {datos.tipoCuenta === 'NEGOCIO' ? (
          <Field label="Nombre del negocio" name="nombreNegocio" value={datos.nombreNegocio} onChange={cambiar}
            onBlur={salir} error={errores.nombreNegocio} />
        ) : null}
        <Field label="Nombre completo" name="nombre" autoComplete="name" value={datos.nombre} onChange={cambiar}
          onBlur={salir} error={errores.nombre} />
        <Field label="Correo" name="email" type="email" autoComplete="email" value={datos.email} onChange={cambiar}
          onBlur={salir} error={errores.email} />
        <Field label="Teléfono (10 dígitos, opcional)" name="telefono" type="tel" inputMode="numeric" maxLength={10}
          value={datos.telefono} onChange={cambiar} onBlur={salir} error={errores.telefono} />
        <Field label="Contraseña" name="password" type="password" autoComplete="new-password" value={datos.password}
          onChange={cambiar} onBlur={salir} error={errores.password} />
        <ul className="requisitos" aria-label="Requisitos de la contraseña">
          {requisitosPassword(datos.password).map((r) => (
            <li key={r.texto} className={r.cumple ? 'ok' : ''}>
              {r.cumple ? '✓' : '○'} {r.texto}
            </li>
          ))}
        </ul>
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
        <button type="submit" className="btn grande" disabled={enviando}>
          {enviando ? 'Creando cuenta...' : 'Crear cuenta'}
        </button>
      </form>
      <p className="centro muted">
        ¿Ya tienes cuenta? <Link className="enlace" to="/login">Iniciar sesión</Link>
      </p>
    </AuthShell>
  );
}
