import http from './http';

export const listarHilos = (params) => http.get('/mensajes/hilos', { params }).then((r) => r.data);
export const obtenerConversacion = (pedidoId, params) =>
  http.get(`/mensajes/pedidos/${pedidoId}`, { params }).then((r) => r.data);
export const enviarMensaje = (pedidoId, texto) =>
  http.post(`/mensajes/pedidos/${pedidoId}`, { texto }).then((r) => r.data);
export const marcarLeido = (pedidoId) => http.patch(`/mensajes/pedidos/${pedidoId}/leido`);
