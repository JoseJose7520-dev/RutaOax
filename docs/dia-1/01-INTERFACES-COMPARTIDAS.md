# DÍA 1 · INTERFACES COMPARTIDAS (congeladas)

Estas son las clases, métodos y archivos que **todas las IA deben usar con exactamente estos nombres**. Es lo que evita que el chat de la mañana y el de la noche generen código incompatible.
Si una tarea necesita cambiar algo de aquí: primero se edita este archivo, se anota en `06-ESTADO.md` → "Decisiones", y después se corrige el código.

Adjunta solo la sección que toca (§1 a §4 backend, §5 a §7 frontend).

---
## BACKEND

### §1 Paquetes, dependencias y convenciones
- `groupId` `com.rutaoax`, `artifactId` `rutaoax-backend`, paquete base `com.rutaoax`, Java 21, Maven. **Sin Lombok** (getters y setters explícitos; DTOs como `record`).
- Dependencias: `spring-boot-starter-web`, `-security`, `-data-jpa`, `-validation`, `-mail`, `flyway-core` y `flyway-mysql`, `mysql-connector-j`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.6).
- Puerto `9250`. `spring.jpa.hibernate.ddl-auto=none`, `spring.jpa.open-in-view=false`.
- Cada módulo vive en su paquete (`com.rutaoax.usuario`, `com.rutaoax.auth`, …). Entidades y enums en la raíz del módulo; DTOs en `dto/`.
- Enums (paquete → valores):
  - `usuario.RolNombre`: `ADMIN, NEGOCIO, REPARTIDOR, CLIENTE`
  - `vehiculo.TipoVehiculo`: `COMBI, MOTOCICLETA, CAMIONETA, BICICLETA, AUTOMOVIL` · `vehiculo.EstadoVehiculo`: `ACTIVO, MANTENIMIENTO`
  - `recorrido.EstadoRecorrido`: `PLANEADO, EN_CURSO, FINALIZADO`
  - `pedido.Modalidad`: `NEGOCIO, ABIERTO` · `pedido.EstadoPedido`: `PENDIENTE, ASIGNADO, EN_CAMINO, ENTREGADO, NO_ENTREGADO`
  - `notificacion.Canal`: `EMAIL, SMS, WHATSAPP` · `notificacion.EstadoEnvio`: `ENVIADO, FALLIDO`
  - Todos con `@Enumerated(EnumType.STRING)`.

### §2 Entidades y repositorios (nombres exactos)
| Tabla | Clase | Paquete | Notas |
|---|---|---|---|
| roles | `Rol` | usuario | `id`, `RolNombre nombre` |
| usuarios | `Usuario` | usuario | `id, nombre, email, passwordHash, telefono, avatarUrl, activo, creadoEn, Set<Rol> roles` (`@ManyToMany` LAZY con `@JoinTable(name="usuario_roles")`) |
| negocios | `Negocio` | negocio | `propietario` (`@OneToOne` LAZY → Usuario) |
| repartidores | `Repartidor` | negocio | `usuario` (`@OneToOne` LAZY), `negocio` (`@ManyToOne` LAZY, null = independiente) |
| vehiculos | `Vehiculo` | vehiculo | `negocio` (`@ManyToOne` LAZY) |
| recorridos | `Recorrido` | recorrido | `repartidor`, `vehiculo` (null permitido), `fecha`, `estado` |
| pedidos | `Pedido` | pedido | `negocio`, `cliente` (Usuario), `recorrido` (todos `@ManyToOne` LAZY y opcionales), `BigDecimal` en dinero y `lat/lng` |
| mensajes | `Mensaje` | mensaje | `pedido`, `remitente`, `destinatario` |
| tokens_revocados | `TokenRevocado` | auth | `jti`, `expiraEn` |
| password_reset_tokens | `PasswordResetToken` | auth | `usuario`, `tokenHash`, `expiraEn`, `usado` |
| notificaciones_log | `NotificacionLog` | notificacion | `canal`, `destino`, `evento`, `estado`, `detalle`, `pedidoId`, `usuarioId` (ids simples, sin relación) |

