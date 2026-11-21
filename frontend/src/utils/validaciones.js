export const REGEX_PASSWORD = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
const REGEX_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validarRequerido(valor, nombre) {
  return String(valor ?? '').trim() ? '' : `${nombre} es obligatorio`;
}

export function validarEmail(valor) {
  const v = String(valor ?? '').trim();
  if (!v) return 'El correo es obligatorio';
  return REGEX_EMAIL.test(v) ? '' : 'El correo no tiene un formato válido';
}

export function validarPasswordSegura(valor) {
  const v = String(valor ?? '');
  if (!v) return 'La contraseña es obligatoria';
  return REGEX_PASSWORD.test(v)
    ? ''
    : 'La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial';
}

export function validarTelefono(valor) {
  const v = String(valor ?? '').trim();
  if (!v) return '';
  return /^\d{10}$/.test(v) ? '' : 'El teléfono debe tener 10 dígitos';
}

export function requisitosPassword(valor) {
  const v = String(valor ?? '');
  return [
    { texto: 'Mínimo 8 caracteres', cumple: v.length >= 8 },
    { texto: 'Una letra mayúscula', cumple: /[A-Z]/.test(v) },
    { texto: 'Un número', cumple: /\d/.test(v) },
    { texto: 'Un carácter especial (por ejemplo # ! $ %)', cumple: /[^A-Za-z0-9]/.test(v) },
  ];
}
