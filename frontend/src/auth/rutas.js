export function rutaInicio(usuario) {
  const roles = usuario?.roles ?? [];
  if (roles.includes('ADMIN')) return '/admin/usuarios';
  if (roles.includes('NEGOCIO')) return '/negocio';
  if (roles.includes('REPARTIDOR')) {
    return usuario.perfil?.tipoRepartidor === 'EMPRESA' ? '/repartidor' : '/independiente';
  }
  if (roles.includes('CLIENTE')) return '/cliente';
  return '/login';
}
