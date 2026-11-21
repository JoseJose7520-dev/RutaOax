import Modal from './Modal';

export default function ConfirmDialog({
  abierto,
  titulo = 'Confirmar acción',
  mensaje,
  textoConfirmar = 'Confirmar',
  peligroso = false,
  cargando = false,
  onConfirmar,
  onCancelar,
}) {
  return (
    <Modal
      abierto={abierto}
      titulo={titulo}
      onCerrar={cargando ? undefined : onCancelar}
      pie={
        <>
          <button type="button" className="btn sec" onClick={onCancelar} disabled={cargando}>
            Cancelar
          </button>
          <button type="button" className={`btn ${peligroso ? 'peligro' : ''}`} onClick={onConfirmar} disabled={cargando}>
            {cargando ? 'Procesando...' : textoConfirmar}
          </button>
        </>
      }
    >
      <p>{mensaje}</p>
    </Modal>
  );
}