Repositorios `XxxRepository extends JpaRepository<Xxx, Long>` en el mismo paquete. Métodos ya definidos:
```java
// UsuarioRepository
@EntityGraph(attributePaths = "roles") Optional<Usuario> findByEmail(String email);
boolean existsByEmail(String email);
// RolRepository
Optional<Rol> findByNombre(RolNombre nombre);
// NegocioRepository
Optional<Negocio> findByPropietarioId(Long usuarioId);
// RepartidorRepository
@EntityGraph(attributePaths = {"usuario", "negocio"}) Optional<Repartidor> findByUsuarioId(Long usuarioId);
// TokenRevocadoRepository
boolean existsByJti(String jti);
void deleteByExpiraEnBefore(LocalDateTime fecha);
// PasswordResetTokenRepository
Optional<PasswordResetToken> findByTokenHash(String hash);
```
`Zonas` (datos de `01-BASE-DATOS.md`) viven en `com.rutaoax.common.ZonaCatalogo`:
```java
public record Zona(String nombre, double lat, double lng) {}
public final class ZonaCatalogo {
  public static List<Zona> todas();
  public static boolean existe(String nombre);
  public static double[] coordenadas(String zona, long semilla); // [lat,lng], desplazamiento determinista de ±0.004
}
```

### §3 Comunes (`com.rutaoax.common`)
```java
@JsonInclude(NON_NULL)
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path, Map<String,String> errores) {}

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
  public static <T> PageResponse<T> from(Page<T> p);
}
// Excepciones (RuntimeException) y su código:
BadRequestException → 400   UnauthorizedException → 401   ForbiddenException → 403
NotFoundException → 404     ConflictException → 409
// Todas reciben (String mensaje).
```
`GlobalExceptionHandler` (`@RestControllerAdvice`) devuelve `ApiError` con estos códigos: las 5 excepciones propias, `MethodArgumentNotValidException`/`ConstraintViolationException` → 422 con `errores` (campo → mensaje), `HttpMessageNotReadableException` → 400, `NoResourceFoundException`/`NoHandlerFoundException` → 404, `AccessDeniedException` → 403, `Exception` → 500 con mensaje genérico (el detalle solo al log). El campo `error` es el texto estándar del código HTTP.

Validación de contraseña: `@PasswordSegura` en `com.rutaoax.common.validation` (anotación + `ConstraintValidator`) con la regex de `00-CONTEXTO.md` y el mensaje "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial".

### §4 Auth, usuario y notificación (cómo hablan entre sí)
```java
// auth.JwtService
String generateToken(Usuario u);              // claims: sub=email, uid, roles (List<String>), jti (UUID), iat, exp
boolean isValid(String token);                // firma y vencimiento
String getEmail(String token);
String getJti(String token);
LocalDateTime getExpiration(String token);
long getExpirationMs();                       // app.jwt.expiration-ms

// auth.UsuarioPrincipal implements UserDetails  (el usuario autenticado en todo el backend)
Long getId(); String getEmail(); Set<String> getRoles();   // roles sin prefijo: "ADMIN"
// authorities = "ROLE_" + rol. En controladores: @AuthenticationPrincipal UsuarioPrincipal yo

// usuario.UsuarioService
UsuarioDTO aDTO(Usuario u);     // ÚNICA forma de construir UsuarioDTO (incluye perfil)

// usuario.dto
public record PerfilDTO(Long negocioId, String negocioNombre, String tipoRepartidor) {}  // "EMPRESA" | "INDEPENDIENTE" | null
public record UsuarioDTO(Long id, String nombre, String email, String telefono, String avatarUrl,
                         boolean activo, List<String> roles, PerfilDTO perfil) {}

// auth.dto
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
public record LoginResponse(String token, String tipo, LocalDateTime expiraEn, UsuarioDTO usuario) {}  // tipo = "Bearer"

// notificacion.EmailService  (se crea en HU-06 y lo reutilizan los demás días)
@Async void enviar(String destino, String asunto, String cuerpoHtml, String evento, Long usuarioId);
// Nunca lanza excepción: guarda en NotificacionLog (ENVIADO o FALLIDO con detalle).
```
`JwtFilter` (`OncePerRequestFilter`): lee `Authorization: Bearer`, valida token, verifica que su `jti` no esté en `tokens_revocados` y que el usuario exista y esté `activo`; si todo va bien coloca el `UsuarioPrincipal` en el `SecurityContext`. Si algo falla no autentica (el `AuthenticationEntryPoint` responde 401 en JSON).

