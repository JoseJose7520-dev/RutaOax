import http from './http';

export const listarZonas = () => http.get('/zonas').then((r) => r.data);
