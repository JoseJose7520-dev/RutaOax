const CLASES = {
  PENDIENTE: 'e-pend',
  ASIGNADO: 'e-cam',
  EN_CAMINO: 'e-cam',
  ENTREGADO: 'e-ok',
  NO_ENTREGADO: 'e-no',
};

const TEXTOS = {
  PENDIENTE: 'Sin asignar',
  ASIGNADO: 'Asignado',
  EN_CAMINO: 'En camino',
  ENTREGADO: 'Entregado',
  NO_ENTREGADO: 'No entregado',
};

export default function StatusBadge({ estado }) {
  return <span className={`etq ${CLASES[estado] ?? 'e-gris'}`}>{TEXTOS[estado] ?? estado}</span>;
}
