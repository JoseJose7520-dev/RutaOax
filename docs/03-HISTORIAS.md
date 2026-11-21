# 03 · HISTORIAS DE USUARIO

Cada historia = una tarjeta de GitHub Projects = una conversación con una IA.
**Prioridad:** P1 obligatoria para la rúbrica · P2 importante · P3 se recorta si hay retraso.
**Hecho** significa: compila, cumple TODOS los criterios, tiene su petición en Bruno, está en el navegador y tiene commit.
Qué pegar a la IA en cada tarea: ver `07-PROMPTS-IA.md`.

---
## DÍA 0 · Preparación (2 a 3 horas, antes de programar)

### HU-00 · Entorno y repositorio — P1
Como equipo, queremos el repositorio, el tablero y el servidor listos para que los cuatro días se dediquen a funciones.
- Repo público en GitHub con carpetas `backend/ frontend/ bruno/ docs/ deploy/`, `.gitignore` (incluye `application.properties`, `node_modules`, `target`) y estos documentos en `docs/`.
- Tablero de GitHub Projects público con columnas Backlog, In Progress, Review, Done y una tarjeta por cada HU de este archivo (todas en Backlog).
- DNS creados y comprobados (ver `05-DESPLIEGUE.md`, sección 1).
- Servidor preparado (Java, MySQL 8, Nginx, Certbot, usuario `rutaoax`) según `05-DESPLIEGUE.md`.
- Cuenta de Twilio creada, número de teléfono propio verificado y sandbox de WhatsApp unido.
- Los dos integrantes pueden hacer `git push` y acceden al VPS por SSH.

---
## DÍA 1 · Base, autenticación y primer despliegue

### HU-01 · Esqueleto del backend y de la base — P1 · Rúbrica: BD
Como equipo, queremos que el backend arranque conectado a MySQL con las tablas creadas por Flyway y datos de prueba.
- Proyecto Spring Boot con las dependencias del stack y la estructura de paquetes de `00-CONTEXTO.md`.
- Se copia `V1__esquema.sql` sin modificarlo; al arrancar, Flyway crea las 12 tablas (comprobar `flyway_schema_history`).
- `SeederUsuarios` y `SeederOperacion` crean roles, los 13 usuarios, negocios, repartidores, 10 vehículos, 15 pedidos, 6 recorridos y 10 mensajes de `01-BASE-DATOS.md`, solo si la base está vacía.
- `ddl-auto=none`. `application.properties` fuera de Git y `application.properties.example` en el repo.
- Contraseñas guardadas con BCrypt (verificar en MySQL que `password_hash` empieza con `$2a$`).
- Archivos: `pom.xml`, `RutaoaxApplication`, `config/SeederUsuarios`, `config/SeederOperacion`, entidades y repositorios de todos los módulos.

### HU-02 · Manejo global de errores y paginación base — P1 · Rúbrica: Backend
Como desarrollador, quiero respuestas de error y de página uniformes para que el frontend las trate igual en todas partes.
- `GlobalExceptionHandler` devuelve el JSON de `02-API.md` para 400, 401, 403, 404, 409, 422 y 500.
- Una validación fallida devuelve 422 con `errores` por campo.
- `ApiError` y `PageResponse<T>` creados en `common/`.
- Bruno: `Error/Recurso inexistente (404)` y `Error/Datos inválidos (422)`.

### HU-03 · Registro de cuenta — P1 · Rúbrica: Usuarios, Backend
Como visitante, quiero crear una cuenta como cliente, negocio o repartidor independiente.
- POST `/auth/register` según el contrato; el email es único (409 si se repite).
- `@PasswordSegura` rechaza contraseñas débiles con 422 y mensaje claro.
- Tipo NEGOCIO crea su `negocio`; INDEPENDIENTE crea `repartidor` con `negocio_id` null; CLIENTE solo el usuario.
- Se envía el correo de bienvenida (si el correo aún no está configurado queda FALLIDO en el log y el registro igual funciona).
- Frontend `/registro`: campos nombre, correo, teléfono, contraseña y tipo de cuenta; errores bajo cada input en tiempo real y al enviar; la contraseña muestra qué requisito falta.
- Bruno: `Auth/Registro correcto`, `Auth/Registro contraseña débil (422)`, `Auth/Registro correo repetido (409)`.

