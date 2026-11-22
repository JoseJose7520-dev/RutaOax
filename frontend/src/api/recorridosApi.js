import http from './http';

export const obtenerRutaHoy = () => http.get('/recorridos/hoy').then((r) => r.data);
export const obtenerRecorrido = (id) => http.get(`/recorridos/${id}`).then((r) => r.data);
export const listarRecorridos = (params) => http.get('/recorridos', { params }).then((r) => r.data);
export const iniciarRecorrido = (id) => http.post(`/recorridos/${id}/iniciar`).then((r) => r.data);
export const marcarEstado = (pedidoId, datos) => http.patch(`/pedidos/${pedidoId}/estado`, datos).then((r) => r.data);
