# EJECUTAR EL DÍA 2 · PARTE 3 · HU-14 (panel del negocio) y HU-15 (publicar y reasignar)

**Meta:** el negocio ve de un vistazo qué atender, puede publicar un pedido para independientes y reasignar uno que no se entregó; y el panel de asignación permite sumar pedidos a una ruta existente y ver bajar el cupo (criterio de cierre del Día 2).
**Tiempo:** 45 a 60 minutos. Comandos en PowerShell.

> Como en las partes anteriores, este código se escribió sin poder compilarlo ni ejecutarlo en el entorno donde se generó. Los pasos 2 y 3 son la primera prueba real; el paso 8 dice qué hacer si algo falla.

## 1. Copiar el zip al repositorio
Copia el contenido de `rutaoax-dia2p3` sobre tu repo (elige *Reemplazar*). Reemplaza archivos que ya tenías, es lo esperado:
- `backend/`: **reemplaza** `EquipoService`, `VehiculoService`, `PedidoService`, `AsignacionService`, `PedidoController`, `PedidoRepository`, `RecorridoRepository`, `VehiculoRepository`, `MensajeRepository` y los DTO `RepartidorDisponibleDTO` y `VehiculoDisponibleDTO`; agrega `ResumenService`, `ResumenController`, `EtiquetasVehiculo` y los DTO del resumen.
- `frontend/`: **reemplaza** `App.jsx`, `AsignarPanel.jsx`, `PedidosPage.jsx` y `api/pedidosApi.js`; agrega `PanelPage.jsx` y `api/negocioApi.js`.
- `bruno/`: carpetas nuevas `Negocio` y `Publicacion`.
- `docs/`: `02-API.md`, `06-ESTADO.md`, `dia-1/01-INTERFACES-COMPARTIDAS.md` actualizados y `dia-2/03-CIERRE-DIA-2.md` nuevo.

## 2. Compilar y arrancar el backend
```powershell
cd backend
mvn -q compile
mvn spring-boot:run
```
`compile` sin errores y `Started RutaoaxApplication`. Si falla con `Validation failed for query for method ...`, ve al paso 8.

## 3. Probar el backend con Bruno (entorno `local`)
Ejecuta **toda** la colección: debe quedar en verde.
| Carpeta | Esperado |
|---|---|
| Auth, Equipo, Error, Pedidos, Perfil, Seguridad, Vehiculos | igual que antes (la lista de "disponibles" ahora trae más campos; las pruebas siguen pasando) |
| Negocio 1 a 4 | resumen del negocio (200), de otro negocio (200 con 0 en camino), cliente (403), admin (403) |
| Publicacion 1 a 9 | crear (201), **publicar (200, modalidad ABIERTO)**, publicar otra vez (409), de otro negocio (404), cliente (403), filtrar publicados (200), reasignar un pendiente (409), de otro negocio (404), eliminar (204) |

## 4. Verificar en MySQL
```sql
SELECT modalidad, estado, COUNT(*) FROM pedidos GROUP BY modalidad, estado;
SELECT COUNT(*) FROM recorridos;     -- 6: Bruno no deja recorridos sueltos
```
Los 15 pedidos de ejemplo siguen igual (Bruno borra los suyos).

## 5. Probar las pantallas
`cd frontend; npm run dev` y entra con `negocio1@rutaoax.com` / `Ruta#2026`.

**Panel (`/negocio`)** — los números suponen que el seed corrió **hoy**; otros días cambian "Entregados hoy" y los recorridos de hoy.
| Prueba | Esperado |
|---|---|
| Abrir `/negocio` | 4 indicadores: **Sin asignar 4** (tarjeta oscura), **En camino 2**, **Entregados hoy 2**, **Mensajes nuevos 2** |
| "Repartidores hoy" | Ana Ruiz (Camioneta OAX-880) y Luis Martínez (Motocicleta OAX-214), cada uno con barra y "0 de 2 entregados"; Jorge Pérez no aparece |
| "Requiere atención" | "Pedido #15 no entregado" (Daniela Ortiz · Santa Lucía del Camino) con **Reasignar**; "Mensaje de Carla Ríos" y "Mensaje de Mario Cruz" con **Abrir**; y, si el seguro de OAX-880 vence en 7 días o menos, "Seguro por vencer" con **Ver** |
| Botón "Reasignar" del panel | te lleva a `/negocio/pedidos?estado=NO_ENTREGADO` con el filtro ya puesto y solo ese pedido |
| "Abrir" en un mensaje | lleva a Mensajes (todavía pantalla pendiente del Día 3) |
| "Ver" en el seguro | lleva a Vehículos |
| Botón "Actualizar" | recarga los datos |
| Con `negocio2@rutaoax.com` | Sin asignar 1, En camino 0, Mensajes 0 y listas casi vacías |

