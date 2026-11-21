# CIERRE DEL DÍA 2 · Negocio

## Criterio de cierre del sprint (de `04-SPRINTS.md`)
> Como `negocio1@`, crear un pedido, asignarlo a `repartidor1@` con un vehículo y ver el cupo bajar; probar que `negocio2@` no ve ni toca nada de `negocio1@`.

- [ ] Crear un pedido nuevo como `negocio1@` (Pedidos → "+ Nuevo pedido").
- [ ] Asignarlo a Luis Martínez (`repartidor1@`) con un vehículo, **en una fecha futura** (hoy Luis ya salió a ruta en los datos de ejemplo).
- [ ] Asignar un segundo pedido a Luis el mismo día y comprobar que el cupo del vehículo baja (10 → 9).
- [ ] Como `negocio2@`: solo ve sus propios pedidos, vehículos y equipo. Respaldo automático en Bruno: `Pedidos 10 y 19`, `Vehiculos 8`, `Equipo 8` y `Publicacion 4 y 8` (todas deben dar 404).

## Lista completa
**Funcionalidad (HU-10 a HU-15)**
- [ ] Vehículos: crear, editar, eliminar, filtros, paginación y aviso de seguro.
- [ ] Equipo: crear cuenta de repartidor, editar, quitar; el repartidor nuevo entra a `/repartidor`; el quitado ya no puede iniciar sesión.
- [ ] Pedidos: tabla con filtros y paginación (10/20/40), crear, editar, eliminar; las reglas 409 muestran su mensaje.
- [ ] Asignación: cupo, vehículo fijo cuando el repartidor ya tiene ruta, desasignar.
- [ ] Panel: indicadores, repartidores de hoy y "Requiere atención" con enlaces.
- [ ] Publicar y reasignar con confirmación.

**Calidad**
- [ ] Bruno completo en verde contra `local` (carpetas Auth, Equipo, Error, Negocio, Pedidos, Perfil, Publicacion, Seguridad, Vehiculos).
- [ ] Sin `alert(` ni `confirm(` en el frontend: `Select-String -Path frontend\src -Pattern "alert\(|confirm\(" -Recurse` no devuelve nada.
- [ ] Todas las pantallas tienen estado de carga, vacío y error de conexión; sin errores rojos en la consola del navegador.
- [ ] Probado en móvil (ancho de 390 px) y escritorio.
- [ ] Ningún listado trae todo y filtra en el navegador: en F12 → Network se ven `?page=`, `&size=` y los filtros en la URL.

**Repositorio y tablero**
- [ ] Commits de las dos personas en `main`.
- [ ] Tarjetas HU-01 a HU-15 en Done (HU-06 puede decir "falta Postfix, se cierra en HU-26").
- [ ] `docs/06-ESTADO.md` actualizado y sin problemas abiertos de código sin verificar.

**Despliegue (HU-09)**
- [ ] Si todavía no lo hiciste: Parte B de `dia-1/EJECUTAR-PARTE-4.md`.
- [ ] `./deploy/deploy.ps1` con todo lo del Día 2 y Bruno contra `produccion` en verde (el seed de producción lleva la fecha del primer arranque: algunas pruebas de "hoy" pueden verse distintas).
- [ ] Respaldo de la base: `ssh root@179.197.84.34 "mysqldump -u rutaoax_app -p`$(cat /root/.rutaoax_db_pass) rutaoax > /root/rutaoax_dia2.sql"`.

## Avance respecto a la rúbrica
| Bloque | Estado |
|---|---|
| Base de datos, Flyway, datos de prueba | listo (falta el diagrama ER y el respaldo en el README, Día 4) |
| Backend (DTOs, validaciones, errores, JWT, roles, paginación del servidor) | muy avanzado |
| Frontend (rutas protegidas, validaciones, modales, paginación y filtros del servidor) | avanzado: faltan las pantallas de repartidores, cliente y administrador |
| Usuarios y roles | negocio completo; falta repartidor, cliente y administrador |
| Comunicación (correo, SMS, WhatsApp) | **Día 3** |
| Bruno | colección grande; se completa el Día 4 |
| Figma, README, GitHub Projects | Día 4 (el tablero debe venir actualizándose todos los días) |

## Preparación del Día 3 (hazla hoy)
El Día 3 empieza con correo, SMS y WhatsApp reales; son las partes con más dependencias externas y tardan en propagarse:
1. **Despliegue hecho** (HU-09): Postfix y los registros de correo necesitan el servidor y el dominio funcionando.
2. **DNS del correo:** registros MX, SPF, DMARC y la clave DKIM publicados (`dia-1/05-DESPLIEGUE.md`, sección 7). La propagación puede tardar horas.
3. **Twilio:** cuenta creada, tu número verificado (en cuenta de prueba solo envía a números verificados) y unido al sandbox de WhatsApp (`join xxxx-xxxx`; dura 72 horas, hay que repetirlo antes de la demo). Anota tu `Account SID`, `Auth Token` y el número de origen para el `application.properties` del servidor.
4. Ten a la mano un teléfono real para recibir los mensajes de prueba.
Pídeme el Día 3 Parte 1 (servicio de notificaciones y correo real) con el mismo formato.
