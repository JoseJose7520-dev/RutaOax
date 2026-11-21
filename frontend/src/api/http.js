import axios from 'axios';

const CLAVE_TOKEN = 'rutaoax_token';

export function getToken() {
  return sessionStorage.getItem(CLAVE_TOKEN);
}

export function setToken(token) {
  sessionStorage.setItem(CLAVE_TOKEN, token);
}

export function clearSession() {
  sessionStorage.removeItem(CLAVE_TOKEN);
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const estado = error.response?.status;
    const url = error.config?.url || '';
    if (estado === 401 && getToken() && !url.includes('/auth/login')) {
      clearSession();
      if (window.location.pathname !== '/login') window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);

export default http;