### HU-04 · Inicio de sesión con JWT — P1 · Rúbrica: Usuarios, Backend, Frontend
Como usuario, quiero entrar con mi correo y contraseña para acceder a mi sección.
- POST `/auth/login` devuelve token con claims `sub` (email), `roles`, `jti`, `exp`.
- `JwtFilter` valida el token en cada petición protegida (firma, vencimiento y que su `jti` no esté revocado).
- Credenciales incorrectas o usuario inactivo → 401 con mensaje "Correo o contraseña incorrectos".
- Frontend `/login`: validación bajo los inputs, estado de carga en el botón, error de red claro; guarda el token (en memoria + `sessionStorage`) y redirige según rol (ver `00-CONTEXTO.md`).
- `http.js` (axios) agrega `Authorization: Bearer` en cada petición y, ante un 401, limpia la sesión y manda a `/login`.
- Bruno: `Auth/Login admin` (guarda el token en una variable de entorno), `Auth/Login incorrecto (401)`, `Auth/Me con token`.

### HU-05 · Cerrar sesión con invalidación del token — P1 · Rúbrica: Backend
Como usuario, quiero cerrar sesión para que nadie pueda reutilizar mi token.
- POST `/auth/logout` guarda el `jti` en `tokens_revocados`.
- Usar ese mismo token después devuelve 401 (comprobarlo con Bruno).
- Tarea programada o limpieza al arrancar que borra tokens vencidos.
- Frontend: botón "Cerrar sesión" en la barra superior llama al logout, limpia la sesión y vuelve a `/login`.
- Bruno: `Auth/Logout` y `Auth/Token revocado (401)`.

### HU-06 · Recuperar contraseña por correo — P1 · Rúbrica: Backend, Comunicación
Como usuario, quiero restablecer mi contraseña con un enlace enviado a mi correo.
- POST `/auth/forgot-password` responde 200 exista o no el correo; si existe, guarda el hash del token (30 min, un uso) y envía el correo con el enlace.
- POST `/auth/reset-password` valida token y contraseña segura; deja de servir tras usarse (409).
- `EmailService` usa `JavaMailSender` hacia Postfix local y registra el resultado en `notificaciones_log`.
- Frontend `/recuperar` y `/restablecer?token=`: formularios con validación bajo inputs y mensajes de éxito/error en pantalla.
- Bruno: `Auth/Olvidé mi contraseña` y `Auth/Restablecer`.
- Nota: el envío real depende de Postfix (HU-26). Mientras tanto debe quedar FALLIDO en el log, nunca simulado.

### HU-07 · Rutas protegidas, navbar y perfil — P1 · Rúbrica: Frontend, Usuarios
Como usuario, quiero ver solo las secciones de mi rol y mis datos en la barra superior.
- `AuthContext` expone usuario, roles, login, logout y carga la sesión con `/auth/me`.
- `ProtectedRoute` (requiere sesión) y `RoleRoute` (requiere rol): sin sesión → `/login`; con rol equivocado → pantalla "No tienes permiso" (403).
- Layout con barra lateral por rol (clases de `estilos.css`) y barra superior con nombre, correo, avatar (iniciales si no hay imagen) y menú con "Mi perfil" y "Cerrar sesión".
- `/perfil`: editar nombre, teléfono y avatar (URL); cambiar contraseña con validación bajo inputs y modal de confirmación.
- Backend: GET/PUT `/perfil` y PUT `/perfil/password`.
- Bruno: `Perfil/Ver`, `Perfil/Actualizar`, `Perfil/Cambiar contraseña`.

