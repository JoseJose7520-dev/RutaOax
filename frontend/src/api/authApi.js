import http from './http';

export const login = (email, password) => http.post('/auth/login', { email, password }).then((r) => r.data);
export const register = (datos) => http.post('/auth/register', datos).then((r) => r.data);
export const logout = () => http.post('/auth/logout').then(() => undefined);
export const me = () => http.get('/auth/me').then((r) => r.data);
export const forgotPassword = (email) => http.post('/auth/forgot-password', { email }).then(() => undefined);
export const resetPassword = (token, password) =>
  http.post('/auth/reset-password', { token, password }).then(() => undefined);
