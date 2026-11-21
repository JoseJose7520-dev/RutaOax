import { useState } from 'react';
import { leerError } from '../../api/errores';
import { actualizarPedido, crearPedido } from '../../api/pedidosApi';
import Field from '../../components/ui/Field';
import { Mensaje } from '../../components/ui/Feedback';
import Modal from '../../components/ui/Modal';
import { validarEmail, validarRequerido, validarTelefono } from '../../utils/validaciones';

function validar(campo, valor, datos) {
  switch (campo) {
    case 'clienteNombre':
      return validarRequerido(valor, 'El nombre del cliente');
    case 'clienteTelefono':
      return validarTelefono(valor);
    case 'clienteEmail':
      return String(valor).trim() ? validarEmail(valor) : '';
    case 'direccion':
      return validarRequerido(valor, 'La dirección');
    case 'zona':
      return valor ? '' : 'Elige una zona de entrega';
    case 'articulos': {
      const n = Number(valor);
      if (valor === '' || !Number.isInteger(n)) return 'El número de artículos debe ser un entero';
      return n < 1 ? 'Debe haber al menos 1 artículo' : '';
    }
    case 'total': {
      if (valor === '') return 'El total es obligatorio';
      const n = Number(valor);
      if (Number.isNaN(n) || n < 0) return 'El total no puede ser negativo';
      return /^\d+(\.\d{1,2})?$/.test(String(valor)) ? '' : 'El total debe tener máximo 2 decimales';
    }
    case 'pagoRepartidor': {
      if (!datos.publicar || valor === '') return '';
      const n = Number(valor);
      if (Number.isNaN(n) || n < 0) return 'El pago no puede ser negativo';
      return /^\d+(\.\d{1,2})?$/.test(String(valor)) ? '' : 'El pago debe tener máximo 2 decimales';
    }
    default:
      return '';
  }
}

const CAMPOS = ['clienteNombre', 'clienteTelefono', 'clienteEmail', 'direccion', 'zona', 'articulos', 'total', 'pagoRepartidor'];