---
## FRONTEND

### §5 Proyecto y archivos
- React + Vite (JavaScript, sin TypeScript), `react-router-dom` v6, `axios`, `leaflet` y `react-leaflet`.
- `.env.development`: `VITE_API_URL=http://localhost:9250/api` · `.env.production`: `VITE_API_URL=/api`.
- El CSS de diseño se copia de `diseno/css/estilos.css` a `src/styles/estilos.css` y se importa una sola vez en `main.jsx`. Las clases nuevas que haga falta (modal, mensajes, cargando, errores de campo) se **agregan al final de ese mismo archivo** usando las variables de `:root`; no hay otro CSS.
- Token: `sessionStorage`, clave `rutaoax_token`.

```
src/
  main.jsx · App.jsx (rutas)
  api/        http.js · errores.js · authApi.js · perfilApi.js
  auth/       AuthContext.jsx · ProtectedRoute.jsx · RoleRoute.jsx · rutas.js
  components/ui/      Field.jsx · Feedback.jsx · Modal.jsx · ConfirmDialog.jsx · StatusBadge.jsx
  components/layout/  AppLayout.jsx · Sidebar.jsx · Topbar.jsx · menus.js
  pages/auth/         LoginPage.jsx · RegistroPage.jsx · RecuperarPage.jsx · RestablecerPage.jsx
  pages/              PaginaPendiente.jsx · NoAutorizado.jsx · perfil/PerfilPage.jsx
  utils/      validaciones.js · format.js
  styles/     estilos.css
```

### §6 Módulos compartidos (firmas exactas)
```js
// api/http.js
export default http;                         // instancia axios con baseURL=VITE_API_URL y Authorization automático
export function getToken(); export function setToken(t); export function clearSession();
// Interceptor: 401 con token guardado (y no en /auth/login) → clearSession() y redirige a /login.

// api/errores.js
export function leerError(err) -> { status, mensaje, errores /* {campo: texto} */, esRed /* true si no hubo respuesta */ }

// api/authApi.js
login(email, password) -> { token, tipo, expiraEn, usuario }
register(datos) -> usuario          // datos = { nombre, email, password, telefono, tipoCuenta, nombreNegocio }
logout() -> void
me() -> usuario
forgotPassword(email) -> void
resetPassword(token, password) -> void

// api/perfilApi.js
getPerfil() · updatePerfil({nombre, telefono, avatarUrl}) · cambiarPassword({actual, nueva})

// auth/AuthContext.jsx
<AuthProvider> y useAuth() -> { usuario, cargando, login(email,password) /*devuelve usuario*/,
   registrar(datos), logout(), refrescar(), tieneRol(rol) }
// Al montar: si hay token llama me(); si falla limpia la sesión. "cargando" es true hasta terminar.

// auth/rutas.js
export function rutaInicio(usuario)   // ADMIN→/admin/usuarios · NEGOCIO→/negocio ·
                                      // REPARTIDOR→ tipoRepartidor==='EMPRESA' ? '/repartidor' : '/independiente' · CLIENTE→/cliente
// auth/ProtectedRoute: exige sesión (si no, /login). Usa <Outlet/>.
// auth/RoleRoute props { roles: string[], tipoRepartidor?: 'EMPRESA'|'INDEPENDIENTE' }: si no cumple → <NoAutorizado/>.

// utils/validaciones.js
export const REGEX_PASSWORD = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
validarRequerido(v, nombre) · validarEmail(v) · validarPasswordSegura(v) · validarTelefono(v)  // devuelven '' si está bien o el mensaje de error
```

