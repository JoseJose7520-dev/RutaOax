import http from './http';

export const listarPedidos = (params) => http.get('/pedidos', { params }).then((r) => r.data);
export const obtenerPedido = (id) => http.get(`/pedidos/${id}`).then((r) => r.data);
export const crearPedido = (datos) => http.post('/pedidos', datos).then((r) => r.data);
export const actualizarPedido = (id, datos) => http.put(`/pedidos/${id}`, datos).then((r) => r.data);
export const eliminarPedido = (id) => http.delete(`/pedidos/${id}`).then(() => undefined);
export const asignarPedidos = (datos) => http.post('/pedidos/asignar', datos).then((r) => r.data);
export const desasignarPedido = (id) => http.post(`/pedidos/${id}/desasignar`).then((r) => r.data);
