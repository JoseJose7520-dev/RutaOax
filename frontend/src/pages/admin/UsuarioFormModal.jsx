import { useState } from 'react';
import { actualizarUsuario, crearUsuario } from '../../api/adminUsuariosApi';
import { leerError } from '../../api/errores';
import Field from '../../components/ui/Field';
import { Mensaje } from '../../components/ui/Feedback';
import Modal from '../../components/ui/Modal';
import {
  requisitosPassword,
  validarEmail,
  validarPasswordSegura,
  validarRequerido,
  validarTelefono,
} from '../../utils/validaciones';

export const TIPOS = [
  { valor: 'CLIENTE', texto: 'Cliente' },
  { valor: 'NEGOCIO', texto: 'Negocio' },
  { valor: 'INDEPENDIENTE', texto: 'Repartidor independiente' },
  { valor: 'ADMIN', texto: 'Administrador' },
];

export default function UsuarioFormModal({ usuario, onCerrar, onGuardado }) {
  const editando = Boolean(usuario);
  const [datos, setDatos] = useState({
    nombre: usuario?.nombre ?? '',
    email: usuario?.email ?? '',
    telefono: usuario?.telefono ?? '',
    tipo: 'CLIENTE',
    nombreNegocio: '',
    password: '',
  });
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [guardando, setGuardando] = useState(false);

  // Al editar, la contraseña es opcional: solo se valida si el administrador escribe una nueva
  const validar = (campo, valor, todos = datos) => {
    switch (campo) {
      case 'nombre':
        return validarRequerido(valor, 'El nombre');
      case 'email':
        return validarEmail(valor);
      case 'telefono':
        return validarTelefono(valor);
      case 'password':
        return editando && !valor ? '' : validarPasswordSegura(valor);
      case 'nombreNegocio':
        return todos.tipo === 'NEGOCIO' ? validarRequerido(valor, 'El nombre del negocio') : '';
      default:
        return '';
    }
  };

  const cambiar = (e) => {
    const { name, value } = e.target;
    const siguientes = { ...datos, [name]: value };
    setDatos(siguientes);
    if (tocados[name]) setErrores((er) => ({ ...er, [name]: validar(name, value, siguientes) }));
    if (name === 'tipo') setErrores((er) => ({ ...er, nombreNegocio: '' }));
  };

  const salir = (e) => {
    const { name, value } = e.target;
    setTocados((t) => ({ ...t, [name]: true }));
    setErrores((er) => ({ ...er, [name]: validar(name, value) }));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const campos = ['nombre', 'email', 'telefono', 'password', 'nombreNegocio'];
    const nuevos = {};
    campos.forEach((c) => {
      nuevos[c] = validar(c, datos[c]);
    });
    setTocados(Object.fromEntries(campos.map((c) => [c, true])));
    setErrores(nuevos);
    setErrorGeneral('');
    if (Object.values(nuevos).some(Boolean)) return;

    setGuardando(true);
    const base = {
      nombre: datos.nombre.trim(),
      email: datos.email.trim(),
      telefono: datos.telefono.trim() || null,
    };
    try {
      if (editando) {
        await actualizarUsuario(usuario.id, { ...base, nuevaPassword: datos.password || null });
        onGuardado(`Se guardaron los cambios de ${base.nombre}.`);
      } else {
        await crearUsuario({
          ...base,
          password: datos.password,
          tipo: datos.tipo,
          nombreNegocio: datos.tipo === 'NEGOCIO' ? datos.nombreNegocio.trim() : null,
        });
        onGuardado(`Se creó la cuenta de ${base.nombre}. Compártele su correo y su contraseña inicial.`);
      }
    } catch (err) {
      const info = leerError(err);
      const deServidor = { ...info.errores };
      if (deServidor.nuevaPassword) deServidor.password = deServidor.nuevaPassword;
      setErrores((er) => ({ ...er, ...deServidor }));
      setErrorGeneral(info.mensaje);
      setGuardando(false);
    }
  };

  return (
    <Modal
      abierto
      titulo={editando ? 'Editar usuario' : 'Nuevo usuario'}
      onCerrar={guardando ? undefined : onCerrar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </button>
          <button type="submit" form="form-usuario" className="btn" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Guardar'}
          </button>
        </>
      }
    >
      <form id="form-usuario" className="form-vertical" onSubmit={enviar} noValidate>
        {editando ? null : (
          <Field label="Tipo de cuenta" name="tipo" as="select" value={datos.tipo} onChange={cambiar}>
            {TIPOS.map((t) => (
              <option key={t.valor} value={t.valor}>{t.texto}</option>
            ))}
          </Field>
        )}
        <Field label="Nombre completo" name="nombre" value={datos.nombre} onChange={cambiar} onBlur={salir}
          error={errores.nombre} />
        <Field label="Correo" name="email" type="email" value={datos.email} onChange={cambiar} onBlur={salir}
          error={errores.email} />
        <Field label="Teléfono (10 dígitos, opcional)" name="telefono" type="tel" inputMode="numeric" maxLength={10}
          value={datos.telefono} onChange={cambiar} onBlur={salir} error={errores.telefono} />
        {!editando && datos.tipo === 'NEGOCIO' ? (
          <Field label="Nombre del negocio" name="nombreNegocio" value={datos.nombreNegocio} onChange={cambiar}
            onBlur={salir} error={errores.nombreNegocio} />
        ) : null}
        <Field label={editando ? 'Nueva contraseña (déjala vacía para no cambiarla)' : 'Contraseña inicial'}
          name="password" type="password" autoComplete="new-password" value={datos.password} onChange={cambiar}
          onBlur={salir} error={errores.password} />
        {!editando || datos.password ? (
          <ul className="requisitos" aria-label="Requisitos de la contraseña">
            {requisitosPassword(datos.password).map((r) => (
              <li key={r.texto} className={r.cumple ? 'ok' : ''}>
                {r.cumple ? '✓' : '○'} {r.texto}
              </li>
            ))}
          </ul>
        ) : null}
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
      </form>
    </Modal>
  );
}