### §7 Componentes de interfaz (firmas)
```jsx
<Field label name type value onChange onBlur error ...inputProps />   // etiqueta + input + mensaje de error debajo (clase del CSS)
<Cargando />  <ErrorRed mensaje onReintentar />  <Vacio mensaje />  <Mensaje tipo="ok|error">texto</Mensaje>
<Modal abierto titulo onCerrar pie>…</Modal>
<ConfirmDialog abierto titulo mensaje textoConfirmar peligroso cargando onConfirmar onCancelar />
<StatusBadge estado />        // PENDIENTE→e-pend · ASIGNADO/EN_CAMINO→e-cam · ENTREGADO→e-ok · NO_ENTREGADO→e-no
<PaginaPendiente titulo />    // "Esta pantalla se construye en un día posterior"
```
`components/layout/menus.js` exporta `menuPara(usuario)` que devuelve el arreglo `[{ ruta, texto }]`:
- NEGOCIO: `/negocio` Panel · `/negocio/pedidos` Pedidos y asignación · `/negocio/equipo` Equipo de reparto · `/negocio/vehiculos` Vehículos · `/negocio/mensajes` Mensajes
- REPARTIDOR empresa: `/repartidor` Mi ruta de hoy · `/repartidor/historial` Historial
- REPARTIDOR independiente: `/independiente` Entregas disponibles · `/independiente/ruta` Mi ruta y mapa · `/independiente/mensajes` Mensajes
- CLIENTE: `/cliente` Mi pedido · `/cliente/mensajes` Mensajes
- ADMIN: `/admin/usuarios` Usuarios · `/admin/pedidos` Pedidos · `/admin/vehiculos` Vehículos · `/admin/notificaciones` Notificaciones
El independiente usa `<aside class="side morado">`; el resto, `side`. Estructura del HTML igual a los diseños (`.marca`, `.rol`, enlaces con clase `on` para la ruta activa).

Los días siguientes solo agregan páginas en `pages/<rol>/` y las enchufan en `App.jsx` reemplazando el `PaginaPendiente` de esa ruta. **No tocan layout, menús, auth ni `http.js`.**


---
## ANEXO · Agregado en la Parte 2 (HU-03 y HU-04). Esto ya existe en el código.

### Backend
- `auth.dto.TipoCuenta`: `CLIENTE, NEGOCIO, INDEPENDIENTE`. `auth.dto.RegisterRequest(nombre, email, password, telefono, tipoCuenta, nombreNegocio)`.
- **Convención de validación:** un método `@AssertTrue` llamado `isXxxValido()` reporta su error en el campo `xxx` (lo traduce `GlobalExceptionHandler`). Ejemplo: `isNombreNegocioValido()` → `errores.nombreNegocio`.
- `auth.JwtService` agrega `Claims parse(String token)` (lanza `JwtException` si es inválido). El token se firma con HS256.
- `auth.UsuarioPrincipal.desde(Usuario)`: fábrica estática. En controladores: `@AuthenticationPrincipal UsuarioPrincipal yo`.
- `auth.JwtFilter` **no es un `@Component`**: `SecurityConfig` lo crea con `new JwtFilter(JwtService, UsuarioRepository, TokenRevocadoRepository)` y lo coloca antes de `UsernamePasswordAuthenticationFilter`.
- `config.ApiErrorWriter.escribir(req, res, HttpStatus, mensaje)`, `config.RestAuthenticationEntryPoint` (401) y `config.RestAccessDeniedHandler` (403): escriben el JSON de `ApiError` desde la capa de seguridad.
- `config.SecurityConfig` (versión real): sin estado, CSRF desactivado, CORS desde `app.cors.allowed-origins`, públicas `/api/auth/login`, `/register`, `/forgot-password`, `/reset-password`; el resto autenticado. **HU-08 agrega** `@EnableMethodSecurity` y las reglas por rol de la matriz, **modificando este mismo archivo**.
- `auth.AuthService`: `login(LoginRequest)`, `register(RegisterRequest)`, `me(Long usuarioId)`. HU-05 y HU-06 agregan `logout`, `forgotPassword` y `resetPassword` aquí.
- Los correos se guardan en minúsculas y sin espacios (`trim().toLowerCase()`) al registrar y al iniciar sesión.
- `GET /api/auth/me`, `POST /api/auth/login` y `POST /api/auth/register` ya existen. `/auth/logout`, `/forgot-password`, `/reset-password` y `/perfil/**` **todavía no**.

