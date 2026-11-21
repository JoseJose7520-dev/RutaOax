export default function Field({ label, name, as = 'input', error, children, ...resto }) {
  const Control = as;
  const idError = `${name}-error`;
  return (
    <div className="campo">
      <label htmlFor={name}>{label}</label>
      <Control
        id={name}
        name={name}
        className={error ? 'invalido' : ''}
        aria-invalid={error ? 'true' : 'false'}
        aria-describedby={error ? idError : undefined}
        {...resto}
      >
        {children}
      </Control>
      {error ? (
        <span className="error" id={idError} role="alert">
          {error}
        </span>
      ) : null}
    </div>
  );
}
