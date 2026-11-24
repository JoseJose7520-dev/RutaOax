import http from './http';

// Catalogo de negocios para filtros y formularios del administrador
export const listarNegociosAdmin = () => http.get('/admin/negocios').then((r) => r.data);

export const listarPedidosAdmin = (params) => http.get('/admin/pedidos', { params }).then((r) => r.data);
export const crearPedidoAdmin = (negocioId, datos) =>
  http.post('/admin/pedidos', datos, { params: { negocioId } }).then((r) => r.data);

export const listarVehiculosAdmin = (params) => http.get('/admin/vehiculos', { params }).then((r) => r.data);
export const crearVehiculoAdmin = (negocioId, datos) =>
  http.post('/admin/vehiculos', datos, { params: { negocioId } }).then((r) => r.data);

export const listarNotificaciones = (params) => http.get('/admin/notificaciones', { params }).then((r) => r.data);
export const enviarNotificacionPrueba = (datos) => http.post('/admin/notificaciones/prueba', datos).then((r) => r.data);
