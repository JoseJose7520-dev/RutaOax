import http from './http';

export const listarVehiculos = (params) => http.get('/vehiculos', { params }).then((r) => r.data);
export const crearVehiculo = (datos) => http.post('/vehiculos', datos).then((r) => r.data);
export const actualizarVehiculo = (id, datos) => http.put(`/vehiculos/${id}`, datos).then((r) => r.data);
export const eliminarVehiculo = (id) => http.delete(`/vehiculos/${id}`).then(() => undefined);
export const vehiculosDisponibles = (fecha) =>
  http.get('/vehiculos/disponibles', { params: fecha ? { fecha } : {} }).then((r) => r.data);
