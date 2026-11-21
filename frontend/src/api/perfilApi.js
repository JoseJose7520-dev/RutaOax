import http from './http';

export const getPerfil = () => http.get('/perfil').then((r) => r.data);
export const updatePerfil = (datos) => http.put('/perfil', datos).then((r) => r.data);
export const cambiarPassword = (datos) => http.put('/perfil/password', datos).then(() => undefined);
