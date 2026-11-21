export function leerError(err) {
  if (!err || !err.response) {
    return {
      status: 0,
      mensaje: 'No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo.',
      errores: {},
      esRed: true,
    };
  }
  const datos = err.response.data || {};
  return {
    status: err.response.status,
    mensaje: datos.message || 'Ocurrió un error inesperado. Intenta de nuevo.',
    errores: datos.errores || {},
    esRed: false,
  };
}
