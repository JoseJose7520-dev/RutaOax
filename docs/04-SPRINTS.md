# 04 · SPRINTS 



## 1 · Base, autenticación y primer despliegue
Meta: entrar con los 4 roles en `https://rutaoax.devs8.tech` y ver su pantalla vacía.
| Bloque | Historias | Qué se entrega |
|---|---|---|
| Mañana | HU-01, HU-02 | Backend arranca, Flyway crea las 12 tablas, seed cargado, errores JSON |
| Mediodía | HU-03, HU-04 | Registro y login JWT (backend + pantallas), primera colección Bruno con token |
| Tarde | HU-05, HU-06, HU-08 | Logout con revocación, recuperación de contraseña, seguridad por rol |
| Noche | HU-07, HU-09 | Rutas protegidas, navbar, perfil y **primer despliegue con HTTPS** |

Criterio de cierre: login de `admin@rutaoax.com`, `negocio1@`, `repartidor1@`, `independiente1@` y `cliente1@rutaoax.com` en producción, cada uno redirigido a su sección; token revocado devuelve 401; Bruno verde.
Si te atrasas: deja HU-06 con el correo en FALLIDO (se cierra el día 3 con Postfix) pero no recortes HU-08 ni HU-09.

##  2 · Negocio
Meta: un negocio crea pedidos, los asigna a un repartidor con vehículo y ve su panel.
| Bloque | Historias | Qué se entrega |
|---|---|---|
| Mañana | HU-10, HU-11 | Vehículos y equipo (backend + pantallas) |
| Tarde | HU-12, HU-13 | Pedidos con paginación/filtros del servidor y asignación con reglas |
| Noche | HU-14, HU-15 | Panel del negocio, publicar y reasignar |

Criterio de cierre: como `negocio1@`, crear un pedido, asignarlo a `repartidor1@` con un vehículo y ver el cupo bajar; probar que `negocio2@` no ve ni toca nada de `negocio1@`.
Si te atrasas: recorta HU-15 y la tarjeta "Requiere atención" de HU-14 (queda solo con KPIs).

##  3 · Repartidores, cliente, mensajes y comunicaciones
Meta: el flujo completo de punta a punta con SMS y WhatsApp reales.
| Bloque | Historias | Qué se entrega |
|---|---|---|
| Mañana | HU-25, HU-26 | Servicio de notificaciones y correo real con Postfix (probar con el endpoint de prueba) |
| Mediodía | HU-16, HU-17, HU-18 | Repartidor de empresa: ruta, iniciar (SMS), entregar (WhatsApp) |
| Tarde | HU-19, HU-20 | Independiente: disponibles, tomar y ruta en mapa |
| Noche | HU-21, HU-23, HU-24 | Seguimiento del cliente y mensajes de los tres roles |

Criterio de cierre: flujo completo en producción: el negocio asigna → el repartidor inicia (llega SMS a tu celular) → marca entregado (llega WhatsApp) → el cliente ve el seguimiento actualizado → escribe un mensaje y lo recibe quien corresponde.
Si te atrasas: HU-22 y el Historial del repartidor no se hacen. Nunca se recortan HU-17, HU-18 ni HU-25 (valen puntos de comunicación).

##  4 · Administración, calidad y entrega
Meta: sistema cerrado, documentado y ensayado. **Después de las 14:00 no se agrega ninguna función nueva.**
| Bloque | Historias | Qué se entrega |
|---|---|---|
| Mañana | HU-27, HU-28, HU-29 | Pantallas de administrador y matriz de permisos con Bruno |
| Mediodía | HU-30, HU-31 | Validaciones y estados de interfaz, colección Bruno completa |
| Tarde | HU-32, HU-33 | README + ER + respaldo SQL; Figma, logo y paleta justificada |
| Noche | HU-34 | Despliegue final y ensayo completo de la demo con cada rol |
