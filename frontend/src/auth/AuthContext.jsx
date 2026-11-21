import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authApi from '../api/authApi';
import { clearSession, getToken, setToken } from '../api/http';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(() => Boolean(getToken()));

  useEffect(() => {
    if (!getToken()) {
      setCargando(false);
      return;
    }
    authApi
      .me()
      .then(setUsuario)
      .catch(() => clearSession())
      .finally(() => setCargando(false));
  }, []);

  const login = useCallback(async (email, password) => {
    const respuesta = await authApi.login(email, password);
    setToken(respuesta.token);
    setUsuario(respuesta.usuario);
    return respuesta.usuario;
  }, []);

  const registrar = useCallback((datos) => authApi.register(datos), []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Si el servidor no responde igual se cierra la sesión en este navegador.
    }
    clearSession();
    setUsuario(null);
  }, []);

  const refrescar = useCallback(async () => {
    const actual = await authApi.me();
    setUsuario(actual);
    return actual;
  }, []);

  const tieneRol = useCallback((rol) => Boolean(usuario?.roles?.includes(rol)), [usuario]);

  const valor = useMemo(
    () => ({ usuario, cargando, login, registrar, logout, refrescar, tieneRol }),
    [usuario, cargando, login, registrar, logout, refrescar, tieneRol],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return contexto;
}
