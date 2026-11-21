import { useState } from 'react';
import { leerError } from '../../api/errores';
import { actualizarVehiculo, crearVehiculo } from '../../api/vehiculosApi';
import Field from '../../components/ui/Field';
import { Mensaje } from '../../components/ui/Feedback';
import Modal from '../../components/ui/Modal';
import { ETIQUETA_TIPO_VEHICULO } from '../../utils/format';

function validar(campo, valor) {
  switch (campo) {
    case 'tipo':
      return valor ? '' : 'Elige el tipo de vehículo';
    case 'placa':
      return /^[A-Za-z0-9-]{3,20}$/.test(String(valor).trim())
        ? ''
        : 'La placa solo puede tener letras, números y guiones (3 a 20 caracteres)';
    case 'capacidad': {
      const n = Number(valor);
      if (valor === '' || !Number.isInteger(n)) return 'La capacidad debe ser un número entero';
      if (n < 1) return 'La capacidad debe ser al menos 1';
      return n > 999 ? 'La capacidad no puede ser mayor a 999' : '';
    }
    case 'estado':
      return valor ? '' : 'Elige el estado del vehículo';
    default:
      return '';
  }
}

export default function VehiculoFormModal({ vehiculo, onCerrar, onGuardado }) {
  const editando = Boolean(vehiculo);
  const [datos, setDatos] = useState({
    tipo: vehiculo?.tipo ?? 'MOTOCICLETA',
    placa: vehiculo?.placa ?? '',
    capacidad: String(vehiculo?.capacidad ?? 10),
    estado: vehiculo?.estado ?? 'ACTIVO',
    seguroVence: vehiculo?.seguroVence ?? '',
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
    const campos = ['tipo', 'placa', 'capacidad', 'estado'];
    const nuevos = {};
    campos.forEach((c) => {
      nuevos[c] = validar(c, datos[c]);
    });
    setTocados(Object.fromEntries(campos.map((c) => [c, true])));
    setErrores(nuevos);
    setErrorGeneral('');
    if (Object.values(nuevos).some(Boolean)) return;

    const cuerpo = {
      tipo: datos.tipo,
      placa: datos.placa.trim().toUpperCase(),
      capacidad: Number(datos.capacidad),
      estado: datos.estado,
      seguroVence: datos.seguroVence || null,
    };
    setGuardando(true);
    try {
      if (editando) await actualizarVehiculo(vehiculo.id, cuerpo);
      else await crearVehiculo(cuerpo);
      onGuardado(editando ? 'El vehículo se actualizó correctamente.' : 'El vehículo se registró correctamente.');
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
      titulo={editando ? 'Editar vehículo' : 'Registrar vehículo'}
      onCerrar={guardando ? undefined : onCerrar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCerrar} disabled={guardando}>
            Cancelar
          </button>
          <button type="submit" form="form-vehiculo" className="btn" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Guardar'}
          </button>
        </>
      }
    >
      <form id="form-vehiculo" className="form-vertical" onSubmit={enviar} noValidate>
        <Field as="select" label="Tipo de vehículo" name="tipo" value={datos.tipo} onChange={cambiar} onBlur={salir}
          error={errores.tipo}>
          {Object.entries(ETIQUETA_TIPO_VEHICULO).map(([valor, texto]) => (
            <option key={valor} value={valor}>{texto}</option>
          ))}
        </Field>
        <div className="form-2col">
          <Field label="Placa o identificador" name="placa" value={datos.placa} onChange={cambiar} onBlur={salir}
            error={errores.placa} placeholder="OAX-214" maxLength={20} />
          <Field label="Capacidad (paquetes)" name="capacidad" type="number" min="1" max="999" value={datos.capacidad}
            onChange={cambiar} onBlur={salir} error={errores.capacidad} />
        </div>
        <div className="form-2col">
          <Field as="select" label="Estado" name="estado" value={datos.estado} onChange={cambiar} onBlur={salir}
            error={errores.estado}>
            <option value="ACTIVO">Activo</option>
            <option value="MANTENIMIENTO">En mantenimiento</option>
          </Field>
          <Field label="Vencimiento del seguro (opcional)" name="seguroVence" type="date" value={datos.seguroVence}
            onChange={cambiar} error={errores.seguroVence} />
        </div>
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
      </form>
    </Modal>
  );
}