### Frontend
- `Field` acepta `as` (`"input"` por defecto, `"select"`, `"textarea"`) y `children` (para las `<option>`).
- Ya existen: `auth/ProtectedRoute`, `auth/RoleRoute`, `auth/RedirectInicio`, `pages/NoAutorizado` y `pages/auth/AuthShell` (el diseño de dos paneles que comparten login, registro y recuperación).
- `components/layout/menus.js` exporta `menuPara(usuario)`, `esIndependiente(usuario)` y `etiquetaRol(usuario)`.
- `utils/validaciones.js` también exporta `requisitosPassword(valor)` (lista de requisitos con `cumple`).
- `Topbar` ya tiene el menú de usuario: "Mi perfil" (`/perfil`) y "Cerrar sesión" (llama a `authApi.logout()` y, falle o no, limpia la sesión del navegador).
- `App.jsx` agrupa las rutas por `RoleRoute`. **Los días siguientes solo cambian el `element` de cada ruta**: reemplazan `<PaginaPendiente titulo="..." />` por la página real; no se tocan layout, menús, auth ni `http.js`.
- Las páginas `RecuperarPage` y `RestablecerPage` son marcadores; HU-06 las completa. `/perfil` es un marcador; HU-07 lo completa.


---
## ANEXO · Agregado en la Parte 3 (HU-05, HU-06 y HU-08). Esto ya existe en el código.

### Backend
- `AuthService` ahora recibe además `TokenRevocadoRepository` y `EmailService`, y agrega `logout(String token)` (guarda el `jti` y su expiración; es idempotente). `register` envía el correo de bienvenida (evento `REGISTRO`).
- **Cambio respecto al plan:** la recuperación **no** está en `AuthService`. Vive en `auth.PasswordResetService`: `solicitar(String email)` y `restablecer(String token, String nuevaClave)`.
- `auth.dto.ForgotPasswordRequest(email)` y `auth.dto.ResetPasswordRequest(token, password)` (la contraseña lleva `@PasswordSegura`).
- `AuthController` agrega: `POST /api/auth/logout` (204), `POST /api/auth/forgot-password` (200, siempre) y `POST /api/auth/reset-password` (204; 409 con "El enlace no es válido o ya venció").
- `auth.PasswordResetTokenRepository` agrega `int marcarUsadosDelUsuario(Long usuarioId)`.
- `auth.TokenCleanupJob` borra los tokens revocados vencidos todos los días a las 03:00.
- `notificacion.EmailService.enviar(destino, asunto, cuerpoHtml, evento, usuarioId)` (`@Async`, nunca lanza excepción, siempre deja una fila en `notificaciones_log` con canal EMAIL). **Es el modelo a imitar para `SmsService` y `WhatsAppService` en el Día 3.**
- `config.AsyncConfig` (`@EnableAsync`) y `config.SchedulingConfig` (`@EnableScheduling`).
- `config.SecurityConfig`: `@EnableMethodSecurity` y la matriz por rol (`/api/admin/**` ADMIN; `/api/negocio/**`, `/equipo/**`, `/vehiculos/**` NEGOCIO o ADMIN; `/api/disponibles/**` REPARTIDOR o ADMIN; `/api/recorridos/**` NEGOCIO, REPARTIDOR o ADMIN; `/api/mensajes/**` NEGOCIO, REPARTIDOR o CLIENTE). CORS: `.cors(Customizer.withDefaults())` con el bean `corsConfigurationSource`; **no** se inyecta `CorsConfigurationSource` por tipo.
- Propiedades: `app.dev.log-reset-link` (solo local), `spring.mail.properties.mail.smtp.*timeout=5000`.
- Hoy siguen sin existir: `/perfil/**` (HU-07).

### Frontend
- `RecuperarPage` y `RestablecerPage` ya están completas (usan `forgotPassword` y `resetPassword` de `authApi`). El cierre de sesión del `Topbar` ya revoca el token en el servidor.
- `/perfil` sigue siendo un marcador (HU-07).


---
## ANEXO · Agregado en la Parte 4 (HU-07 y HU-09). Esto ya existe en el código.

