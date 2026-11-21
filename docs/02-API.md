# 02 · CONTRATO DE API

Base: `/api`. JSON en UTF-8. Todas las rutas requieren `Authorization: Bearer <token>` salvo las marcadas **público**. Los roles indicados incluyen siempre a ADMIN (ve todo) salvo que se diga lo contrario. "Pertenencia" = además de tener el rol, el recurso debe ser suyo.

## Formatos comunes
**Página** (todo listado): `?page=0&size=10` → 
```json
{ "content": [], "page": 0, "size": 10, "totalElements": 0, "totalPages": 0 }
```
**Error** (siempre este formato):
```json
{ "timestamp": "2026-11-15T10:00:00", "status": 422, "error": "Unprocessable Entity",
  "message": "Hay campos inválidos", "path": "/api/pedidos",
  "errores": { "clienteNombre": "El nombre es obligatorio" } }
```
`errores` solo aparece en 422. Códigos: 400 JSON mal formado · 401 sin token/token inválido/revocado/credenciales malas · 403 sin permiso · 404 no existe (o no es tuyo) · 409 conflicto de negocio · 422 validación · 500 inesperado.

**UsuarioDTO** `{ id, nombre, email, telefono, avatarUrl, activo, roles:["CLIENTE"], perfil }`
`perfil` = `{ negocioId, negocioNombre, tipoRepartidor }` (cada campo puede ser null; `tipoRepartidor`: `EMPRESA`, `INDEPENDIENTE` o null).

**PedidoDTO** `{ id, clienteNombre, clienteTelefono, direccion, zona, lat, lng, articulos, total, notas, origen, pagoRepartidor, modalidad, estado, negocio:{id,nombre}|null, repartidor:{id,nombre}|null, recorridoId, ordenParada, creadoEn }`
**PedidoRequest** `{ clienteNombre*, clienteTelefono, clienteEmail, direccion*, zona*, articulos*(>=1), total*(>=0), notas, origen, pagoRepartidor, modalidad }` (`*` obligatorio; `clienteEmail` enlaza `cliente_id` si existe una cuenta CLIENTE con ese correo).
**VehiculoDTO / Request** `{ id, tipo*, placa*, capacidad*(>=1), estado*, seguroVence, enUsoHoy, repartidorHoy }` (los dos últimos solo en respuesta).
**RecorridoDTO** `{ id, fecha, estado, repartidor:{id,nombre}, vehiculo:{id,tipo,placa}|null, total, entregados, paradas:[PedidoDTO ordenados por ordenParada] }`

## Autenticación (`/api/auth`)
| Método y ruta | Quién | Cuerpo → Respuesta |
|---|---|---|
| POST `/auth/register` | público | `{ nombre*, email*, password*, telefono, tipoCuenta*: CLIENTE\|NEGOCIO\|INDEPENDIENTE, nombreNegocio (obligatorio si NEGOCIO) }` → 201 `UsuarioDTO`. Crea rol, negocio o repartidor según el tipo. 409 si el email ya existe. Envía correo de bienvenida. |
| POST `/auth/login` | público | `{ email*, password* }` → 200 `{ token, tipo:"Bearer", expiraEn, usuario:UsuarioDTO }`. 401 si son incorrectas o el usuario está inactivo. |
| POST `/auth/logout` | autenticado | → 204. Guarda el `jti` en `tokens_revocados`; ese token ya no sirve (401). |
| POST `/auth/forgot-password` | público | `{ email* }` → 200 siempre (no revela si existe). Envía correo con enlace `{frontendUrl}/restablecer?token=...` (vence en 30 min, un solo uso). |
| POST `/auth/reset-password` | público | `{ token*, password* }` → 204. 409 si el token venció o ya se usó. |
| GET `/auth/me` | autenticado | → `UsuarioDTO` |

## Perfil (`/api/perfil`) — cualquier rol
- GET `/perfil` → `UsuarioDTO`
- PUT `/perfil` `{ nombre*, telefono, avatarUrl }` → `UsuarioDTO`
- PUT `/perfil/password` `{ actual*, nueva* }` → 204 (409 si la actual es incorrecta)

