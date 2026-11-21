# 06 · ESTADO ACTUAL (actualizar al cerrar cada tarea y cada día)

Última actualización: ________ · Día: 1 · Día 2 · Parte 3 entregada (HU-14 y HU-15) · Quién: ________

## Resumen
Fase actual: **DÍA 2 · Parte 3 entregada** (HU-14 panel del negocio, HU-15 publicar y reasignar, y la mejora del panel de asignación; **por verificar** con `docs/dia-2/EJECUTAR-DIA-2-PARTE-3.md`). Con esto termina el Día 2.
- Verificado por el equipo en local: Día 1 (salvo el despliegue) y Día 2 Partes 1 y 2. **HU-09 (primer despliegue con HTTPS) sigue sin ejecutarse.**
- Parte 3 entrega: `GET /api/negocio/resumen` y la pantalla `/negocio` (indicadores, repartidores de hoy y "Requiere atención"); `POST /pedidos/{id}/publicar` y `/reasignar` con sus botones; y el panel de asignación ahora **puede sumar pedidos a una ruta ya planeada** (resuelve la limitación anotada; era necesario para "ver el cupo bajar").
- Siguiente: Día 3 (repartidores, cliente, mensajes y comunicaciones reales). Antes, cerrar el Día 2 con `docs/dia-2/03-CIERRE-DIA-2.md` y, de preferencia, ejecutar HU-09.
- Interfaces congeladas en `docs/dia-1/01-INTERFACES-COMPARTIDAS.md` (con anexos hasta el Día 2 Parte 3).

## URLs y accesos
- Sitio: https://rutaoax.devs8.tech · API: https://rutaoax.devs8.tech/api · VPS: 179.197.84.34 · Backend local: http://localhost:9250/api · Frontend local: http://localhost:5173
- Repo: ________ · Tablero: ________ · Figma: ________
- Credenciales demo: admin `admin@rutaoax.com` / `Admin#2026`; resto `Ruta#2026` (negocio1@, negocio2@, repartidor1@ a repartidor4@, independiente1@, independiente2@, cliente1@ a cliente4@ … @rutaoax.com).
- MySQL local: base `rutaoax`, usuario `rutaoax_app`.

## Plan del Día 1 (tareas de `docs/dia-1/02-TAREAS-DIA-1.md`)
Estados: ⬜ pendiente · 🟨 en curso o por verificar · ✅ hecha y verificada · ❌ bloqueada
| Tarea | HU | Responsable | Estado | Notas |
|---|---|---|---|---|
| A1 Backend + Flyway + entidades | HU-01a | A | ✅ | Verificado por el equipo |
| A2 Seeder usuarios | HU-01b | A | ✅ | Verificado por el equipo |
| A3 Seeder operación | HU-01c | A | ✅ | Verificado por el equipo |
| A4 Errores y paginación | HU-02 | A | ✅ | Verificado por el equipo |
| A5 Login y JWT | HU-04 | A | ✅ | Verificado (arranca y funciona tras corregir CORS) |
| A6 Registro | HU-03 | A | ✅ | Verificado |
| A7 Logout con revocación | HU-05 | A | ✅ | Verificado por el equipo |
| A8 Recuperar contraseña y correo | HU-06 | A | ✅ | Verificado; el envío real queda FALLIDO hasta tener Postfix (HU-26) |
| A9 Seguridad por rol | HU-08 | A | ✅ | Verificado por el equipo |
| A10 Perfil backend | HU-07 | A | ✅ | Verificado en local |
| B1 Esqueleto frontend | HU-07a | B | ✅ | Verificado |
| B2 Login y sesión | HU-04 | B | ✅ | Verificado |
| B3 Registro | HU-03 | B | ✅ | Verificado |
| B4 Recuperar y restablecer | HU-06 | B | ✅ | Verificado por el equipo |
| B5 Rutas protegidas, navbar y perfil | HU-07/05 | B | ✅ | Verificado en local |
| D Despliegue con HTTPS | HU-09 | A+B | 🟨 | Entregado en la Parte 4 (script de servidor y deploy.ps1); falta ejecutarlo |

