desarrolladores 
Jose Jose Osvaldo
Santiago Ramire Erick Omar


links
proyecto desplegado
```https://rutaoax.devs8.tech/login```

repositorio 
``` https://github.com/JoseJose7520-dev/RutaOax ```

``` https://github.com/users/JoseJose7520-dev/projects/4 ```

figma
``` https://www.figma.com/design/K1VaaQSKUMMfZGb8G0xqos/RutaSur-Log%C3%ADstica---Mockup?node-id=21-28&p=f&t=v7RFA644sc3QjSh8-0  ```



RutaOax es un sistema web de logística de entregas en Oaxaca de Juárez. Conecta negocios, repartidores (de empresa o independientes) y clientes.




## Estructura del repositorio
```
rutaoax/
  backend/            Spring Boot (paquete base com.rutaoax)
  frontend/           React + Vite
  bruno/              colección Bruno
  docs/               estos documentos
  deploy/             rutaoax.service, nginx-rutaoax.conf, deploy.ps1
  README.md           documentación final de entrega (ER, credenciales, enlaces)
```

### Backend: un paquete por módulo (monolito modular)
```
com.rutaoax
  config/        SecurityConfig, CorsConfig, DataSeeder (CommandLineRunner)
  common/        ApiError, PageResponse, GlobalExceptionHandler (@RestControllerAdvice), excepciones propias
  auth/          AuthController, AuthService, JwtService, JwtFilter, dto/
  usuario/       Usuario, Rol, UsuarioRepository, PerfilController, AdminUsuarioController
  negocio/       Negocio, Repartidor, EquipoController, ResumenController
  vehiculo/      Vehiculo, VehiculoController, VehiculoService
  pedido/        Pedido, PedidoController, PedidoService, DisponiblesController
  recorrido/     Recorrido, RecorridoController, RecorridoService
  mensaje/       Mensaje, MensajeController, MensajeService
  notificacion/  NotificacionService, EmailService, SmsService, WhatsAppService, NotificacionLog
```



## 1. Requisitos (comprueba cada uno)
| Comando | Debes ver |
|---|---|
| `java -version` | `openjdk version "21...` (o Temurin 21) |
| `mvn -v` | `Apache Maven 3.9...` y `Java version: 21` |
| `mysql --version` | contiene **MySQL** y `8.` (si dice MariaDB, no sirve) |
| `git --version` | cualquier versión |
Si falta Java o Maven: instala JDK 21 y Maven 3.9, y agrega sus carpetas `bin` al `PATH`; abre una terminal nueva. También instala Bruno.



# ejecutar el proyecto

1._ clonar el proyetco, usa el comando desde la terminnal para hacer una copia del proyetco de forma local 
```
git clone https://github.com/JoseJose7520-dev/RutaOax.git
```
2._ actualiza `application.properties.example` 
colocando tu usuario y contraseña de mysql  

## 3. Base de datos local

comando para conectarte como usuario root 
```powershell
mysql -u root -p ```


crear usuario para la aplicasión asi como la base de datos, es el la misma terminal despues de entrar como root a mysql 

```sql
CREATE DATABASE rutaoax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'rutaoax_app'@'localhost' IDENTIFIED BY 'Rutaoax_local1';
GRANT ALL PRIVILEGES ON rutaoax.* TO 'rutaoax_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```


Prueba: `mysql -u rutaoax_app -pRutaoax_local1 rutaoax -e "SELECT 1;"` → tabla con un `1`.

## 4. Configuración local
```powershell
cd backend\src\main\resources
copy application.properties.example application.properties
notepad application.properties
```
Cambia solo estas líneas (el resto déjalo igual):
```
spring.datasource.username=rutaoax_app
spring.datasource.password=Rutaoax_local1
app.jwt.secret=rutaoax-clave-local-de-desarrollo-minimo-32-caracteres
```
`application.properties` está en `.gitignore`: no se sube a Git.

