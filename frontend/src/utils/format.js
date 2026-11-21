export function iniciales(nombre) {
  const partes = String(nombre ?? '').trim().split(/\s+/).filter(Boolean);
  if (partes.length === 0) return '?';
  return (partes[0][0] + (partes[1] ? partes[1][0] : '')).toUpperCase();
}

export function fechaLarga(fecha = new Date()) {
  const texto = fecha.toLocaleDateString('es-MX', { weekday: 'long', day: 'numeric', month: 'long' });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function leerFechaIso(iso) {
  const [a, m, d] = String(iso).split('-').map(Number);
  return new Date(a, m - 1, d);
}

export function fechaCorta(iso) {
  if (!iso) return '';
  return leerFechaIso(iso).toLocaleDateString('es-MX', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function diasHasta(iso) {
  if (!iso) return null;
  const hoy = new Date();
  const inicioHoy = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  return Math.round((leerFechaIso(iso) - inicioHoy) / 86400000);
}

export const ETIQUETA_TIPO_VEHICULO = {
  COMBI: 'Combi',
  MOTOCICLETA: 'Motocicleta',
  CAMIONETA: 'Camioneta',
  BICICLETA: 'Bicicleta',
  AUTOMOVIL: 'Automóvil',
};

export function hoyIso() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

export function moneda(valor) {
  return Number(valor ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' });
}