## Plan del Día 2 (Negocio)
| Tarea | HU | Responsable | Estado | Notas |
|---|---|---|---|---|
| A11 Vehículos (backend) | HU-10 | A | ✅ | Verificado por el equipo |
| B6 Vehículos (pantalla) | HU-10 | B | ✅ | Verificado por el equipo |
| A12 Equipo (backend) | HU-11 | A | ✅ | Verificado por el equipo |
| B7 Equipo (pantalla) | HU-11 | B | ✅ | Verificado por el equipo |
| A13 Pedidos (backend) | HU-12 | A | ✅ | Verificado por el equipo |
| B8 Pedidos (pantalla) | HU-12 | B | ✅ | Verificado por el equipo |
| A14 Asignación | HU-13 | A+B | ✅ | Verificado por el equipo (el panel se mejoró en la Parte 3) |
| A15 Panel y publicar/reasignar | HU-14, HU-15 | A+B | 🟨 | Entregado en la Parte 3; falta verificar |

## Estado por historia
| HU | Nombre | Estado | Notas |
|---|---|---|---|
| 00 | Entorno y repositorio | ⬜ | |
| 01 | Esqueleto backend y BD | ✅ | Verificado. V1 ajustado (ver Decisiones) |
| 02 | Errores globales y paginación | ✅ | Verificado |
| 03 | Registro | ✅ | Verificado |
| 04 | Login JWT | ✅ | Verificado |
| 05 | Logout con revocación | ✅ | Verificado |
| 06 | Recuperar contraseña | ✅ | Verificado; correo real pendiente de Postfix (HU-26) |
| 07 | Rutas protegidas, navbar y perfil | ✅ | Verificado en local |
| 08 | Seguridad por rol | ✅ | Verificado con la carpeta Seguridad de Bruno |
| 09 | Primer despliegue HTTPS | 🟨 | Scripts entregados; falta ejecutar el despliegue |
| 10 | Vehículos | ✅ | Verificado |
| 11 | Equipo de reparto | ✅ | Verificado |
| 12 | Pedidos del negocio | ✅ | Verificado |
| 13 | Asignar pedidos | ✅ | Verificado; mejorado en la Parte 3 |
| 14 | Panel del negocio | 🟨 | Backend y pantalla entregados; falta verificar |
| 15 | Publicar y reasignar | 🟨 | Backend y botones entregados; falta verificar |
| 16 a 34 | Días 3 y 4 | ⬜ | ver `03-HISTORIAS.md` |

