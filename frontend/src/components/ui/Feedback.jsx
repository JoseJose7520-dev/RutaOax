export function Cargando({ mensaje = 'Cargando...' }) {
  return (
    <div className="cargando" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{mensaje}</span>
    </div>
  );
}

export function ErrorRed({ mensaje = 'No se pudo conectar con el servidor.', onReintentar }) {
  return (
    <div className="mensaje error" role="alert">
      <p>{mensaje}</p>
      {onReintentar ? (
        <button type="button" className="btn sec sm" onClick={onReintentar} style={{ marginTop: 8 }}>
          Reintentar
        </button>
      ) : null}
    </div>
  );
}

export function Vacio({ mensaje = 'No hay información para mostrar.' }) {
  return <div className="vacio">{mensaje}</div>;
}

export function Mensaje({ tipo = 'ok', children }) {
  return (
    <div className={`mensaje ${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  );
}
