import http from './http';

export const listarEquipo = (params) => http.get('/equipo', { params }).then((r) => r.data);
export const crearMiembro = (datos) => http.post('/equipo', datos).then((r) => r.data);
export const actualizarMiembro = (id, datos) => http.put(`/equipo/${id}`, datos).then((r) => r.data);
export const eliminarMiembro = (id) => http.delete(`/equipo/${id}`).then(() => undefined);
export const repartidoresDisponibles = (fecha) =>
  http.get('/equipo/disponibles', { params: fecha ? { fecha } : {} }).then((r) => r.data);