### HU-08 · Seguridad por rol en el backend — P1 · Rúbrica: Usuarios, Backend
Como sistema, quiero que cada endpoint exija el rol correcto.
- `SecurityFilterChain` según la matriz de seguridad de `02-API.md`; sesión sin estado; CSRF desactivado; CORS con `app.cors.allowed-origins`.
- 401 y 403 salen en el JSON estándar (con `AuthenticationEntryPoint` y `AccessDeniedHandler` propios).
- Bruno: `Seguridad/Cliente intenta ver usuarios admin (403)` y `Seguridad/Sin token (401)`.

### HU-09 · Primer despliegue con HTTPS — P1 · Rúbrica: Seguridad/Despliegue
Como equipo, queremos el sitio vivo en el VPS desde el día 1.
- `https://rutaoax.devs8.tech` muestra la pantalla de login con candado válido (Certbot).
- `https://rutaoax.devs8.tech/api/auth/login` responde desde el JAR gestionado por systemd (`systemctl status rutaoax` activo).
- El backend se reinicia solo si se cae y al reiniciar el servidor.
- `deploy/deploy.ps1` publica backend y frontend con un comando.
- Se puede entrar con `admin@rutaoax.com` en producción.

---
## DÍA 2 · Negocio

### HU-10 · Gestión de vehículos — P1 · Diseño: `negocio-vehiculos.html`
Como negocio, quiero registrar y mantener mis vehículos.
- CRUD completo con las rutas de `02-API.md`; solo ve y toca los suyos (otro → 404).
- La placa es única (409). Listado paginado en servidor con filtros `q`, `tipo`, `estado`.
- Cada tarjeta muestra tipo, placa, capacidad, "En uso"/"Disponible" calculado con los recorridos de hoy, repartidor de hoy y aviso si el seguro vence en 7 días o menos.
- Formulario en modal con validación bajo inputs; eliminar pide confirmación en modal.
- Estados de carga, vacío y error de red.
- Bruno: carpeta `Vehiculos/` (crear, listar con filtro, editar, eliminar, 404 de uno ajeno).

### HU-11 · Equipo de reparto — P1 · Diseño: `negocio-equipo.html`
Como negocio, quiero dar de alta a mis repartidores para asignarles entregas.
- POST `/equipo` crea la cuenta REPARTIDOR ligada al negocio; la tabla muestra vehículo de hoy, avance y estado (En ruta / Libre).
- Listado paginado con búsqueda. Editar y desactivar con modal de confirmación.
- Un repartidor desactivado no puede iniciar sesión (401).
- Bruno: carpeta `Equipo/`.

### HU-12 · Pedidos del negocio: listado, crear, editar y borrar — P1 · Diseño: `negocio-pedidos.html`
Como negocio, quiero administrar mis pedidos.
- Tabla con paginación real (`page`, `size` 10/20/40), búsqueda por folio, cliente o dirección y filtros por estado y zona, todo enviado como parámetros a la API.
- Crear y editar en modal; la zona sale de `/zonas`; si se escribe el correo de un cliente registrado, el pedido queda ligado a su cuenta.
- Editar solo en PENDIENTE o ASIGNADO. Eliminar con confirmación; 409 si ya salió a ruta.
- Estados con etiquetas de color y texto.
- Bruno: carpeta `Pedidos/` (crear, listar con filtros, editar, borrar, 422 de datos inválidos).

### HU-13 · Asignar pedidos a repartidor y vehículo — P1 · Diseño: `negocio-pedidos.html` (panel derecho)
Como negocio, quiero asignar varios pedidos a un repartidor con un vehículo.
- Se marcan pedidos PENDIENTE, se elige repartidor y vehículo disponibles (`/equipo/disponibles`, `/vehiculos/disponibles`) y fecha; el panel muestra el cupo restante.
- POST `/pedidos/asignar` cumple todas las reglas 409 del contrato y deja los pedidos en ASIGNADO con su `ordenParada`.
- Desasignar un pedido ASIGNADO lo regresa a PENDIENTE.
- Los datos persisten en MySQL (comprobar con una consulta).
- Bruno: `Asignacion/Asignar correcto`, `Asignacion/Excede capacidad (409)`, `Asignacion/Pedido ajeno (404)`, `Asignacion/Desasignar`.