## 5. Compilar
```powershell
cd ..\..\..    # queda en backend\
mvn -q compile
```
Debes ver **nada** (sin errores) y volver al prompt. La primera vez tarda porque descarga dependencias.

## 6. Arrancar
```powershell
mvn spring-boot:run
```
Busca estas líneas en la consola (el orden puede variar un poco):
- `Successfully applied 1 migration to schema `rutaoax``
- `SeederUsuarios: 13 usuarios, 2 negocios y 6 repartidores creados`
- `SeederOperacion: 10 vehículos, 6 recorridos, 15 pedidos y 10 mensajes creados`
- `Tomcat started on port 9250`
- `Started RutaoaxApplication`



# 01 · BASE DE DATOS

MySQL 8 · Flyway · script real en `backend/src/main/resources/db/migration/V1__esquema.sql` 

## Tablas
| Tabla | Para qué sirve | Relaciones clave |
|---|---|---|
| `roles` | ADMIN, NEGOCIO, REPARTIDOR, CLIENTE | |
| `usuarios` | cuentas (email único, `password_hash` BCrypt, `activo`) | |
| `usuario_roles` | **N:M** usuarios–roles | PK compuesta |
| `negocios` | un negocio por usuario NEGOCIO | `propietario_id` único → usuarios |
| `repartidores` | perfil del repartidor; `negocio_id` null = independiente | `usuario_id` único, `negocio_id` → negocios |
| `vehiculos` | flota del negocio; estado ACTIVO o MANTENIMIENTO ("en uso hoy" se calcula con recorridos) | `negocio_id` → negocios |
| `recorridos` | ruta de un repartidor en una fecha (único por repartidor y fecha) | `repartidor_id`, `vehiculo_id` (null para independiente) |
| `pedidos` | entrega; paradas = pedidos con `recorrido_id` ordenados por `orden_parada` | `negocio_id` (null si ABIERTO), `cliente_id` (null si no tiene cuenta), `recorrido_id` |
| `mensajes` | mensajes ligados a un pedido | `pedido_id`, `remitente_id`, `destinatario_id` |
| `tokens_revocados` | `jti` de tokens JWT cerrados con logout | |
| `password_reset_tokens` | tokens de recuperación (se guarda el hash, vence en 30 min) | `usuario_id` |
| `notificaciones_log` | cada correo/SMS/WhatsApp enviado o fallido | |


**Contraseña de todos los usuarios demo: `Ruta#2026`. La del administrador: `Admin#2026`.**

| # | Nombre | Email | Rol | Notas |
|---|---|---|---|---|
| 1 | Administrador RutaOax | admin@rutaoax.com | ADMIN | credencial fija documentada en el README |
| 2 | Marta Valle | negocio1@rutaoax.com | NEGOCIO | negocio "Dulces del Valle" |
| 4 | Luis Martínez | repartidor1@rutaoax.com | REPARTIDOR | empresa: Dulces del Valle |

| 9 | Iván Torres | independiente2@rutaoax.com | REPARTIDOR | independiente |
| 10 | Carla Ríos | cliente1@rutaoax.com | CLIENTE | |

Diagrama de la base de datos 
![Diagrama de la base de datos ](img/diagrama_rutaOax.png)


# proyecto desplegado en el vps 

link del proyecto 
``` https://rutaoax.devs8.tech/login ```

![login](img/proyecto_vps/login.png)


# vista del administardor 
![alt text](img/proyecto_vps/panel_admin.png)

![alt text](img/proyecto_vps/administrador_pedidos.png)

# vista del usuario dueño de un negocio 

![panel negocio](img/proyecto_vps/panel_negocio.png)

![negocio_pedidos](img/proyecto_vps/negocio_pedidos.png)



# repartidor de un negocio

![alt text](img/proyecto_vps/repartidor(negocio)_ruta.png)

![alt text](img/proyecto_vps/repartidor(negocio)_historial.png)

#  repartidor independiente

![alt text](img/proyecto_vps/repartidor(independiente)_entregas.png)