## Administración (`/api/admin`) — solo ADMIN
- GET `/admin/usuarios?q=&rol=&activo=&page=&size=` → página de `UsuarioDTO`
- POST `/admin/usuarios` `{ nombre*, email*, password*, telefono, rol*, nombreNegocio, negocioId }` → 201
- PUT `/admin/usuarios/{id}` `{ nombre*, telefono, rol }` → `UsuarioDTO`
- PATCH `/admin/usuarios/{id}/activo` `{ activo }` → `UsuarioDTO`
- DELETE `/admin/usuarios/{id}` → 204 (409 si es el único ADMIN)
- GET `/admin/notificaciones?canal=&estado=&page=&size=` → página de `{ id, canal, destino, evento, estado, detalle, creadoEn }`
- POST `/admin/notificaciones/prueba` `{ canal*, destino* }` → 200 `{ estado, detalle }` (envía un mensaje de prueba real; sirve para verificar configuración)

## Negocio (`/api/negocio`) — NEGOCIO
- GET `/negocio/resumen` → `{ sinAsignar, enCamino, entregadosHoy, mensajesNuevos, repartidoresHoy:[{ repartidorId, nombre, vehiculo, entregados, total }], atencion:[{ tipo:"NO_ENTREGADO"|"MENSAJE"|"SEGURO", titulo, detalle, referenciaId }] }`

### Equipo (`/api/equipo`) — NEGOCIO
- GET `/equipo?q=&page=&size=` → página de `{ id, usuario:UsuarioDTO, vehiculoHoy, entregadosHoy, totalHoy, estadoHoy: "EN_RUTA"|"LIBRE" }`
- POST `/equipo` `{ nombre*, email*, password*, telefono }` → 201 (crea usuario REPARTIDOR con `negocio_id` del negocio)
- PUT `/equipo/{repartidorId}` `{ nombre*, telefono }` → 200
- DELETE `/equipo/{repartidorId}` → 204 (desactiva la cuenta; 409 si tiene recorrido EN_CURSO)
- GET `/equipo/disponibles?fecha=` → lista corta `[{id,nombre}]` de repartidores sin recorrido ese día (para el selector de asignación)

### Vehículos (`/api/vehiculos`) — NEGOCIO (pertenencia)
- GET `/vehiculos?q=&tipo=&estado=&page=&size=` · GET `/vehiculos/{id}` · POST `/vehiculos` → 201 · PUT `/vehiculos/{id}` · DELETE `/vehiculos/{id}` → 204 (409 si tiene recorridos futuros)
- GET `/vehiculos/disponibles?fecha=` → `[{id,tipo,placa,capacidad,cupoLibre}]` ACTIVO y sin recorrido ese día.

## Pedidos (`/api/pedidos`)
Alcance de los listados según rol: NEGOCIO → los de su negocio · CLIENTE → los suyos (`cliente_id`) · REPARTIDOR → los de sus recorridos · ADMIN → todos.
- GET `/pedidos?q=&estado=&zona=&modalidad=&asignado=&page=&size=` → página de `PedidoDTO` (`q` busca en folio, cliente y dirección; `asignado=false` filtra los sin recorrido).
- POST `/pedidos` `PedidoRequest` → 201. NEGOCIO: modalidad NEGOCIO por defecto (o ABIERTO si la manda). CLIENTE: siempre ABIERTO, sin negocio, `cliente_id` = él. Calcula lat/lng por zona.
- GET `/pedidos/{id}` · PUT `/pedidos/{id}` (NEGOCIO dueño, solo si está PENDIENTE o ASIGNADO) · DELETE `/pedidos/{id}` → 204 (409 si EN_CAMINO o ENTREGADO)
- POST `/pedidos/asignar` `{ pedidoIds*:[1,2], repartidorId*, vehiculoId*, fecha* }` → 200 `RecorridoDTO`. NEGOCIO. Reglas (409 si falla): pedidos propios y PENDIENTE; repartidor del negocio y sin recorrido EN_CURSO ese día; vehículo ACTIVO del negocio; `paradas + nuevos <= capacidad`. Crea o reutiliza el recorrido (repartidor + fecha), pone los pedidos en ASIGNADO y los ordena por cercanía desde el último punto.
- POST `/pedidos/{id}/desasignar` → 200. NEGOCIO; solo ASIGNADO → PENDIENTE.
- POST `/pedidos/{id}/publicar` → 200. NEGOCIO; solo PENDIENTE: cambia modalidad a ABIERTO.
- PATCH `/pedidos/{id}/estado` `{ estado*: ENTREGADO|NO_ENTREGADO, motivo }` → 200 `PedidoDTO`. REPARTIDOR de ese recorrido, solo desde EN_CAMINO. ENTREGADO dispara WhatsApp; si todas las paradas terminan, el recorrido pasa a FINALIZADO.
- POST `/pedidos/{id}/reasignar` → 200. NEGOCIO; NO_ENTREGADO → PENDIENTE y se quita del recorrido.
- GET `/pedidos/{id}/seguimiento` → CLIENTE dueño (o NEGOCIO/ADMIN):
```json
{ "pedidoId": 1038, "estado": "EN_CAMINO", "negocio": {"id":1,"nombre":"Dulces del Valle"},
  "repartidor": {"nombre":"Luis Martínez","tipo":"EMPRESA"}, "paradasAntes": 2,
  "pasos": [ {"clave":"RECIBIDO","hecho":true,"fecha":"..."}, {"clave":"ASIGNADO","hecho":true,"fecha":null},
             {"clave":"EN_CAMINO","hecho":true,"fecha":null}, {"clave":"ENTREGADO","hecho":false,"fecha":null} ] }
```