### HU-14 · Panel del negocio — P2 · Diseño: `negocio-panel.html`
Como negocio, quiero ver de un vistazo qué necesita atención.
- GET `/negocio/resumen` alimenta los 4 indicadores, "Repartidores hoy" con barra de avance y "Requiere atención".
- Los botones de la tarjeta de atención llevan a la pantalla correspondiente.
- Bruno: `Negocio/Resumen`.

### HU-15 · Publicar y reasignar pedidos — P2 · Diseño: `negocio-pedidos.html`
Como negocio, quiero publicar un pedido para independientes o reasignar uno no entregado.
- Acción "Publicar para independientes" (solo PENDIENTE) y "Reasignar" (solo NO_ENTREGADO), ambas con modal de confirmación.
- Bruno: `Pedidos/Publicar` y `Pedidos/Reasignar`.

---
## DÍA 3 · Repartidores, cliente, mensajes y comunicaciones

### HU-16 · Repartidor de empresa: mi ruta de hoy — P1 · Diseño: `repartidor-ruta.html`
Como repartidor de empresa, quiero ver solo mi ruta del día.
- GET `/recorridos/hoy` muestra barra de avance y paradas numeradas (hechas atenuadas, actual resaltada).
- Sin recorrido → estado vacío claro ("Hoy no tienes entregas asignadas").
- Un repartidor no puede consultar recorridos ajenos (404).
- Menú propio del diseño: Mi ruta, Parada actual, Historial (este último P3).
- Bruno: `Repartidor/Mi ruta de hoy`, `Repartidor/Recorrido ajeno (404)`.

### HU-17 · Iniciar recorrido y notificar por SMS — P1 · Rúbrica: Comunicación
Como repartidor, quiero iniciar mi ruta y que el cliente reciba un aviso.
- POST `/recorridos/{id}/iniciar`: recorrido EN_CURSO, pedidos EN_CAMINO.
- Cada pedido envía un SMS real al `cliente_telefono` por `SmsService` (Twilio) y registra el resultado en `notificaciones_log`.
- Si Twilio falla, el recorrido igual inicia (log FALLIDO).
- Botón "Iniciar ruta" con estado de carga y modal de confirmación.
- Bruno: `Repartidor/Iniciar recorrido`, `Repartidor/Iniciar sin paradas (409)`.

### HU-18 · Parada actual y marcar entregado — P1 · Rúbrica: Comunicación · Diseño: `repartidor-parada.html`
Como repartidor, quiero concentrarme en una parada y marcarla.
- La pantalla muestra solo la parada actual en grande, con "Llamar" (enlace `tel:`) y "Cómo llegar" (enlace a mapas con lat/lng), y la siguiente en una tarjeta discreta.
- "Marcar como entregado" → modal de confirmación → PATCH `/pedidos/{id}/estado` ENTREGADO → WhatsApp real al cliente (`WhatsAppService`) y log.
- "No pude entregar" pide motivo en un modal y marca NO_ENTREGADO.
- Al cerrar la última parada el recorrido queda FINALIZADO y se vuelve a la lista.
- Un pedido que no es de su recorrido → 403/404.
- Bruno: `Repartidor/Marcar entregado`, `Repartidor/Marcar no entregado`, `Repartidor/Pedido ajeno (404)`.

### HU-19 · Independiente: entregas disponibles y tomar — P1 · Diseño: `independiente-disponibles.html`
Como repartidor independiente, quiero elegir las entregas que haré.
- Lista paginada de `/disponibles` con filtros por zona y distancia; botón "Tomar" con carga y mensaje de éxito.
- Si otro la tomó antes → 409 con mensaje "Esta entrega ya fue tomada" y la lista se actualiza.
- Un repartidor de empresa que llame a `/disponibles` recibe 403.
- Mapa maqueta con los pines cercanos usando Leaflet.
- Bruno: `Independiente/Listar disponibles`, `Independiente/Tomar`, `Independiente/Tomar ya tomada (409)`, `Independiente/Empresa no puede (403)`.