## Archivos que ya existen
**Backend** (`backend/`): `pom.xml`, `.gitignore`, `src/main/resources/db/migration/V1__esquema.sql`, `application.properties.example`.
`src/main/java/com/rutaoax/`:
- `RutaoaxApplication`
- `common/`: `ApiError`, `PageResponse`, `Zona`, `ZonaCatalogo`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `ConflictException`, `GlobalExceptionHandler`, `validation/PasswordSegura`, `validation/PasswordSeguraValidator`
- `config/`: `PasswordConfig`, `SecurityConfig` (**con roles desde la Parte 3**), `AsyncConfig`, `SchedulingConfig`, `ApiErrorWriter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `SeederUsuarios`, `SeederOperacion`
- `usuario/`: `Usuario`, `Rol`, `RolNombre`, `UsuarioRepository`, `RolRepository`, `UsuarioService`, `dto/` (`UsuarioDTO`, `PerfilDTO`)
- `negocio/`: `Negocio`, `Repartidor`, sus repositorios
- `vehiculo/`: `Vehiculo`, `TipoVehiculo`, `EstadoVehiculo`, `VehiculoRepository`
- `recorrido/`: `Recorrido`, `EstadoRecorrido`, `RecorridoRepository`
- `pedido/`: `Pedido`, `Modalidad`, `EstadoPedido`, `PedidoRepository`
- `mensaje/`: `Mensaje`, `MensajeRepository`
- `auth/`: `TokenRevocado`, `PasswordResetToken`, sus repositorios, `JwtService`, `JwtFilter`, `UsuarioPrincipal`, `AuthService`, `AuthController`, `PasswordResetService`, `TokenCleanupJob`, `dto/` (`LoginRequest`, `LoginResponse`, `RegisterRequest`, `TipoCuenta`, `ForgotPasswordRequest`, `ResetPasswordRequest`)
- `notificacion/`: `NotificacionLog`, `Canal`, `EstadoEnvio`, `NotificacionLogRepository`, `EmailService`
**Endpoints vivos:** `POST /api/auth/login`, `/register`, `/logout`, `/forgot-password`, `/reset-password` y `GET /api/auth/me`.
**Frontend** (`frontend/`): `package.json`, `vite.config.js`, `index.html`, `.env.development`, `.env.production`; `src/`: `main.jsx`, `App.jsx`, `styles/estilos.css`, `api/` (`http`, `errores`, `authApi`), `auth/` (`AuthContext`, `ProtectedRoute`, `RoleRoute`, `RedirectInicio`, `rutas`), `components/ui/` (`Field`, `Feedback`, `Modal`, `ConfirmDialog`, `StatusBadge`), `components/layout/` (`AppLayout`, `Sidebar`, `Topbar`, `menus`), `pages/` (`PaginaPendiente`, `NoAutorizado`, `auth/` `AuthShell`, `LoginPage`, `RegistroPage`, `RecuperarPage` y `RestablecerPage` ya completas), `utils/` (`validaciones`, `format`).

**Perfil (Parte 4):** `usuario/` agrega `PerfilController`, `PerfilService`, `dto/PerfilRequest`, `dto/CambiarPasswordRequest`. Endpoints vivos nuevos: `GET /api/perfil`, `PUT /api/perfil`, `PUT /api/perfil/password`. Frontend: `api/perfilApi.js`, `pages/perfil/PerfilPage.jsx` y `App.jsx` con la ruta `/perfil` real.
**Despliegue (Parte 4):** `deploy/preparar-servidor.sh`, `deploy/deploy.ps1` (versión nueva), `deploy/nginx-rutaoax.conf` y `deploy/rutaoax.service` (ya existían).

**Día 2 Parte 1 (backend):** `negocio/` agrega `NegocioContexto`, `EquipoController`, `EquipoService`, `dto/` (`EquipoDTO`, `EquipoRequest`, `EquipoUpdateRequest`, `RepartidorDisponibleDTO`) y `RepartidorRepository` ampliado; `vehiculo/` agrega `VehiculoController`, `VehiculoService`, `dto/` (`VehiculoDTO`, `VehiculoRequest`, `VehiculoDisponibleDTO`) y `VehiculoRepository` ampliado; `recorrido/RecorridoRepository` y `pedido/PedidoRepository` con consultas nuevas.
**Endpoints vivos nuevos:** `/api/vehiculos` (GET, GET/{id}, GET/disponibles, POST, PUT/{id}, DELETE/{id}) y `/api/equipo` (GET, GET/disponibles, POST, PUT/{id}, DELETE/{id}).
**Día 2 Parte 1 (frontend):** `api/vehiculosApi.js`, `api/equipoApi.js`, `components/ui/Pagination.jsx`, `utils/useDebounce.js`, `utils/format.js` ampliado, `pages/negocio/` (`VehiculosPage`, `VehiculoFormModal`, `EquipoPage`, `MiembroFormModal`), `App.jsx` y `styles/estilos.css` actualizados.
**Bruno nuevo:** carpetas `Vehiculos` (13 peticiones) y `Equipo` (12) y `Auth/Login negocio 2`.

**Día 2 Parte 2 (backend):** `pedido/` agrega `PedidoController`, `PedidoService`, `AsignacionService`, `dto/` (`PedidoDTO`, `PedidoRequest`, `AsignarRequest`, `NegocioRefDTO`, `RepartidorRefDTO`) y `PedidoRepository` ampliado; `recorrido/` agrega `RecorridoService`, `dto/` (`RecorridoDTO`, `VehiculoRefDTO`) y `RecorridoRepository` ampliado; `common/` agrega `Geo` y `ZonaController`.
**Endpoints vivos nuevos:** `GET/POST /api/pedidos`, `GET/PUT/DELETE /api/pedidos/{id}`, `POST /api/pedidos/asignar`, `POST /api/pedidos/{id}/desasignar`, `GET /api/zonas`.
**Día 2 Parte 2 (frontend):** `api/pedidosApi.js`, `api/zonasApi.js`, `pages/negocio/` (`PedidosPage`, `PedidoFormModal`, `AsignarPanel`), `utils/format.js` (agrega `hoyIso`, `moneda`), `App.jsx` y `styles/estilos.css` actualizados.
**Bruno nuevo:** carpeta `Pedidos` (26 peticiones: listado, filtros, CRUD, permisos, asignación, capacidad, desasignación).

**Día 2 Parte 3 (backend):** `negocio/` agrega `ResumenService`, `ResumenController` y `dto/` (`ResumenDTO`, `RepartidorHoyDTO`, `AtencionDTO`); `EquipoService` y `VehiculoService` cambian `disponibles` (ahora incluyen rutas PLANEADAS); `PedidoService.publicar`, `AsignacionService.reasignar` y los endpoints correspondientes; `vehiculo/EtiquetasVehiculo`; repositorios ampliados (`PedidoRepository`, `MensajeRepository`, `VehiculoRepository`, `RecorridoRepository`); `RepartidorDisponibleDTO` y `VehiculoDisponibleDTO` con campos nuevos.
**Endpoints vivos nuevos:** `GET /api/negocio/resumen`, `POST /api/pedidos/{id}/publicar`, `POST /api/pedidos/{id}/reasignar`.
**Día 2 Parte 3 (frontend):** `api/negocioApi.js`, `pages/negocio/PanelPage.jsx`, `AsignarPanel.jsx` y `PedidosPage.jsx` actualizados, `api/pedidosApi.js` (agrega `publicarPedido` y `reasignarPedido`), `App.jsx` (ruta `/negocio`).
**Bruno nuevo:** carpetas `Negocio` (4 peticiones) y `Publicacion` (9).

## Bitácora de sesiones con IA
| Fecha | Tarea | Quién | IA / cuenta | Resultado | Archivos creados o modificados |
|---|---|---|---|---|---|
| ________ | Parte 1: HU-01 (a, b, c) y HU-02 | ________ | Claude (esta conversación) | Verificado por el equipo; se corrigió una línea del SQL | `backend/` base |
| ________ | Parte 2: HU-03, HU-04 (backend y frontend) | ________ | Claude (esta conversación) | Verificado por el equipo; se corrigió `SecurityConfig` (CORS duplicado) | ver lista de archivos |
| ________ | Parte 3: HU-05, HU-06, HU-08 | ________ | Claude (esta conversación) | Verificado por el equipo | ver lista de archivos |
| ________ | Parte 4: HU-07 y HU-09 | ________ | Claude (esta conversación) | Perfil verificado en local por el equipo; el despliegue (HU-09) aún no se ejecuta | ver lista de archivos |
| ________ | Día 2 Parte 1: HU-10 y HU-11 | ________ | Claude (esta conversación) | Verificado por el equipo | ver lista de archivos |
| ________ | Día 2 Parte 2: HU-12 y HU-13 | ________ | Claude (esta conversación) | Verificado por el equipo | ver lista de archivos |
| ________ | Día 2 Parte 3: HU-14 y HU-15 | ________ | Claude (esta conversación) | Entregado sin compilar ni ejecutar en el entorno de la IA; verificación pendiente | ver lista de archivos |

## Decisiones técnicas tomadas
Formato: fecha · decisión · motivo · documento corregido.
- Spring Boot **3.5.7** (no 4.x), Java 21: jjwt 0.12.x usa Jackson 2 y el código que generan las IA es de la línea 3.x. Corregido en `00-CONTEXTO.md`.
- Seeders separados: `SeederUsuarios` (@Order 1) y `SeederOperacion` (@Order 2), ambos idempotentes. Corregido en `00`, `03` y `08`.
- `SecurityConfig` provisional con `permitAll` durante la Parte 1; HU-04 la sustituye entera (mismo archivo, no se crea otra).
- Sin Lombok: getters y setters explícitos. Fechas `creadoEn`/`actualizadoEn` se asignan en `@PrePersist`/`@PreUpdate`.
- Coordenadas del seed: `ZonaCatalogo.coordenadas(zona, n)` con `n` consecutivo del 1 al 15 (determinista). Los pedidos nuevos usarán su id.
- `GlobalExceptionHandler` también cubre 405 (método no permitido) y parámetros mal formados como 400.
- El correo de bienvenida y `EmailService` se agregan en HU-06; `app.dev.log-reset-link` solo en local.
- `ddl-auto=none`; se verifican columnas y conteos con consultas SQL.
- **V1 corregido por el equipo:** `fk_ped_negocio` quedó sin `ON DELETE SET NULL` (MySQL no permite `CHECK` sobre una columna con acción referencial, error 3823). Se conserva `ck_ped_dueno`. Consecuencia: no se puede borrar un negocio con pedidos (manejar 409 en el Día 4). Corregido en `01-BASE-DATOS.md`.
- JWT: HS256, claims `sub`, `uid`, `roles`, `jti`, `iat`, `exp`; el filtro usa el usuario y los roles de la base en cada petición (no confía solo en los roles del token).
- `JwtFilter` no es `@Component`: lo crea `SecurityConfig` (evita que se ejecute dos veces).
- Los métodos `@AssertTrue` llamados `isXxxValido()` reportan el error en el campo `xxx` (convención en `GlobalExceptionHandler`).
- Frontend: React 18, React Router 6, axios, Vite 5; token en `sessionStorage` (`rutaoax_token`). `ProtectedRoute`, `RoleRoute` y el cierre de sesión se adelantaron de HU-07 porque el layout los necesita.
- `SecurityConfig`: `.cors(Customizer.withDefaults())` en lugar de inyectar `CorsConfigurationSource` (Spring tiene otro bean de ese tipo y fallaba el arranque). Spring toma el bean llamado `corsConfigurationSource`.
- HU-05/06: la recuperación vive en `PasswordResetService` (no en `AuthService`); `AuthService` agrega `logout` y el correo de bienvenida. Token de recuperación: 32 bytes aleatorios, se guarda solo el hash SHA-256 (hex), vence en 30 minutos y es de un solo uso; al pedir uno nuevo se invalidan los anteriores.
- `EmailService.enviar` es `@Async` y nunca lanza excepciones: registra ENVIADO o FALLIDO en `notificaciones_log`. Eventos: `REGISTRO` y `RECUPERACION`.
- HU-08: matriz de roles en `SecurityConfig` + `@EnableMethodSecurity`. El filtro solo comprueba el rol; las reglas de pertenencia y de tipo de repartidor (empresa o independiente) se aplican en cada servicio desde el Día 2.
- HU-07: el cambio de contraseña exige la actual (409 si no coincide) y que la nueva sea distinta; no revoca los tokens ya emitidos. El avatar es una URL http(s) opcional. La lógica vive en `PerfilService` (no en `UsuarioService`).
- HU-09: `preparar-servidor.sh` es idempotente; genera la clave de MySQL (`/root/.rutaoax_db_pass`) y el secreto JWT y crea `/opt/rutaoax/config/application.properties` solo si no existe. Pide el certificado con Certbot únicamente si el DNS ya apunta al VPS y se define `CERTBOT_EMAIL`.
- En producción el frontend y la API comparten dominio (Nginx envía `/api/` al puerto 9250), por eso `VITE_API_URL=/api` y CORS solo permite `https://rutaoax.devs8.tech`.
- **Vehículos:** la placa se guarda en mayúsculas y es única (409). "En uso hoy" = tiene un recorrido de hoy sin finalizar (PLANEADO o EN_CURSO); `repartidorHoy` es el nombre del repartidor. Eliminar da 409 si tiene recorridos de hoy o futuros sin finalizar. El ADMIN puede listar, ver, editar y borrar cualquier vehículo pero **no crear** (un vehículo siempre pertenece a un negocio).
- **Equipo:** `/api/equipo` es solo para el rol NEGOCIO (el ADMIN recibe 403; gestiona usuarios en `/api/admin`). `id` es el id del repartidor. Quitar a un repartidor desactiva su cuenta (no pierde su historial) y **da 409 si tiene recorridos de hoy o futuros sin finalizar** (más estricto que el contrato original, que solo pedía EN_CURSO). `estadoHoy` es `EN_RUTA` solo con recorrido EN_CURSO; la pantalla muestra "Con ruta asignada" si tiene paradas y aún no sale. "Hoy" considera solo recorridos PLANEADO o EN_CURSO.
- `negocio.NegocioContexto` (`negocioDe(yo)` y `ambito(yo)`) es el punto único para saber a qué negocio pertenece el usuario; los módulos del negocio lo reutilizan.
- Listados del Día 2: paginación (`page`, `size` máx. 50) y filtros se resuelven en la base de datos; el frontend usa `useDebounce` (400 ms) en las búsquedas y `Pagination`.
- **Pedidos:** el listado respeta el ámbito del rol (ADMIN todos; NEGOCIO los suyos; CLIENTE los suyos por `cliente_id`; REPARTIDOR los de sus recorridos). Orden: más recientes primero (`id` descendente), `size` máx. 50. `q` busca en folio, cliente y dirección (se ignora un `#` inicial). Crear: solo NEGOCIO por ahora (CLIENTE se habilita en HU-22). Editar y eliminar: NEGOCIO dueño o ADMIN. Editar solo PENDIENTE o ASIGNADO; **no cambia la modalidad** y, si `clienteEmail` viene vacío, **conserva** el cliente enlazado. Eliminar da 409 si está EN_CAMINO o ENTREGADO.
- **Asignación:** `POST /api/pedidos/asignar` y `desasignar` son solo del rol NEGOCIO (el ADMIN recibe 403; se corrigió la matriz de `08-RUBRICA-Y-DEMO.md`). Reglas 409: pedido no PENDIENTE, vehículo en mantenimiento, vehículo ya asignado a otro repartidor ese día, repartidor con otro vehículo ese día o con recorrido EN_CURSO/FINALIZADO, y cupo insuficiente. La capacidad cuenta **pedidos** (paradas), no artículos. Pedido o repartidor/vehículo ajeno → 404. `fecha` anterior a hoy → 422. Asignar fija `modalidad = NEGOCIO`. Las paradas nuevas se ordenan por vecino más cercano partiendo de la última parada (o del Centro Histórico si no hay).
- Si al quitar un pedido (desasignar o eliminar) el recorrido PLANEADO queda sin paradas, **se elimina** (libera al repartidor y al vehículo); si quedan paradas se renumeran 1..n.
- Coordenadas del pedido: la zona fija el centro y un desplazamiento determinista por id (`ZonaCatalogo.coordenadas`); se recalculan al editar.
- `GET /api/zonas` ya existe (cualquier usuario autenticado).
- **Panel de asignación (corregido en la Parte 3):** `/equipo/disponibles` y `/vehiculos/disponibles` ahora incluyen a quienes tienen una ruta **PLANEADA** ese día. Si el repartidor ya tiene ruta, el panel fija su vehículo y muestra el cupo restante (capacidad − paradas); los vehículos con ruta de otro repartidor no se ofrecen. Esto permite sumar pedidos a una ruta existente y ver bajar el cupo.
- **Panel del negocio (HU-14):** `entregadosHoy` usa `actualizado_en` del pedido ENTREGADO (aproximación: no existe una columna de fecha de entrega). `atencion` muestra hasta 5 elementos por tipo.
- **Publicar (HU-15):** solo PENDIENTE y no publicado; conserva la posibilidad de asignarlo al equipo mientras nadie lo tome. **Reasignar:** solo NO_ENTREGADO → PENDIENTE; sale del recorrido sin renumerar las demás paradas (puede quedar un hueco en un recorrido en curso o finalizado; no afecta porque solo se asignan pedidos a recorridos PLANEADOS, que sí se renumeran).
- El equipo usa JDK 23 en local: no hay problema porque el proyecto compila con `java.version=21`; el VPS debe usar JRE 21.