## Entregas disponibles (`/api/disponibles`) — REPARTIDOR independiente
- GET `/disponibles?zona=&maxKm=&page=&size=` → página de `PedidoDTO` con modalidad ABIERTO, estado PENDIENTE y sin recorrido; incluye `distanciaKm` desde el punto de referencia (el Centro Histórico) — campo extra solo en este listado.
- POST `/disponibles/{pedidoId}/tomar` → 200 `RecorridoDTO` (recorrido de hoy del independiente; lo crea si no existe). 409 si ya lo tomaron (dos personas a la vez: gana la primera, usar `@Version` o actualización condicional).
- POST `/disponibles/{pedidoId}/soltar` → 200. Solo si es suyo y está ASIGNADO.

## Recorridos (`/api/recorridos`)
- GET `/recorridos?fecha=&estado=&page=&size=` → página de `RecorridoDTO`. NEGOCIO: de sus repartidores. REPARTIDOR: los suyos (Historial usa `estado=FINALIZADO`).
- GET `/recorridos/hoy` → REPARTIDOR: `RecorridoDTO` de hoy o 404 si no tiene.
- GET `/recorridos/{id}` → `RecorridoDTO` (pertenencia).
- POST `/recorridos/{id}/iniciar` → 200. REPARTIDOR dueño; recorrido PLANEADO con al menos una parada: pasa a EN_CURSO y sus pedidos ASIGNADO a EN_CAMINO (dispara un SMS por parada).
- PUT `/recorridos/{id}/orden` `{ pedidoIds*:[3,1,2] }` → 200. REPARTIDOR dueño (o NEGOCIO); debe contener exactamente las paradas del recorrido. El orden por cercanía lo calcula el frontend con `sortByNearest` y lo manda aquí.

## Mensajes (`/api/mensajes`) — NEGOCIO, REPARTIDOR independiente, CLIENTE
- GET `/mensajes/hilos?page=&size=` → página de `{ pedidoId, titulo:"Pedido #1038", contraparte:{id,nombre}, ultimoMensaje, fecha, noLeidos }`
- GET `/mensajes/pedidos/{pedidoId}?page=&size=` → página de `{ id, pedidoId, remitente:{id,nombre}, texto, mio, leido, creadoEn }` (orden cronológico; solo participantes, si no 404)
- POST `/mensajes/pedidos/{pedidoId}` `{ texto*(1-1000) }` → 201 `MensajeDTO`. El destinatario lo decide el backend (ver `00-CONTEXTO.md`).
- PATCH `/mensajes/pedidos/{pedidoId}/leido` → 204 (marca como leídos los dirigidos a mí)
- GET `/mensajes/no-leidos` → `{ total }`

## Referencias
- GET `/zonas` → `[{ nombre, lat, lng }]` (cualquier autenticado)

## Matriz de seguridad (SecurityFilterChain)
| Patrón | Acceso |
|---|---|
| `/api/auth/register`, `/login`, `/forgot-password`, `/reset-password` | público |
| `/api/admin/**` | ADMIN |
| `/api/negocio/**`, `/api/equipo/**`, `/api/vehiculos/**` | NEGOCIO, ADMIN |
| `/api/disponibles/**` | REPARTIDOR, ADMIN (el servicio exige `negocio_id` null para independientes) |
| `/api/recorridos/**` | NEGOCIO, REPARTIDOR, ADMIN |
| `/api/pedidos/**`, `/api/mensajes/**`, `/api/perfil/**`, `/api/zonas`, `/api/auth/me`, `/api/auth/logout` | autenticado (el servicio filtra por rol y pertenencia) |