### HU-20 · Independiente: mi ruta en el mapa — P1 · Diseño: `independiente-ruta.html`
Como repartidor independiente, quiero ver mis entregas ordenadas en un mapa.
- Mapa Leaflet con pines numerados y línea de ruta; orden inicial por cercanía con `sortByNearest` a partir de mi ubicación (geolocalización del navegador o Centro Histórico si se niega).
- Muestra distancia total y paradas con kilómetros; botones "Reordenar" (vuelve a calcular y guarda con PUT `/recorridos/{id}/orden`), "Quitar entrega" (soltar, con confirmación) e "Iniciar ruta".
- Las paradas se atienden con la misma pantalla de parada de HU-18 (`/repartidor/parada/:id` con acento morado).
- Bruno: `Independiente/Soltar`, `Recorrido/Cambiar orden`.

### HU-21 · Cliente: mis pedidos y seguimiento — P1 · Diseño: `cliente-pedido.html`
Como cliente, quiero ver en qué estado va mi pedido.
- Lista de mis pedidos (paginada) y el detalle con línea de tiempo del `/seguimiento`, repartidor y paradas antes de la mía.
- Solo veo mis pedidos; el de otro cliente → 404.
- Bruno: `Cliente/Mis pedidos`, `Cliente/Seguimiento`, `Cliente/Pedido ajeno (404)`.

### HU-22 · Cliente: crear un envío abierto — P3 · Diseño: formulario en modal
Como cliente, quiero pedir una entrega para que la tome un repartidor independiente.
- Modal con dirección, zona, artículos y notas; crea pedido ABIERTO propio.
- Bruno: `Cliente/Crear envío`.

### HU-23 · Mensajes del cliente — P1 · Diseño: `cliente-mensajes.html` y cuadro de `cliente-pedido.html`
Como cliente, quiero escribir sobre mi pedido y ver las respuestas.
- Desde el seguimiento puedo enviar un mensaje; el backend lo dirige según las reglas de `00-CONTEXTO.md` (negocio o independiente).
- Bandeja con hilos por pedido, cada uno con la etiqueta "Pedido #id", último mensaje y no leídos; entrar a un hilo los marca como leídos.
- Si aún no hay a quién enviarlo (ABIERTO sin repartidor) se muestra el mensaje del 409 bajo el cuadro.
- Bruno: `Mensajes/Cliente envía a negocio`, `Mensajes/Cliente envía a independiente`, `Mensajes/Sin destinatario (409)`, `Mensajes/Hilo ajeno (404)`.

### HU-24 · Mensajes de negocio e independiente — P1 · Diseño: `negocio-mensajes.html`, `independiente-mensajes.html`
Como negocio o independiente, quiero atender los mensajes de mis clientes.
- Bandeja con hilos (la misma pantalla compartida, con acento azul o morado), cabecera con cliente, dirección y repartidor, burbujas y campo de respuesta.
- El mensaje de un cliente cuyo pedido lleva un repartidor de empresa aparece en la bandeja del negocio, no en la del repartidor; el de un independiente aparece en la suya.
- El repartidor de empresa recibe 403 en `/mensajes`.
- Bruno: `Mensajes/Negocio responde`, `Mensajes/Independiente responde`, `Mensajes/Empresa no tiene mensajes (403)`.

### HU-25 · Servicio de notificaciones unificado — P1 · Rúbrica: Comunicación
Como equipo, queremos un solo punto para enviar correo, SMS y WhatsApp.
- `NotificacionService` con métodos por evento; `EmailService`, `SmsService`, `WhatsAppService` por canal. Los envíos son asíncronos (`@Async`) y nunca lanzan errores hacia el flujo principal.
- Cada intento queda en `notificaciones_log` (ENVIADO o FALLIDO con detalle).
- Números con prefijo de país configurable (`app.notificaciones.pais-prefijo`); WhatsApp usa `whatsapp:+52...`.
- POST `/admin/notificaciones/prueba` y GET `/admin/notificaciones` para comprobar cada canal sin crear pedidos.
- Bruno: `Admin/Probar correo`, `Admin/Probar SMS`, `Admin/Probar WhatsApp`, `Admin/Ver log`.