### Backend
- `usuario.PerfilService`: `UsuarioDTO obtener(Long usuarioId)`, `UsuarioDTO actualizar(Long usuarioId, PerfilRequest)`, `void cambiarPassword(Long usuarioId, CambiarPasswordRequest)`.
- `usuario.dto.PerfilRequest(nombre, telefono, avatarUrl)` y `usuario.dto.CambiarPasswordRequest(actual, nueva)` (la nueva lleva `@PasswordSegura`).
- `usuario.PerfilController`: `GET /api/perfil`, `PUT /api/perfil`, `PUT /api/perfil/password` (204). Reglas: 409 "La contraseña actual es incorrecta" y 409 "La contraseña nueva debe ser distinta a la actual".

### Frontend
- `api/perfilApi.js`: `getPerfil()`, `updatePerfil({nombre, telefono, avatarUrl})`, `cambiarPassword({actual, nueva})`.
- `pages/perfil/PerfilPage.jsx` (ruta `/perfil`, cualquier rol). Tras guardar llama a `refrescar()` de `useAuth()` para que el `Topbar` muestre los datos nuevos. Es el **modelo** de pantalla con carga, error de red, validación bajo cada campo y `ConfirmDialog`.

### Despliegue
- `deploy/preparar-servidor.sh` (se ejecuta una vez en el VPS) y `deploy/deploy.ps1` (se ejecuta desde la raíz del repo en cada publicación). Rutas en el servidor: `/opt/rutaoax/backend/app.jar`, `/opt/rutaoax/config/application.properties`, `/var/www/rutaoax`. Servicio: `rutaoax` (systemd). Puerto interno de la API: 9250.


---
## ANEXO · Agregado en el Día 2 Parte 1 (HU-10 y HU-11). Esto ya existe en el código.

### Backend
- `negocio.NegocioContexto` (`@Component`): `Negocio negocioDe(UsuarioPrincipal)` (lanza 403 si la cuenta no es de un negocio) y `Long ambito(UsuarioPrincipal)` (null para ADMIN = sin filtro). **Todo servicio del negocio lo usa** para filtrar por pertenencia; un recurso de otro negocio responde 404.
- Patrón de listado (modelo para pedidos, mensajes, etc.): el controlador recibe `q`, filtros, `page` (0) y `size` (10, máx. 50); el servicio arma `PageRequest` con orden fijo, consulta con `@Query` filtrando en SQL, calcula datos de apoyo del día **en una sola consulta por página** (sin N+1) y devuelve `PageResponse<DTO>` con `PageResponse.from(pagina.map(...))`.
- `vehiculo.VehiculoService`: `listar(yo, q, tipo, estado, page, size)`, `obtener`, `crear`, `actualizar`, `eliminar`, `disponibles(yo, fecha)`. DTOs: `VehiculoDTO(id, tipo, placa, capacidad, estado, seguroVence, enUsoHoy, repartidorHoy)`, `VehiculoRequest(tipo, placa, capacidad, estado, seguroVence)`, `VehiculoDisponibleDTO(id, tipo, placa, capacidad, cupoLibre)`.
- `negocio.EquipoService`: `listar`, `crear`, `actualizar`, `eliminar` (desactiva), `disponibles(yo, fecha)`. DTOs: `EquipoDTO(id, usuario, vehiculoHoy, entregadosHoy, totalHoy, estadoHoy)`, `EquipoRequest(nombre, email, password, telefono)`, `EquipoUpdateRequest(nombre, telefono)`, `RepartidorDisponibleDTO(id, nombre)`. `POST` y `PUT` responden con el mismo `EquipoDTO` del listado.
- Repositorios con consultas nuevas: `RecorridoRepository` (`activosPorVehiculos`, `activosPorRepartidores`, `vehiculoIdsConRecorrido`, `repartidorIdsConRecorrido`, `existsBy...FechaGreaterThanEqualAndEstadoNot`), `PedidoRepository.conteoPorRecorrido`, `RepartidorRepository` (`buscarEquipo`, `findByIdAndNegocioId`, `findByNegocioIdAndUsuarioActivoTrue`), `VehiculoRepository` (`buscar`, `existsByPlaca`, `existsByPlacaAndIdNot`, `findByNegocioIdAndEstadoOrderByPlaca`).
- `/api/vehiculos/**` lo permite el filtro a NEGOCIO y ADMIN; `/api/equipo/**` se restringe además con `@PreAuthorize("hasRole('NEGOCIO')")`.

