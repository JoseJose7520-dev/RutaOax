import http from './http';

export const listarUsuarios = (params) => http.get('/admin/usuarios', { params }).then((r) => r.data);
export const crearUsuario = (datos) => http.post('/admin/usuarios', datos).then((r) => r.data);
export const actualizarUsuario = (id, datos) => http.put(`/admin/usuarios/${id}`, datos).then((r) => r.data);
export const cambiarEstadoUsuario = (id, activo) =>
  http.patch(`/admin/usuarios/${id}/activo`, { activo }).then((r) => r.data);
export const eliminarUsuario = (id) => http.delete(`/admin/usuarios/${id}`).then(() => undefined);