**Publicar y reasignar (HU-15)**
| Prueba | Esperado |
|---|---|
| Pedidos → estado "Sin asignar" | los pedidos que dicen "· Publicado" no tienen botón Publicar; los otros dos sí |
| "Publicar" en uno | confirmación; al aceptar mensaje verde y la fila dice "· Publicado"; el filtro Tipo "Publicados" ahora muestra 3 |
| Marcar ese pedido publicado y asignarlo a tu equipo | se puede; al asignarlo deja de decir "Publicado" (vuelve a ser de tu equipo) |
| Pedido #15 "No entregado" | tiene **Reasignar** y **Eliminar**, pero no Editar |
| "Reasignar" en #15 | confirmación; al aceptar pasa a "Sin asignar" y "Asignado a" queda en "—"; en el panel sube "Sin asignar" y desaparece de "Requiere atención" |
Para repetir la prueba de reasignar, vuelve a dejar el pedido en ese estado (solo si tu base es la de ejemplo): `UPDATE pedidos SET estado='NO_ENTREGADO', recorrido_id=6, orden_parada=2 WHERE id=15;`

**Mejora del panel de asignación: ver bajar el cupo (criterio de cierre del Día 2)**
| Prueba | Esperado |
|---|---|
| Cambia la fecha del panel a **mañana** | repartidores: Ana, Jorge y Luis; vehículos: BIC-03, OAX-214, OAX-301, OAX-777 y OAX-880 |
| Marca 1 pedido sin asignar, elige **Luis Martínez + Motocicleta OAX-214** | recuadro "Cupo restante del vehículo 10 pedidos / Seleccionados 1" |
| Asignar | mensaje verde |
| Marca otro pedido; en el selector aparece "Luis Martínez · **ya tiene ruta (1 parada)**" | al elegirlo, el vehículo queda **fijo** en OAX-214 con la nota "Se usará el vehículo que ya tiene en su ruta de ese día" y el cupo muestra **9** |
| Asignar | mensaje verde; el cupo ahora sería 8 |
| Elegir a otro repartidor (Ana) el mismo día | OAX-214 **ya no se ofrece** como vehículo libre |
| Desasignar los dos pedidos | Luis vuelve a mostrarse sin ruta y OAX-214 vuelve a la lista de libres |
Con F12 → Consola no deben salir errores en rojo. Prueba también en ancho móvil: los indicadores pasan a una columna.

## 6. Cerrar la parte
```powershell
cd ..    # raíz del repo
git add backend bruno
git commit -m "feat(negocio): panel resumen, publicar y reasignar pedidos, asignacion a rutas planeadas"
git add frontend docs
git commit -m "feat(frontend): panel del negocio y acciones de publicar y reasignar"
git push
```
1. **Tablero:** HU-14 y HU-15 → Done si todo pasó.
2. **`docs/06-ESTADO.md`:** cambia 🟨 por ✅ en HU-14, HU-15 y la fila A15; borra el problema abierto #1; llena fecha y "Quién". Si ya habías puesto repo, tablero y Figma en tu copia, pásalos al archivo nuevo.
3. **Cierra el Día 2** con `docs/dia-2/03-CIERRE-DIA-2.md` (lista de criterios y preparación del Día 3).

## 7. Qué sigue
**Día 3:** repartidores, cliente, mensajes y comunicaciones reales (correo, SMS y WhatsApp). Es el día con más dependencias externas: necesitas Postfix con SPF y DKIM funcionando y tu cuenta de Twilio lista. Si todavía no ejecutas el despliegue (HU-09), hazlo **antes** de empezar el Día 3; la guía del cierre explica el orden.
Si sigues en esta conversación no necesito nada; en una nueva, súbele `00-CONTEXTO.md`, `02-API.md`, `03-HISTORIAS.md`, `06-ESTADO.md` y `dia-1/01-INTERFACES-COMPARTIDAS.md` (con todos los anexos).

## 8. Si algo falla
| Síntoma | Qué hacer |
|---|---|
| `mvn -q compile` marca un error | Copia el error completo y el archivo; pide a una IA (con `00-CONTEXTO.md` y los anexos de interfaces) el archivo completo corregido |
| Arranque: `Validation failed for query ...` | Pega el mensaje y el repositorio indicado (`RecorridoRepository`, `MensajeRepository`, `PedidoRepository` o `VehiculoRepository`) |
| Arranque: `No property ... found for type ...` | Un método de repositorio no coincide con un campo: pega el mensaje y la entidad |
| `/api/negocio/resumen` responde 500 | Primer error en rojo de la consola con `ResumenService`; suele ser una consulta de repositorio |
| El panel muestra "0" en todo | Estás con un usuario sin datos (por ejemplo `negocio2@`) o la base tiene otra fecha; revisa con SQL |
| "Entregados hoy" en 0 | El seed corrió otro día (el contador usa la fecha de la última actualización del pedido) |
| Bruno `Negocio 2` falla | `negocio2@` tiene pedidos en camino: revisa `SELECT estado, COUNT(*) FROM pedidos WHERE negocio_id = 2 GROUP BY estado;` |
| Bruno `Publicacion 6` falla | Debe haber 3 o más pedidos publicados: revisa que Bruno `Publicacion 2` haya pasado |
| El panel no deja elegir el vehículo | Hay un repartidor con ruta elegido: su vehículo queda fijo a propósito; elige "Elige un repartidor" para soltarlo |
| Pantalla en blanco | F12 → Consola: copia el primer error rojo y el archivo que menciona |
Para pedir ayuda a una IA usa siempre: prompt maestro + `00-CONTEXTO.md` + el error completo + solo el archivo que falla.