## Problemas abiertos
| # | Descripción | Archivo/HU | Cómo reproducir |
|---|---|---|---|
| 1 | El código del Día 2 Parte 3 (panel, publicar, reasignar y mejora de asignación) no se compiló ni ejecutó en el entorno donde se escribió | `backend/` y `frontend/` | Seguir `docs/dia-2/EJECUTAR-DIA-2-PARTE-3.md` |
| 4 | HU-09: el primer despliegue con HTTPS aún no se ha ejecutado | `deploy/` | Seguir `docs/dia-1/EJECUTAR-PARTE-4.md` (Parte B) |
| 3 | Correo real: Postfix, SPF y DKIM aún no están configurados; los envíos quedan FALLIDO | HU-26 (Día 3) | `SELECT estado FROM notificaciones_log;` |
| 2 | Borrar un negocio con pedidos fallará por la llave foránea; falta responder 409 legible | Día 4 (admin) | `DELETE /admin/usuarios/{id}` de un negocio con pedidos |

## Cómo probar lo que hay hoy
Backend: seguir `docs/dia-1/EJECUTAR-PARTE-1.md` (base local `rutaoax`, copiar `application.properties.example` a `application.properties`, `mvn spring-boot:run`).
Frontend: `cd frontend && npm install && npm run dev` → http://localhost:5173 (con el backend corriendo).
Bruno: abrir `bruno/`, entorno `local`; ver en `EJECUTAR-PARTE-2.md` qué peticiones deben estar en verde hoy.