export default function PedidoFormModal({ pedido, zonas, onCerrar, onGuardado }) {
  const editando = Boolean(pedido);
  const [datos, setDatos] = useState({
    clienteNombre: pedido?.clienteNombre ?? '',
    clienteTelefono: pedido?.clienteTelefono ?? '',
    clienteEmail: '',
    direccion: pedido?.direccion ?? '',
    zona: pedido?.zona ?? '',
    articulos: String(pedido?.articulos ?? 1),
    total: pedido ? String(pedido.total) : '',
    notas: pedido?.notas ?? '',
    publicar: pedido?.modalidad === 'ABIERTO',
    origen: pedido?.origen ?? '',
    pagoRepartidor: pedido?.pagoRepartidor != null ? String(pedido.pagoRepartidor) : '',
  });
  const [tocados, setTocados] = useState({});
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState('');
  const [guardando, setGuardando] = useState(false);

  const cambiar = (e) => {
    const { name, value, type, checked } = e.target;
    const nuevoValor = type === 'checkbox' ? checked : value;
    const siguiente = { ...datos, [name]: nuevoValor };
    setDatos(siguiente);
    if (tocados[name]) setErrores((er) => ({ ...er, [name]: validar(name, nuevoValor, siguiente) }));
  };

  const salir = (e) => {
    const { name, value } = e.target;
    setTocados((t) => ({ ...t, [name]: true }));
    setErrores((er) => ({ ...er, [name]: validar(name, value, datos) }));
  };

  const enviar = async (e) => {
    e.preventDefault();
    const nuevos = {};
    CAMPOS.forEach((c) => {
      nuevos[c] = validar(c, datos[c], datos);
    });
    setTocados(Object.fromEntries(CAMPOS.map((c) => [c, true])));
    setErrores(nuevos);
    setErrorGeneral('');
    if (Object.values(nuevos).some(Boolean)) return;

    const cuerpo = {
      clienteNombre: datos.clienteNombre.trim(),
      clienteTelefono: datos.clienteTelefono.trim() || null,
      clienteEmail: datos.clienteEmail.trim() || null,
      direccion: datos.direccion.trim(),
      zona: datos.zona,
      articulos: Number(datos.articulos),
      total: Number(datos.total),
      notas: datos.notas.trim() || null,
      modalidad: datos.publicar ? 'ABIERTO' : 'NEGOCIO',
      origen: datos.publicar ? datos.origen.trim() || null : null,
      pagoRepartidor: datos.publicar && datos.pagoRepartidor !== '' ? Number(datos.pagoRepartidor) : null,
    };
    setGuardando(true);
    try {
      if (editando) await actualizarPedido(pedido.id, cuerpo);
      else await crearPedido(cuerpo);
      onGuardado(editando ? `El pedido #${pedido.id} se actualizó correctamente.` : 'El pedido se creó correctamente.');
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
      titulo={editando ? `Editar pedido #${pedido.id}` : 'Nuevo pedido'}
      onCerrar={guardando ? undefined : onCerrar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCerrar} disabled={guardando}>Cancelar</button>
          <button type="submit" form="form-pedido" className="btn" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Guardar'}
          </button>
        </>
      }
    >
      <form id="form-pedido" className="form-vertical" onSubmit={enviar} noValidate>
        <Field label="Nombre del cliente" name="clienteNombre" value={datos.clienteNombre} onChange={cambiar}
          onBlur={salir} error={errores.clienteNombre} />
        <div className="form-2col">
          <Field label="Teléfono (10 dígitos, opcional)" name="clienteTelefono" type="tel" inputMode="numeric" maxLength={10}
            value={datos.clienteTelefono} onChange={cambiar} onBlur={salir} error={errores.clienteTelefono} />
          <Field label="Correo del cliente (opcional)" name="clienteEmail" type="email" value={datos.clienteEmail}
            onChange={cambiar} onBlur={salir} error={errores.clienteEmail} placeholder="si tiene cuenta, verá su pedido" />
        </div>
        <Field label="Dirección de entrega" name="direccion" value={datos.direccion} onChange={cambiar} onBlur={salir}
          error={errores.direccion} />
        <div className="form-2col">
          <Field as="select" label="Zona" name="zona" value={datos.zona} onChange={cambiar} onBlur={salir} error={errores.zona}>
            <option value="">Elige una zona</option>
            {zonas.map((z) => (
              <option key={z.nombre} value={z.nombre}>{z.nombre}</option>
            ))}
          </Field>
          <Field label="Artículos" name="articulos" type="number" min="1" value={datos.articulos} onChange={cambiar}
            onBlur={salir} error={errores.articulos} />
        </div>
        <Field label="Total ($)" name="total" type="number" min="0" step="0.01" value={datos.total} onChange={cambiar}
          onBlur={salir} error={errores.total} />
        <Field as="textarea" label="Notas (opcional)" name="notas" rows={2} maxLength={255} value={datos.notas}
          onChange={cambiar} error={errores.notas} />
        {editando && pedido.modalidad !== 'ABIERTO' ? null : (
          <label className="fila-g">
            <input type="checkbox" name="publicar" checked={datos.publicar} onChange={cambiar} disabled={editando} />
            <span>Publicar para repartidores independientes</span>
          </label>
        )}
        {datos.publicar ? (
          <div className="form-2col">
            <Field label="Punto de recolección (opcional)" name="origen" value={datos.origen} onChange={cambiar}
              maxLength={150} error={errores.origen} />
            <Field label="Pago al repartidor ($, opcional)" name="pagoRepartidor" type="number" min="0" step="0.01"
              value={datos.pagoRepartidor} onChange={cambiar} onBlur={salir} error={errores.pagoRepartidor} />
          </div>
        ) : null}
        {errorGeneral ? <Mensaje tipo="error">{errorGeneral}</Mensaje> : null}
      </form>
    </Modal>
  );
}
