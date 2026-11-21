import { useEffect } from 'react';

export default function Modal({ abierto, titulo, onCerrar, pie, children }) {
  useEffect(() => {
    if (!abierto) return undefined;
    const alTeclear = (e) => {
      if (e.key === 'Escape') onCerrar?.();
    };
    document.addEventListener('keydown', alTeclear);
    return () => document.removeEventListener('keydown', alTeclear);
  }, [abierto, onCerrar]);

  if (!abierto) return null;
  return (
    <div className="modal-fondo" onMouseDown={(e) => e.target === e.currentTarget && onCerrar?.()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo}>
        <header>
          <h2>{titulo}</h2>
        </header>
        <div className="cuerpo">{children}</div>
        {pie ? <footer>{pie}</footer> : null}
      </div>
    </div>
  );
}