![alt text](img/proyecto_vps/repartidor(independiente)_ruta.png)


## Estados del pedido
`PENDIENTE → ASIGNADO → EN_CAMINO → ENTREGADO | NO_ENTREGADO`
- `PENDIENTE → ASIGNADO`: el negocio asigna o el independiente toma.
- `ASIGNADO → EN_CAMINO`: el repartidor inicia el recorrido (todas sus paradas ASIGNADO pasan a EN_CAMINO).
- `EN_CAMINO → ENTREGADO | NO_ENTREGADO`: lo marca el repartidor en la parada.
- `ASIGNADO → PENDIENTE`: soltar (independiente) o desasignar (negocio), solo antes de EN_CAMINO.
- `NO_ENTREGADO → PENDIENTE`: el negocio lo reasigna.

## cliente 

![alt text](img/proyecto_vps/cliente_pedidos.png)

![alt text](img/proyecto_vps/cliente_pedidos.png)


# Diseño en figma 

https://www.figma.com/design/K1VaaQSKUMMfZGb8G0xqos/RutaSur-Log%C3%ADstica---Mockup?node-id=0-1&p=f&t=v7RFA644sc3QjSh8-0

### estilos y componentes 
paleta de colores, logo, y vistas de los usuarios, y repartidores de un negocio 

![alt text](img/figma/Estilos_componentes.png)
modo prototipo 
![alt text](img/figma/Estilos_componentes_prototipo.png)


### pantallas del administrador

![alt text](img/figma/vistas_admin.png)

![alt text](img/figma/vistas_admin_prototipo.png)

## 
![alt text](img/figma/negocio.png)




# pruebas de bruno 
para usar las prubebas abre bruno y se elcciona en abrir coleccion, busca en donde guardaste el proyecto selecciona la carpeta que dice bruno 
![alt text](img/pruebas_bruno/prueba_login_admin.png)

![alt text](img/pruebas_bruno/registro_usuario.png)

# Información para la evaluación

| | |
|---|---|
| **Sitio (HTTPS)** | https://rutaoax.devs8.tech |
| **URL base de la API** | https://rutaoax.devs8.tech/api |
| **Tablero de GitHub Projects** | https://github.com/users/JoseJose7520-dev/projects/4 |
| **Prototipo en Figma** | https://www.figma.com/design/K1VaaQSKUMMfZGb8G0xqos/RutaSur-Log%C3%ADstica---Mockup?node-id=0-1&p=f&t=v7RFA644sc3QjSh8-0 |

## Credenciales de prueba

**Cuenta de evaluación del administrador** (se crea siempre al arrancar, con datos propios para revisar sin afectar a los demás):

| Correo | Contraseña |
|---|---|
| `admin@rutaoax.com` | `Admin#2026` |

Las demás cuentas de prueba usan la contraseña **`Ruta#2026`**:

| Rol | Correo | Datos de ejemplo |
|---|---|---|
| Negocio | `negocio1@rutaoax.com` | Dulces del Valle, con pedidos, equipo, vehículos y recorridos |
| Negocio | `negocio2@rutaoax.com` | La Espiga |
| Repartidor de empresa | `repartidor1@rutaoax.com` | Tiene ruta asignada para hoy |
| Repartidor independiente | `independiente1@rutaoax.com` | Tiene entregas tomadas y mensajes |
| Cliente | `cliente1@rutaoax.com` | Tiene pedidos con seguimiento y mensajes |

Hay más cuentas: `negocio3` a `negocio10`, `repartidor2` a `repartidor12`, `independiente2` y `cliente2` a `cliente4`, todas `@rutaoax.com` con `Ruta#2026`.

## Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Security, JWT (jjwt), Spring Data JPA / Hibernate, Bean Validation, JavaMailSender |
| Base de datos | MySQL 8, migraciones con Flyway |
| Frontend | React 18, Vite, React Router, Axios, Leaflet (mapas con OpenStreetMap) |
| Comunicación | Postfix + OpenDKIM en el VPS (correo), Twilio (SMS), WhatsApp Cloud API de Meta (WhatsApp) |
| Servidor | VPS Ubuntu, Nginx, Let's Encrypt con Certbot, systemd |
| Pruebas de API | Bruno |
| Diseño y gestión | Figma, GitHub Projects |

## Despliegue en el VPS

Todo corre en el VPS propio del equipo, sin plataformas externas:

- **Backend:** JAR de Spring Boot ejecutado por el servicio de systemd `rutaoax` (`deploy/rutaoax.service`), con la configuración real fuera del repositorio en `/opt/rutaoax/config/application.properties`.
- **Frontend:** build estático de Vite servido por Nginx desde `/var/www/rutaoax`.
- **Nginx** (`deploy/nginx-rutaoax.conf`) sirve el frontend y redirige `/api` al backend.
- **HTTPS** con certificado gratuito de Let's Encrypt generado con Certbot en el propio VPS.
- **MySQL 8** en el mismo servidor.

Para publicar una nueva versión desde Windows: `.\deploy\deploy.ps1` (compila el JAR y el frontend, los sube con `scp` y reinicia el servicio). La preparación inicial del servidor está en `deploy/preparar-servidor.sh`.

## Comunicación con el usuario

| Evento | Canal | Cómo |
|---|---|---|
| Registro de cuenta, recuperación de contraseña | Correo | JavaMailSender hacia Postfix en el propio VPS (`localhost:25`) |
| El repartidor inicia su recorrido (pedido en camino) | SMS | API de Twilio |
| Pedido entregado | WhatsApp + correo | WhatsApp Cloud API de Meta y Postfix |
| Pedido no entregado (con motivo) | WhatsApp | WhatsApp Cloud API de Meta |

- **Correo:** Postfix configurado solo para envío, firmado con OpenDKIM. El dominio `devs8.tech` tiene registros **SPF**, **DKIM** (selector `mail`) y **DMARC**; Gmail los marca como PASS y los correos llegan a la bandeja de entrada.
- Los avisos se envían de forma asíncrona (`@Async`) después de confirmar la transacción, para que una falla externa no deshaga la entrega.
- Cada envío queda registrado en `notificaciones_log` (ENVIADO o FALLIDO), y el administrador lo consulta en *Notificaciones*.

## Seguridad

- Contraseñas guardadas con **BCrypt**. La contraseña pide mínimo 8 caracteres, una mayúscula, un número y un carácter especial; se valida bajo el campo en React y con la anotación `@PasswordSegura` (Bean Validation) en el backend.
- **JWT** en el header `Authorization: Bearer <token>` de cada petición protegida. El logout revoca el token (`tokens_revocados`). La recuperación de contraseña usa un token de un solo uso con vencimiento.
- **Autorización por rol** en `SecurityConfig` y con `@PreAuthorize`. En React, `RoleRoute` protege las rutas y el menú cambia según el rol.
- Errores en JSON con su código correcto (400, 401, 403, 404, 409, 422 y 500) desde `GlobalExceptionHandler` (`@ControllerAdvice`).
- Todos los datos sensibles (JWT, base de datos, correo, Twilio, Meta) viven en `application.properties`, que no se sube. El repositorio solo incluye `application.properties.example`.

## Pruebas de API con Bruno

La colección está en la carpeta [`bruno/`](bruno) y tiene dos entornos: `local` y `produccion`.

1. Abrir Bruno → *Open Collection* → elegir la carpeta `bruno`.
2. Arriba a la derecha, elegir el entorno (`produccion` apunta a `https://rutaoax.devs8.tech/api`).
3. Correr primero `Auth` (login de cada rol). El script de cada login guarda el token en una variable y las demás peticiones lo usan en *Auth → Bearer Token*.

Incluye casos de error a propósito: sin token (401), rol sin permiso (403), recurso inexistente (404), datos duplicados (409), datos inválidos (422) y parámetros faltantes (400).

