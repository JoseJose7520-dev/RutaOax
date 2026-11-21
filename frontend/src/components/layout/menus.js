export function menuPara(usuario) {
  const roles = usuario?.roles ?? [];
  const tipo = usuario?.perfil?.tipoRepartidor;
  if (roles.includes('ADMIN')) {
    return [
      { ruta: '/admin/usuarios', texto: 'Usuarios' },
      { ruta: '/admin/pedidos', texto: 'Pedidos' },
      { ruta: '/admin/vehiculos', texto: 'Vehículos' },
      { ruta: '/admin/notificaciones', texto: 'Notificaciones' },
    ];
  }
  if (roles.includes('NEGOCIO')) {
    return [
      { ruta: '/negocio', texto: 'Panel' },
      { ruta: '/negocio/pedidos', texto: 'Pedidos y asignación' },
      { ruta: '/negocio/equipo', texto: 'Equipo de reparto' },
      { ruta: '/negocio/vehiculos', texto: 'Vehículos' },
      { ruta: '/negocio/mensajes', texto: 'Mensajes' },
    ];
  }
  if (roles.includes('REPARTIDOR') && tipo === 'EMPRESA') {
    return [
      { ruta: '/repartidor', texto: 'Mi ruta de hoy' },
      { ruta: '/repartidor/historial', texto: 'Historial' },
    ];
  }
  if (roles.includes('REPARTIDOR')) {
    return [
      { ruta: '/independiente', texto: 'Entregas disponibles' },
      { ruta: '/independiente/ruta', texto: 'Mi ruta y mapa' },
      { ruta: '/independiente/mensajes', texto: 'Mensajes' },
    ];
  }
  return [
    { ruta: '/cliente', texto: 'Mi pedido' },
    { ruta: '/cliente/mensajes', texto: 'Mensajes' },
  ];
}

export function esIndependiente(usuario) {
  return (usuario?.roles ?? []).includes('REPARTIDOR') && usuario?.perfil?.tipoRepartidor !== 'EMPRESA';
}

export function etiquetaRol(usuario) {
  const roles = usuario?.roles ?? [];
  const negocio = usuario?.perfil?.negocioNombre;
  if (roles.includes('ADMIN')) return 'Administrador';
  if (roles.includes('NEGOCIO')) return negocio ? `Negocio · ${negocio}` : 'Negocio';
  if (roles.includes('REPARTIDOR')) {
    return usuario?.perfil?.tipoRepartidor === 'EMPRESA' ? `Repartidor · ${negocio ?? ''}` : 'Repartidor independiente';
  }
  return 'Cliente';
}
