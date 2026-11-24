import { useState } from 'react';

function IconoOjo({ tachado }) {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" />
      <circle cx="12" cy="12" r="3" />
      {tachado ? <line x1="3" y1="3" x2="21" y2="21" /> : null}
    </svg>
  );
}

export default function Field({ label, name, as = 'input', error, children, type, ...resto }) {
  const Control = as;
  const idError = `${name}-error`;
  const esPassword = as === 'input' && type === 'password';
  // En los campos de contraseña, el boton del ojo alterna entre ocultar y mostrar lo escrito
  const [visible, setVisible] = useState(false);

  const control = (
    <Control
      id={name}
      name={name}
      type={esPassword && visible ? 'text' : type}
      className={error ? 'invalido' : ''}
      aria-invalid={error ? 'true' : 'false'}
      aria-describedby={error ? idError : undefined}
      {...resto}
    >
      {children}
    </Control>
  );

  return (
    <div className="campo">
      <label htmlFor={name}>{label}</label>
      {esPassword ? (
        <div className="con-ojo">
          {control}
          <button
            type="button"
            className="ojo"
            onClick={() => setVisible((v) => !v)}
            aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            aria-pressed={visible}
            title={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          >
            <IconoOjo tachado={visible} />
          </button>
        </div>
      ) : (
        control
      )}
      {error ? (
        <span className="error" id={idError} role="alert">
          {error}
        </span>
      ) : null}
    </div>
  );
}