### HU-26 · Postfix, SPF y DKIM funcionando — P1 · Rúbrica: Comunicación
Como equipo, queremos que los correos lleguen a la bandeja de entrada y no a spam.
- Postfix instalado y enviando desde `no-reply@devs8.tech` según `05-DESPLIEGUE.md`.
- Un correo de prueba a Gmail llega a Recibidos y en "Mostrar original" aparecen `SPF: PASS` y `DKIM: PASS`.
- La recuperación de contraseña (HU-06) entrega el correo real.

---
## DÍA 4 · Administración, calidad y entrega

### HU-27 · Administrador: usuarios — P1 · Rúbrica: Usuarios
Como administrador, quiero gestionar todas las cuentas.
- Tabla paginada de usuarios con búsqueda y filtros por rol y estado (servidor); crear, editar, activar/desactivar y eliminar con confirmación.
- No se puede eliminar al único ADMIN (409).
- Bruno: carpeta `Admin/` (listar, crear, desactivar, eliminar).

### HU-28 · Administrador: ver y gestionar todo — P2
Como administrador, quiero consultar pedidos, vehículos y notificaciones de todo el sistema.
- Pantallas `/admin/pedidos`, `/admin/vehiculos`, `/admin/notificaciones` con los mismos componentes de tabla, paginación y filtros; el ADMIN ve todo sin filtro de pertenencia y puede editar y borrar.
- Bruno: `Admin/Pedidos de todos`.

### HU-29 · Revisión de seguridad y permisos — P1 · Rúbrica: Usuarios, Backend
Como equipo, queremos comprobar que cada rol solo accede a lo suyo.
- Se ejecuta una matriz de pruebas en Bruno: cada rol contra cada grupo de endpoints (ver `08-RUBRICA-Y-DEMO.md`); todo lo no permitido responde 403 o 404 y nada devuelve datos ajenos.
- Ninguna respuesta contiene `password_hash` ni entidades JPA.
- Las rutas de React ocultan menús y bloquean URLs de otros roles.

### HU-30 · Validaciones y estados de interfaz — P1 · Rúbrica: Frontend
Como equipo, queremos que toda pantalla cumpla los requisitos de experiencia.
- Todas las formas validan bajo el input (tiempo real y al enviar). Cero `alert()` o `confirm()` en el código (`grep` limpio).
- Cada pantalla tiene estado de carga, vacío y error de red.
- Se revisa en móvil (390 px) y escritorio (1280 px).

### HU-31 · Colección de Bruno completa — P1 · Rúbrica: Bruno
- `bruno/` versionada en el repo, con entornos `local` y `produccion`, y las carpetas de las HU anteriores.
- Incluye login con token guardado en variable, uso del token en peticiones protegidas y al menos un caso de error de cada tipo (401, 403, 404, 409, 422).

### HU-32 · README, diagrama ER y respaldo SQL — P1 · Rúbrica: GitHub, BD
- README con: descripción, tecnologías, diagrama ER (Mermaid de `01-BASE-DATOS.md`), instalación local, URL del sitio y de la API, credenciales de los 4 roles, cómo ejecutar Bruno, enlaces a Figma y GitHub Projects, y estructura del repo.
- `database/rutaoax_backup.sql` generado con `mysqldump`.
- `application.properties.example` completo.
- Commits de ambos integrantes visibles en el historial.

### HU-33 · Figma y logo — P1 · Rúbrica: Diseño
- Logo original de RutaOax.
- Prototipo navegable con las pantallas clave de los 4 roles (se reconstruyen a partir de los HTML de `diseno/`) y conexiones entre ellas.
- Justificación de la paleta (documentada en una página de Figma y en el README).
- Enlace público en el README.

### HU-34 · Ensayo de demo final — P1
- Se recorre el guion de `08-RUBRICA-Y-DEMO.md` en producción, con cada rol, sin errores.
- Cada integrante practica explicar su parte del código.
- El tablero de GitHub Projects muestra todas las tarjetas movidas a lo largo de los días (no todas en Done al final).
