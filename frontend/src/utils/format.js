export function iniciales(nombre) {
  const partes = String(nombre ?? '').trim().split(/\s+/).filter(Boolean);
  if (partes.length === 0) return '?';
  return (partes[0][0] + (partes[1] ? partes[1][0] : '')).toUpperCase();
}

export function fechaLarga(fecha = new Date()) {
  const texto = fecha.toLocaleDateString('es-MX', { weekday: 'long', day: 'numeric', month: 'long' });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
