import http from './http';

export const obtenerResumen = () => http.get('/negocio/resumen').then((r) => r.data);
