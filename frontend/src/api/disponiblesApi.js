import http from './http';

export const listarDisponibles = (params) => http.get('/disponibles', { params }).then((r) => r.data);
export const tomarEntrega = (pedidoId) => http.post(`/disponibles/${pedidoId}/tomar`).then((r) => r.data);
export const soltarEntrega = (pedidoId) => http.post(`/disponibles/${pedidoId}/soltar`);