### Frontend
- `components/ui/Pagination.jsx`: `<Pagination page totalPages totalElements onPage />` (page desde 0). Usar con la respuesta `PageResponse` del servidor.
- `utils/useDebounce.js`: `useDebounce(valor, ms = 400)`.
- `utils/format.js` agrega `fechaCorta(iso)`, `diasHasta(iso)` y `ETIQUETA_TIPO_VEHICULO`.
- `api/vehiculosApi.js` (`listarVehiculos`, `crearVehiculo`, `actualizarVehiculo`, `eliminarVehiculo`, `vehiculosDisponibles`) y `api/equipoApi.js` (`listarEquipo`, `crearMiembro`, `actualizarMiembro`, `eliminarMiembro`, `repartidoresDisponibles`). **Estas dos listas de "disponibles" son las que usa el panel de asignación de pedidos (HU-13).**
- Patrón de pantalla de listado (modelo para pedidos y las siguientes): filtros en estado + `useDebounce` + `page`; `cargar()` con `useCallback`; carga inicial, error de red con reintento, estado vacío (con y sin filtros); `Mensaje` de éxito o error arriba; formulario en `Modal` montado solo mientras está abierto (usa `<button type="submit" form="id-del-form">` en el pie); `ConfirmDialog` para eliminar; si se borra el último elemento de una página > 0, retrocede una página.
- `styles/estilos.css` agrega `.aviso.alerta`, `.paginacion`, `.tabla-scroll`, `.acciones`, `.form-vertical`, `.form-2col`.


---
## ANEXO · Agregado en el Día 2 Parte 2 (HU-12 y HU-13). Esto ya existe en el código.

### Backend
- `pedido.dto.PedidoDTO(id, clienteNombre, clienteTelefono, direccion, zona, lat, lng, articulos, total, notas, origen, pagoRepartidor, modalidad, estado, negocio:NegocioRefDTO, repartidor:RepartidorRefDTO, recorridoId, ordenParada, creadoEn)` con `PedidoDTO.from(Pedido)`. **Requiere** que `negocio`, `recorrido`, `recorrido.repartidor` y `repartidor.usuario` estén cargados (usar `PedidoRepository.detalle`, `buscar` o `paradasDe`, que ya los traen con `join fetch`).
- `pedido.dto.PedidoRequest` con validaciones; la zona se valida con `isZonaValido()` (el error sale en `errores.zona`). `pedido.dto.AsignarRequest(pedidoIds, repartidorId, vehiculoId, fecha)` (`fecha` no anterior a hoy).
- `pedido.PedidoService`: `listar(yo, q, estado, zona, modalidad, asignado, page, size)` (aplica el ámbito del rol), `obtener`, `crear`, `actualizar`, `eliminar`. `pedido.AsignacionService`: `asignar(yo, AsignarRequest)` → `RecorridoDTO` y `desasignar(yo, pedidoId)` → `PedidoDTO`.
- `recorrido.dto.RecorridoDTO(id, fecha, estado, repartidor, vehiculo:VehiculoRefDTO, total, entregados, paradas:List<PedidoDTO>)`. `recorrido.RecorridoService`: `aDTO(Long recorridoId)` (**el mapeo oficial de un recorrido; el Día 3 lo reutiliza**) y `compactar(Recorrido)` (renumera paradas y elimina el recorrido PLANEADO vacío).
- `PedidoRepository`: `buscar(negocioId, clienteId, repUsuarioId, q, estado, zona, modalidad, asig, pageable)` (usa `join fetch` + `countQuery`; `asig` es `""`, `"SI"` o `"NO"`), `detalle(id)`, `paradasDe(recorridoId)`, `findByRecorridoIdOrderByOrdenParada`, `countByRecorridoId`, `conteoPorRecorrido`. `RecorridoRepository` agrega `findByRepartidorIdAndFecha`, `findByVehiculoIdAndFecha`, `detalle(id)`.
- `common.Geo.distanciaKm(lat1, lng1, lat2, lng2)` (haversine). `common.ZonaController`: `GET /api/zonas`.
- Autorización: `GET /api/pedidos` y `GET /{id}` para cualquier rol autenticado (el servicio filtra); `POST` solo NEGOCIO; `PUT` y `DELETE` NEGOCIO o ADMIN; `asignar` y `desasignar` solo NEGOCIO (`@PreAuthorize`).

