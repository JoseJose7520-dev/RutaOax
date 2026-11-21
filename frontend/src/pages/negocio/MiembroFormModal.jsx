import { useState } from 'react';
import { leerError } from '../../api/errores';
import { actualizarMiembro, crearMiembro } from '../../api/equipoApi';
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

function validar(campo, valor) {
  switch (campo) {
    case 'nombre':
      return validarRequerido(valor, 'El nombre');
    case 'email':
      return validarEmail(valor);
    case 'telefono':
      return validarTelefono(valor);
    case 'password':
      return validarPasswordSegura(valor);
    default:
      return '';
  }
}

export default function MiembroFormModal({ miembro, onCerrar, onGuardado }) {
  const editando = Boolean(miembro);
  const [datos, setDatos] = useState({
    nombre: miembro?.usuario.nombre ?? '',
    email: miembro?.usuario.email ?? '',
    telefono: miembro?.usuario.telefono ?? '',
    password: '',
  });
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [guardando, setGuardando] = useState(false);

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
    const campos = editando ? ['nombre', 'telefono'] : ['nombre', 'email', 'telefono', 'password'];
    const nuevos = {};
    campos.forEach((c) => {
      nuevos[c] = validar(c, datos[c]);
    });
    setTocados(Object.fromEntries(campos.map((c) => [c, true])));
    setErrores(nuevos);
    setErrorGeneral('');
    if (Object.values(nuevos).some(Boolean)) return;

    setGuardando(true);
    try {
      if (editando) {
        await actualizarMiembro(miembro.id, { nombre: datos.nombre.trim(), telefono: datos.telefono.trim() || null });
        onGuardado('Los datos del repartidor se actualizaron correctamente.');
      } else {
        await crearMiembro({
          nombre: datos.nombre.trim(),
          email: datos.email.trim(),
          password: datos.password,
          telefono: datos.telefono.trim() || null,
        });
        onGuardado(`Se creó la cuenta de ${datos.nombre.trim()}. Compártele su correo y su contraseña inicial; podrá cambiarla desde su perfil.`);
      }
    } catch (err) {
      const info = leerError(err);
      setErrores((er) => ({ ...er, ...info.errores }));
      setErrorGeneral(info.mensaje);
      setGuardando(false);
    }
  };

  return (
    <Modal
      abierto
      titulo={editando ? 'Editar repartidor' : 'Agregar repartidor'}
      onCerrar={guardando ? undefined : onCerrar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </button>
          <button type="submit" form="form-miembro" className="btn" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Guardar'}
          </button>
        </>
      }
    >
      <form id="form-miembro" className="form-vertical" onSubmit={enviar} noValidate>
        <Field label="Nombre completo" name="nombre" value={datos.nombre} onChange={cambiar} onBlur={salir}
          error={errores.nombre} />
        <Field label="Correo" name="email" type="email" value={datos.email} onChange={cambiar} onBlur={salir}
          error={errores.email} readOnly={editando} disabled={editando} />
        <Field label="Teléfono (10 dígitos, opcional)" name="telefono" type="tel" inputMode="numeric" maxLength={10}
          value={datos.telefono} onChange={cambiar} onBlur={salir} error={errores.telefono} />
        {editando ? null : (
          <>
            <Field label="Contraseña inicial" name="password" type="password" autoComplete="new-password"
              value={datos.password} onChange={cambiar} onBlur={salir} error={errores.password} />
            <ul className="requisitos" aria-label="Requisitos de la contraseña">
              {requisitosPassword(datos.password).map((r) => (
                <li key={r.texto} className={r.cumple ? 'ok' : ''}>
                  {r.cumple ? '✓' : '○'} {r.texto}
                </li>
              ))}
            </ul>
          </>
        )}
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
      </form>
    </Modal>
  );
}