### Frontend
- `api/pedidosApi.js`: `listarPedidos`, `obtenerPedido`, `crearPedido`, `actualizarPedido`, `eliminarPedido`, `asignarPedidos`, `desasignarPedido`. `api/zonasApi.js`: `listarZonas`.
- `utils/format.js` agrega `hoyIso()` y `moneda(valor)`.
- `pages/negocio/PedidosPage` (tabla con selección múltiple, filtros, tamaño de página 10/20/40, acciones por fila), `PedidoFormModal` (crear/editar, incluye "publicar para independientes") y `AsignarPanel` (fecha, repartidor y vehículo disponibles, cupo; recibe `seleccionados` y llama a `onAsignado(mensaje)`).
- `styles/estilos.css` agrega `.col-check`, `.panel-asignar`, `.cupo`, `.chips` y estilos de checkbox.


---
## ANEXO · Agregado en el Día 2 Parte 3 (HU-14 y HU-15). Esto ya existe en el código.

### Backend
- **Cambio de contrato en las listas "disponibles":** `RepartidorDisponibleDTO(id, nombre, vehiculoId, paradas)` y `VehiculoDisponibleDTO(id, tipo, placa, capacidad, cupoLibre, repartidorId)`. Incluyen a los que no tienen recorrido ese día **o tienen uno PLANEADO**; con ruta planeada `vehiculoId`/`repartidorId` indican la pareja y `paradas`/`cupoLibre` el avance. `AsignacionService.asignar` ya aceptaba reusar la ruta; ahora la pantalla también.
- `negocio.ResumenService.resumen(yo)` → `ResumenDTO(sinAsignar, enCamino, entregadosHoy, mensajesNuevos, repartidoresHoy:List<RepartidorHoyDTO>, atencion:List<AtencionDTO>)`. `RepartidorHoyDTO(repartidorId, nombre, vehiculo, entregados, total)`. `AtencionDTO(tipo, titulo, detalle, referenciaId)` con `tipo` en `NO_ENTREGADO`, `MENSAJE` o `SEGURO`. `negocio.ResumenController`: `GET /api/negocio/resumen` (`@PreAuthorize` NEGOCIO).
- `negocio.EquipoService.repartidoresConRutaHoy(Negocio)` (reutilizable). `vehiculo.EtiquetasVehiculo.etiqueta(TipoVehiculo)` es el único lugar con las etiquetas en español de los tipos de vehículo.
- `pedido.PedidoService.publicar(yo, id)` y `pedido.AsignacionService.reasignar(yo, id)` → `PedidoDTO`. Endpoints `POST /api/pedidos/{id}/publicar` y `/reasignar` (solo NEGOCIO).
- Repositorios nuevos: `PedidoRepository` (`countByNegocioIdAndEstado`, `countByNegocioIdAndEstadoAndActualizadoEnGreaterThanEqual`, `findTop5ByNegocioIdAndEstadoOrderByActualizadoEnDesc`), `MensajeRepository` (`countByDestinatarioIdAndLeidoFalse`, `noLeidos(usuarioId, pageable)`), `VehiculoRepository.findByNegocioIdAndSeguroVenceLessThanEqualOrderBySeguroVence`, `RecorridoRepository.porVehiculosYFecha`.

### Frontend
- `api/negocioApi.js`: `obtenerResumen()`. `api/pedidosApi.js` agrega `publicarPedido(id)` y `reasignarPedido(id)`.
- `pages/negocio/PanelPage.jsx` (`/negocio`): indicadores, "Repartidores hoy" y "Requiere atención" con enlaces (`/negocio/pedidos?estado=NO_ENTREGADO`, `/negocio/mensajes`, `/negocio/vehiculos`).
- `PedidosPage` lee `?estado=` de la URL para el filtro inicial y agrega las acciones "Publicar" (PENDIENTE no publicado) y "Reasignar" (NO_ENTREGADO), ambas con `ConfirmDialog`.
- `AsignarPanel`: si el repartidor elegido ya tiene ruta fija su vehículo y muestra el cupo restante; los vehículos con ruta de otro repartidor no se ofrecen.
