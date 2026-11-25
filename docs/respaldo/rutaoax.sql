-- MySQL dump 10.13  Distrib 8.4.13, for Linux (x86_64)
--
-- Host: localhost    Database: rutaoax
-- ------------------------------------------------------
-- Server version	8.4.13-0ubuntu0.26.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','esquema','SQL','V1__esquema.sql',-2005951036,'rutaoax_app','2026-11-21 07:25:58',265,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mensajes`
--

DROP TABLE IF EXISTS `mensajes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mensajes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint NOT NULL,
  `remitente_id` bigint NOT NULL,
  `destinatario_id` bigint NOT NULL,
  `texto` varchar(1000) NOT NULL,
  `leido` tinyint(1) NOT NULL DEFAULT '0',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_msg_rem` (`remitente_id`),
  KEY `idx_msg_pedido` (`pedido_id`,`creado_en`),
  KEY `idx_msg_dest` (`destinatario_id`,`leido`),
  CONSTRAINT `fk_msg_dest` FOREIGN KEY (`destinatario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_msg_rem` FOREIGN KEY (`remitente_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mensajes`
--

LOCK TABLES `mensajes` WRITE;
/*!40000 ALTER TABLE `mensajes` DISABLE KEYS */;
INSERT INTO `mensajes` VALUES (1,11,10,2,'Hola, ¿pueden llegar después de las 5 p. m.?',1,'2026-11-21 01:26:15'),(2,11,2,10,'Claro, avisamos al repartidor.',1,'2026-11-21 01:26:15'),(3,11,10,2,'Muchas gracias.',1,'2026-11-21 01:26:15'),(4,12,11,2,'¿Me confirman la hora estimada de llegada?',0,'2026-11-21 01:26:15'),(5,10,10,8,'Estoy en la puerta azul.',1,'2026-11-21 01:26:15'),(6,10,8,10,'Listo, llego en 5 minutos.',1,'2026-11-21 01:26:15'),(7,10,10,8,'Perfecto, aquí espero.',0,'2026-11-21 01:26:15'),(8,8,12,2,'¿Pueden cambiar la dirección de entrega?',1,'2026-11-21 01:26:15'),(9,8,2,12,'Sí, dime la nueva dirección.',0,'2026-11-21 01:26:15'),(10,13,12,2,'Gracias, ya lo recibí.',1,'2026-11-21 01:26:15'),(11,11,10,2,'Hola',1,'2026-11-23 14:43:34');
/*!40000 ALTER TABLE `mensajes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `negocios`
--

DROP TABLE IF EXISTS `negocios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `negocios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) NOT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `propietario_id` bigint NOT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `propietario_id` (`propietario_id`),
  CONSTRAINT `fk_negocio_prop` FOREIGN KEY (`propietario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `negocios`
--

LOCK TABLES `negocios` WRITE;
/*!40000 ALTER TABLE `negocios` DISABLE KEYS */;
INSERT INTO `negocios` VALUES (1,'Dulces del Valle','Calle Hidalgo 45, Centro Histórico','9515550101',2,'2026-11-21 01:26:14'),(2,'Panadería La Espiga','Av. Independencia 210, Centro Histórico','9515550102',3,'2026-11-21 01:26:14'),(3,'PIZZAS E',NULL,NULL,14,'2026-11-21 12:56:53'),(4,'scomida',NULL,NULL,19,'2026-11-21 13:32:37'),(5,'Mezcalería El Agave','Calle Macedonio Alcalá 401, Centro Histórico','9515550203',22,'2026-11-21 15:04:42'),(6,'Chocolates Mayordomo Norte','Av. Eduardo Mata 1205, Colonia Reforma','9515550204',24,'2026-11-21 15:04:42'),(7,'Tlayudas Doña Mary','Calle Porfirio Díaz 112, Xochimilco','9515550205',26,'2026-11-21 15:04:42'),(8,'Florería Jalatlaco','Calle Aldama 305, Jalatlaco','9515550206',28,'2026-11-21 15:04:43'),(9,'Farmacia San Felipe','Av. San Felipe 88, San Felipe del Agua','9515550207',30,'2026-11-21 15:04:43'),(10,'Papelería Volcanes','Calle Popocatépetl 230, Colonia Volcanes','9515550208',32,'2026-11-21 15:04:43'),(11,'Abarrotes Cinco Señores','Av. Símbolos Patrios 540, Cinco Señores','9515550209',34,'2026-11-21 15:04:43'),(12,'Café Xoxocotlán','Av. Ferrocarril 77, Santa Cruz Xoxocotlán','9515550210',36,'2026-11-21 15:04:43');
/*!40000 ALTER TABLE `negocios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificaciones_log`
--

DROP TABLE IF EXISTS `notificaciones_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificaciones_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `canal` varchar(10) NOT NULL,
  `destino` varchar(150) NOT NULL,
  `evento` varchar(40) NOT NULL,
  `estado` varchar(10) NOT NULL,
  `detalle` varchar(255) DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_not_creado` (`creado_en`),
  CONSTRAINT `ck_not_canal` CHECK ((`canal` in (_utf8mb4'EMAIL',_utf8mb4'SMS',_utf8mb4'WHATSAPP'))),
  CONSTRAINT `ck_not_estado` CHECK ((`estado` in (_utf8mb4'ENVIADO',_utf8mb4'FALLIDO')))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificaciones_log`
--

LOCK TABLES `notificaciones_log` WRITE;
/*!40000 ALTER TABLE `notificaciones_log` DISABLE KEYS */;
INSERT INTO `notificaciones_log` VALUES (1,NULL,14,'EMAIL','pi@gmail.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 12:56:54'),(2,NULL,17,'EMAIL','pau@gmail.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 13:26:29'),(3,NULL,18,'EMAIL','l@gmail.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 13:29:03'),(4,NULL,19,'EMAIL','comidaap26@gmail.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 13:32:37'),(5,NULL,19,'EMAIL','comidaap26@gmail.com','RECUPERACION','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 13:33:28'),(6,NULL,20,'EMAIL','prueba1795292400165@rutaoax.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 14:20:01'),(7,NULL,10,'EMAIL','cliente1@rutaoax.com','RECUPERACION','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 14:20:03'),(8,NULL,38,'EMAIL','prueba1795295133058@rutaoax.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:05:34'),(9,NULL,10,'EMAIL','cliente1@rutaoax.com','RECUPERACION','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:05:36'),(10,NULL,40,'EMAIL','prueba1795296722318@rutaoax.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:32:03'),(11,NULL,10,'EMAIL','cliente1@rutaoax.com','RECUPERACION','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:32:05'),(12,NULL,42,'EMAIL','prueba1795296876684@rutaoax.com','REGISTRO','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:34:37'),(13,NULL,10,'EMAIL','cliente1@rutaoax.com','RECUPERACION','FALLIDO','Mail server connection failed. Failed messages: org.eclipse.angus.mail.util.MailConnectException: Couldn\'t connect to host, port: localhost, 25; timeout 5000;   nested exception is: 	java.net.ConnectException: Connection refused (connect failed)','2026-11-21 15:34:39'),(14,NULL,45,'EMAIL','josejosecuenta.7520@gmail.com','REGISTRO','ENVIADO','Correo entregado al servidor de salida','2026-11-21 20:57:37'),(15,NULL,45,'EMAIL','josejosecuenta.7520@gmail.com','RECUPERACION','ENVIADO','Correo entregado al servidor de salida','2026-11-21 20:59:25'),(16,NULL,NULL,'WHATSAPP','529511147148','PRUEBA','ENVIADO','Aceptado por WhatsApp (wamid.HBgNNTIxOTUxMTE0NzE0OBUCABEYEkY0NTRFMDYxRjZCQzUyNjAwOQ)','2026-11-22 14:22:47'),(17,NULL,NULL,'SMS','+529513292836','PRUEBA','FALLIDO','Twilio respondió 400 (código 572006): Invalid template name. Trial accounts can only use predefined SMS templates.','2026-11-22 14:24:54'),(18,NULL,NULL,'SMS','+529513292836','PRUEBA','ENVIADO','Aceptado por Twilio (SMdc3ff2f25065eb548211c768842da815)','2026-11-22 14:30:45'),(19,NULL,NULL,'WHATSAPP','529511147148','PRUEBA','ENVIADO','Aceptado por WhatsApp (wamid.HBgNNTIxOTUxMTE0NzE0OBUCABEYEjE3RjI3Qjk1NjkzNkJBRUNGRg)','2026-11-22 14:40:27'),(20,NULL,46,'EMAIL','prueba1795573180943@rutaoax.com','REGISTRO','ENVIADO','Correo entregado al servidor de salida','2026-11-24 20:19:43');
/*!40000 ALTER TABLE `notificaciones_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `token_hash` varchar(100) NOT NULL,
  `expira_en` datetime NOT NULL,
  `usado` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_hash` (`token_hash`),
  KEY `fk_prt_usuario` (`usuario_id`),
  CONSTRAINT `fk_prt_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
INSERT INTO `password_reset_tokens` VALUES (1,19,'f684dbcfcce83f8c03c29d1da5ff6dab51cac81e45e2277e221cb23453ae9fcd','2026-11-21 14:03:28',0),(2,10,'635cef403d3daac65b6286a3f409bfb3032c9624179c3042f9618d98766a64d6','2026-11-21 14:50:03',1),(3,10,'bff585abf3650819d9b1773e89c1cdadbe6bf91d35ac9dbb775660924bb1d8af','2026-11-21 15:35:36',1),(4,10,'fd482186125e723d302f5840a1c55976d157c5e6a9137f77b034aa78f8780370','2026-11-21 16:02:05',1),(5,10,'81524f098717eaa7997d6a5218d2c7427e76b1ebf5956ff17664afca6e73d729','2026-11-21 16:04:39',0),(6,45,'20d62e3b8826cb7d6e4d417fed143d1d6e8f0377f67c800ff80db04a3aefb4a5','2026-11-21 21:29:24',1);
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `negocio_id` bigint DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `cliente_nombre` varchar(100) NOT NULL,
  `cliente_telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(200) NOT NULL,
  `zona` varchar(60) NOT NULL,
  `lat` decimal(9,6) NOT NULL,
  `lng` decimal(9,6) NOT NULL,
  `articulos` int NOT NULL DEFAULT '1',
  `total` decimal(10,2) NOT NULL DEFAULT '0.00',
  `notas` varchar(255) DEFAULT NULL,
  `origen` varchar(150) DEFAULT NULL,
  `pago_repartidor` decimal(8,2) DEFAULT NULL,
  `modalidad` varchar(10) NOT NULL DEFAULT 'NEGOCIO',
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  `recorrido_id` bigint DEFAULT NULL,
  `orden_parada` int DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ped_negocio_estado` (`negocio_id`,`estado`),
  KEY `idx_ped_cliente` (`cliente_id`),
  KEY `idx_ped_abiertos` (`modalidad`,`estado`),
  KEY `idx_ped_recorrido` (`recorrido_id`,`orden_parada`),
  KEY `idx_ped_zona` (`zona`),
  CONSTRAINT `fk_ped_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ped_negocio` FOREIGN KEY (`negocio_id`) REFERENCES `negocios` (`id`),
  CONSTRAINT `fk_ped_recorrido` FOREIGN KEY (`recorrido_id`) REFERENCES `recorridos` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_ped_dueno` CHECK (((`modalidad` = _utf8mb4'ABIERTO') or (`negocio_id` is not null))),
  CONSTRAINT `ck_ped_estado` CHECK ((`estado` in (_utf8mb4'PENDIENTE',_utf8mb4'ASIGNADO',_utf8mb4'EN_CAMINO',_utf8mb4'ENTREGADO',_utf8mb4'NO_ENTREGADO'))),
  CONSTRAINT `ck_ped_modalidad` CHECK ((`modalidad` in (_utf8mb4'NEGOCIO',_utf8mb4'ABIERTO')))
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES (1,NULL,10,'Carla Ríos','9515550109','Calle Hidalgo 22','Centro Histórico',17.062747,-96.726019,3,420.00,NULL,'Mercado 20 de Noviembre',40.00,'ABIERTO','ASIGNADO',23,1,'2026-11-21 01:26:15','2026-11-23 20:45:43'),(2,NULL,11,'Mario Cruz','9515550110','Av. Universidad 310','Xochimilco',17.073849,-96.723788,1,180.00,NULL,'Farmacia Centro',55.00,'ABIERTO','ASIGNADO',24,1,'2026-11-21 01:26:15','2026-11-25 01:43:08'),(3,1,NULL,'Lucía Vera','9515550120','Calle Murguía 8','Jalatlaco',17.068148,-96.721032,2,260.00,NULL,'Dulces del Valle',45.00,'ABIERTO','PENDIENTE',NULL,NULL,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(4,1,12,'Elena Gómez','9515550111','Calle Reforma 71','Colonia Reforma',17.076345,-96.712650,1,150.00,NULL,'Dulces del Valle',50.00,'ABIERTO','PENDIENTE',NULL,NULL,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(5,1,NULL,'Sofía León','9515550121','Av. Juárez 105','Centro Histórico',17.062744,-96.728594,4,540.00,NULL,NULL,NULL,'NEGOCIO','PENDIENTE',NULL,NULL,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(6,1,NULL,'Tomás Aguilar','9515550122','Calle Alcalá 330','Centro Histórico',17.062746,-96.724663,2,310.00,NULL,NULL,NULL,'NEGOCIO','PENDIENTE',NULL,NULL,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(7,2,NULL,'Beatriz Núñez','9515550123','Av. Independencia 801','Colonia América',17.071846,-96.728007,5,625.00,NULL,NULL,NULL,'NEGOCIO','PENDIENTE',NULL,NULL,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(8,1,12,'Elena Gómez','9515550111','Calle Murguía 8','Jalatlaco',17.068142,-96.716825,2,275.00,NULL,NULL,NULL,'NEGOCIO','ASIGNADO',2,1,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(9,1,13,'Raúl Silva','9515550112','Av. Juárez 105','Colonia Reforma',17.076341,-96.713869,1,120.00,NULL,NULL,NULL,'NEGOCIO','ASIGNADO',2,2,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(10,NULL,10,'Carla Ríos','9515550109','Calle Hidalgo 22','Centro Histórico',17.062743,-96.727238,2,210.00,NULL,'Panadería La Espiga',45.00,'ABIERTO','ASIGNADO',4,1,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(11,1,10,'Carla Ríos','9515550109','Calle Hidalgo 22','Centro Histórico',17.062743,-96.725881,3,420.00,NULL,NULL,NULL,'NEGOCIO','EN_CAMINO',1,1,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(12,1,11,'Mario Cruz','9515550110','Av. Universidad 310','Xochimilco',17.073839,-96.728799,1,180.00,NULL,NULL,NULL,'NEGOCIO','EN_CAMINO',1,2,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(13,1,12,'Elena Gómez','9515550111','Calle Reforma 71','Colonia Reforma',17.076338,-96.716443,2,230.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',5,1,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(14,1,13,'Raúl Silva','9515550112','Calle Alcalá 140','Centro Histórico',17.062741,-96.721812,1,95.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',6,1,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(15,1,NULL,'Daniela Ortiz','9515550124','Camino Real 12','Santa Lucía del Camino',17.072540,-96.693756,2,205.00,NULL,NULL,NULL,'NEGOCIO','NO_ENTREGADO',6,2,'2026-11-21 01:26:15','2026-11-21 01:26:15'),(16,3,NULL,'pepe',NULL,'Calle Hidalgo 22','Centro Histórico',17.062759,-96.725077,5,1240.00,NULL,NULL,NULL,'NEGOCIO','ASIGNADO',7,1,'2026-11-21 13:22:07','2026-11-21 13:22:22'),(17,3,NULL,'alberto',NULL,'Av. Universidad 310','Xochimilco',17.073858,-96.725421,1,129.00,NULL,NULL,NULL,'NEGOCIO','ASIGNADO',7,2,'2026-11-21 13:23:33','2026-11-21 13:23:56'),(21,1,NULL,'Erica Alonso',NULL,'Av Mina, Oaxaca','Centro Histórico',17.062756,-96.726296,1,150.00,'Parada Bus','Dulces del Valle',10.00,'NEGOCIO','ASIGNADO',9,1,'2026-11-21 14:32:02','2026-11-21 14:34:22'),(22,1,10,'Patricia Luna','9515551234','Calle García Vigil 512','Centro Histórico',17.062758,-96.722365,2,350.00,'Tocar el timbre dos veces',NULL,NULL,'NEGOCIO','ASIGNADO',9,2,'2026-11-21 14:36:49','2026-11-21 14:37:07'),(23,5,NULL,'Patricia Luna','9515550403','Calle 10 de Centro Histórico','Centro Histórico',17.062676,-96.727740,1,150.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',10,1,'2026-11-21 15:04:42','2026-11-21 15:04:42'),(24,6,NULL,'Andrés Vásquez','9515550404','Calle 17 de Colonia Reforma','Colonia Reforma',17.076275,-96.717084,2,185.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',11,1,'2026-11-21 15:04:42','2026-11-21 15:04:42'),(25,7,NULL,'Gabriela Mendoza','9515550405','Calle 24 de Xochimilco','Xochimilco',17.073778,-96.724153,3,220.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',12,1,'2026-11-21 15:04:42','2026-11-21 15:04:42'),(26,8,NULL,'Verónica Castillo','9515550406','Calle 31 de Jalatlaco','Jalatlaco',17.068077,-96.721397,4,255.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',13,1,'2026-11-21 15:04:43','2026-11-21 15:04:43'),(27,9,NULL,'Jaime Ortega','9515550407','Calle 38 de San Felipe del Agua','San Felipe del Agua',17.103773,-96.715015,1,290.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',14,1,'2026-11-21 15:04:43','2026-11-21 15:04:43'),(28,10,NULL,'Rocío Hernández','9515550408','Calle 45 de Colonia Volcanes','Colonia Volcanes',17.085272,-96.713658,2,325.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',15,1,'2026-11-21 15:04:43','2026-11-21 15:04:43'),(29,11,NULL,'Lorena Aquino','9515550409','Calle 52 de Cinco Señores','Cinco Señores',17.043275,-96.707827,3,360.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',16,1,'2026-11-21 15:04:43','2026-11-21 15:04:43'),(30,12,NULL,'Felipe Cervantes','9515550410','Calle 59 de Santa Cruz Xoxocotlán','Santa Cruz Xoxocotlán',17.025974,-96.735371,4,395.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',17,1,'2026-11-21 15:04:43','2026-11-21 15:04:43'),(40,1,10,'Carla Ríos','9515550109','Calle García Vigil 512','Centro Histórico',17.062719,-96.729122,2,350.00,NULL,NULL,NULL,'NEGOCIO','ENTREGADO',21,1,'2026-11-21 15:35:04','2026-11-21 15:35:08'),(41,1,NULL,'Andrés Vásquez','9515554321','Av. Juárez 210','Jalatlaco',17.068118,-96.720065,1,180.00,'No entregado: No había nadie en el domicilio',NULL,NULL,'NEGOCIO','NO_ENTREGADO',21,2,'2026-11-21 15:35:04','2026-11-21 15:35:09'),(42,1,NULL,'Sergio Maldonado',NULL,'Calle García Vigil 512','Centro Histórico',17.062721,-96.723834,2,300.00,NULL,NULL,NULL,'NEGOCIO','ASIGNADO',22,1,'2026-11-21 20:18:46','2026-11-21 20:19:08');
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recorridos`
--

DROP TABLE IF EXISTS `recorridos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recorridos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `repartidor_id` bigint NOT NULL,
  `vehiculo_id` bigint DEFAULT NULL,
  `fecha` date NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PLANEADO',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rec_rep_fecha` (`repartidor_id`,`fecha`),
  KEY `fk_rec_veh` (`vehiculo_id`),
  CONSTRAINT `fk_rec_rep` FOREIGN KEY (`repartidor_id`) REFERENCES `repartidores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rec_veh` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculos` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_rec_estado` CHECK ((`estado` in (_utf8mb4'PLANEADO',_utf8mb4'EN_CURSO',_utf8mb4'FINALIZADO')))
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recorridos`
--

LOCK TABLES `recorridos` WRITE;
/*!40000 ALTER TABLE `recorridos` DISABLE KEYS */;
INSERT INTO `recorridos` VALUES (1,1,1,'2026-11-21','EN_CURSO','2026-11-21 01:26:15'),(2,2,2,'2026-11-21','PLANEADO','2026-11-21 01:26:15'),(3,4,7,'2026-11-21','PLANEADO','2026-11-21 01:26:15'),(4,5,NULL,'2026-11-21','PLANEADO','2026-11-21 01:26:15'),(5,1,1,'2026-11-20','FINALIZADO','2026-11-21 01:26:15'),(6,2,2,'2026-11-20','FINALIZADO','2026-11-21 01:26:15'),(7,8,11,'2026-11-21','PLANEADO','2026-11-21 13:22:22'),(9,1,3,'2026-11-22','PLANEADO','2026-11-21 14:34:22'),(10,11,15,'2026-11-19','FINALIZADO','2026-11-21 15:04:42'),(11,12,16,'2026-11-18','FINALIZADO','2026-11-21 15:04:42'),(12,13,17,'2026-11-17','FINALIZADO','2026-11-21 15:04:42'),(13,14,18,'2026-11-16','FINALIZADO','2026-11-21 15:04:43'),(14,15,19,'2026-11-15','FINALIZADO','2026-11-21 15:04:43'),(15,16,20,'2026-11-14','FINALIZADO','2026-11-21 15:04:43'),(16,17,21,'2026-11-13','FINALIZADO','2026-11-21 15:04:43'),(17,18,22,'2026-11-12','FINALIZADO','2026-11-21 15:04:43'),(21,22,NULL,'2026-11-21','FINALIZADO','2026-11-21 15:35:05'),(22,3,3,'2026-11-21','PLANEADO','2026-11-21 20:19:08'),(23,5,NULL,'2026-11-23','PLANEADO','2026-11-23 14:45:43'),(24,6,NULL,'2026-11-24','PLANEADO','2026-11-24 19:43:08');
/*!40000 ALTER TABLE `recorridos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repartidores`
--

DROP TABLE IF EXISTS `repartidores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repartidores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `negocio_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `usuario_id` (`usuario_id`),
  KEY `fk_rep_negocio` (`negocio_id`),
  CONSTRAINT `fk_rep_negocio` FOREIGN KEY (`negocio_id`) REFERENCES `negocios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_rep_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repartidores`
--

LOCK TABLES `repartidores` WRITE;
/*!40000 ALTER TABLE `repartidores` DISABLE KEYS */;
INSERT INTO `repartidores` VALUES (1,4,1),(2,5,1),(3,6,1),(4,7,2),(5,8,NULL),(6,9,NULL),(7,15,3),(8,16,3),(9,18,NULL),(10,21,1),(11,23,5),(12,25,6),(13,27,7),(14,29,8),(15,31,9),(16,33,10),(17,35,11),(18,37,12),(19,39,1),(20,41,1),(21,43,1),(22,44,1);
/*!40000 ALTER TABLE `repartidores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(4,'CLIENTE'),(2,'NEGOCIO'),(3,'REPARTIDOR');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tokens_revocados`
--

DROP TABLE IF EXISTS `tokens_revocados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tokens_revocados` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `jti` varchar(64) NOT NULL,
  `expira_en` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `jti` (`jti`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tokens_revocados`
--

LOCK TABLES `tokens_revocados` WRITE;
/*!40000 ALTER TABLE `tokens_revocados` DISABLE KEYS */;
INSERT INTO `tokens_revocados` VALUES (16,'ffd74f7b-7782-46a3-9a53-1eb53b9d82bd','2026-11-24 06:07:08'),(17,'06124fd2-23cd-4398-8b49-d6fde929ed65','2026-11-24 06:17:14'),(18,'2d33163f-e93f-458d-a7f9-c6c6118c0f60','2026-11-24 06:28:26'),(19,'7c537db8-45fc-43e5-a7f3-e96d1109866d','2026-11-24 06:29:02'),(20,'3b965770-424e-4a8c-a22c-53f5083de238','2026-11-24 06:29:38'),(21,'257a98d0-4101-4b06-a4f5-8a9026763538','2026-11-25 03:28:27'),(22,'b2b39788-e50d-4cba-94b5-96ea40b8065e','2026-11-25 03:33:47'),(23,'7eeec907-86b8-4492-b1e4-7b0a5387158b','2026-11-25 03:38:03'),(24,'8531fbb8-e47b-4fd3-be19-e7655ba609f5','2026-11-25 03:39:02'),(25,'a95781e5-51bd-4763-b683-7d4e4349f752','2026-11-25 03:42:19'),(26,'1acba3bd-b68b-495d-a1cf-21957bc86301','2026-11-25 03:47:09');
/*!40000 ALTER TABLE `tokens_revocados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_roles`
--

DROP TABLE IF EXISTS `usuario_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `fk_ur_rol` (`rol_id`),
  CONSTRAINT `fk_ur_rol` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_ur_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_roles`
--

LOCK TABLES `usuario_roles` WRITE;
/*!40000 ALTER TABLE `usuario_roles` DISABLE KEYS */;
INSERT INTO `usuario_roles` VALUES (1,1),(2,2),(3,2),(14,2),(19,2),(22,2),(24,2),(26,2),(28,2),(30,2),(32,2),(34,2),(36,2),(4,3),(5,3),(6,3),(7,3),(8,3),(9,3),(15,3),(16,3),(18,3),(21,3),(23,3),(25,3),(27,3),(29,3),(31,3),(33,3),(35,3),(37,3),(39,3),(41,3),(43,3),(44,3),(10,4),(11,4),(12,4),(13,4),(17,4),(20,4),(38,4),(40,4),(42,4),(45,4),(46,4);
/*!40000 ALTER TABLE `usuario_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'Administrador RutaOax','admin@rutaoax.com','$2a$10$E4NQqtqGxRJbg7nl2ShVGuHTKMgdrY5iRYTdAeJYtz1JeQcabZ/EO','9515550100',NULL,1,'2026-11-21 01:26:14'),(2,'Marta Valle','negocio1@rutaoax.com','$2a$10$I53jQxGj41iwxV/1UpX7o.2EXEuzaKdDU6wvTL/vowB.nHb1PL9f6','9515550101',NULL,1,'2026-11-21 01:26:14'),(3,'Héctor Mora','negocio2@rutaoax.com','$2a$10$weS2OqS.pOe2B28B/QczzOld.YDU0QiXucXXvqceTesTJxTwRqBT6','9515550102',NULL,1,'2026-11-21 01:26:14'),(4,'Luis Martínez','repartidor1@rutaoax.com','$2a$10$F23qXJkaCbWnYxDPpsfMcesFa1Xno1z7PwDNLqZ9vGMiPI08lV73W','9515550103',NULL,1,'2026-11-21 01:26:14'),(5,'Ana Ruiz','repartidor2@rutaoax.com','$2a$10$.FS2tCQAWu.6D2PxHXYOMuOoVAFyki62iviZPi7s3IziGkoxK2yO6','9515550104',NULL,1,'2026-11-21 01:26:14'),(6,'Jorge Pérez','repartidor3@rutaoax.com','$2a$10$kh54QaOmSxpLb83PypY9jOZ.JPMnrejw13miUeAtf3kTamteEa0te','9515550105',NULL,1,'2026-11-21 01:26:14'),(7,'Rosa Cruz','repartidor4@rutaoax.com','$2a$10$pel.e6pdbQ76jQwxiZzUWeMqprAHvbmpDlqGK38lxjHHERoCiUTqS','9515550106',NULL,1,'2026-11-21 01:26:15'),(8,'Pablo Hernández','independiente1@rutaoax.com','$2a$10$IkOQdXDUlFTbps0GI39eZegY1fueVG5lz/DZc/C3QSy94ZeatOAKe','9515550107',NULL,1,'2026-11-21 01:26:15'),(9,'Iván Torres','independiente2@rutaoax.com','$2a$10$klmbdq.H.zmDrYkK8hdaM.HnhqGx3Z.aToWkltvWpti8gBCmM6iEK','9515550108',NULL,1,'2026-11-21 01:26:15'),(10,'Carla Ríos','cliente1@rutaoax.com','$2a$10$IXcw7y65itSTFNvFlAjgL.FfyfZU91gNunMbz0aEM8MOk822WNT8K','9515550110','https://www.google.com/url?sa=t&source=web&rct=j&url=https%3A%2F%2Fwww.magnific.com%2Fes%2Ffotos-vectores-gratis%2Fperfil-pandas&ved=0CBcQjRxqFwoTCMjJrc3Vn5gDFQAAAAAdAAAAABBz&opi=89978449',1,'2026-11-21 01:26:15'),(11,'Mario Cruz','cliente2@rutaoax.com','$2a$10$dL2hTLoUlwwbQ1/.OM6im.bSJJQPCvol3barWZhCIQqEZG7nAb5Oi','9515550110',NULL,1,'2026-11-21 01:26:15'),(12,'Elena Gómez','cliente3@rutaoax.com','$2a$10$kvBrF4rZ4XR8Q5Hkf7vk8OboHBXB92aLU3WIk0TEvcpe8QkerUotO','9515550111',NULL,1,'2026-11-21 01:26:15'),(13,'Raúl Silva','cliente4@rutaoax.com','$2a$10$XECdv4gtPKuXzBYBhHPl2OASOpJMWXcNh9rNDXUkYrMqmhptySnR2','9515550112',NULL,1,'2026-11-21 01:26:15'),(14,'PIZZAS E','pi@gmail.com','$2a$10$JoF8CxsYJd7Xg6JMqgurKu6as06qzmNVZLKNX6zNu7ydJ.L3aj7lm',NULL,NULL,1,'2026-11-21 12:56:53'),(15,'jose','jos@gmail.com','$2a$10$zubjDI1OmGGKESncaskUHuIMcb2et/CFg.mCHfGjkGrHmjtxEftwG',NULL,NULL,1,'2026-11-21 12:59:29'),(16,'erick','eri@gmail.com','$2a$10$W0O75xDudqvKUs2S/IIQJOIWfU.h/BAODfQQO.zjI78ga7ivH3.VG',NULL,NULL,1,'2026-11-21 13:00:52'),(17,'pauliño','pau@gmail.com','$2a$10$yy1pl7D.0hTJWNueBOC83OBXImjjJGaGLq69mRsPCeOCeawhr/bnu',NULL,NULL,1,'2026-11-21 13:26:29'),(18,'luismi','l@gmail.com','$2a$10$KwwapeJz5uqUX7BX8ueVdOgYPChCYxxwIGSKryiKkLep6CYdcErVO',NULL,NULL,1,'2026-11-21 13:29:03'),(19,'scomida:)','comidaap26@gmail.com','$2a$10$o3A2Aph/fg0H8ga7fYOrbeM6m/EBznMgaSa2lEl8Rsb418KGEhJ3K',NULL,NULL,1,'2026-11-21 13:32:37'),(20,'Usuario Prueba','prueba1795292400165@rutaoax.com','$2a$10$WSudtOmoPfys.Bs6RD0KDOYAZVSAe7fyrHUjh6BJpiLFz3y/Mk5Q2','9515550199',NULL,1,'2026-11-21 14:20:01'),(21,'Repartidor Prueba Editado','miembro1795292406375@rutaoax.com','$2a$10$5nS4t1OtTX9NRIdxPdiCG.2BgFzeRlhtD/ydSDOP2qmOf5exYydXm',NULL,NULL,0,'2026-11-21 14:20:07'),(22,'Sergio Ramos','negocio3@rutaoax.com','$2a$10$xg7aAG8xWViQHDvOoakXcucZn2S7eMqkDoJOZ/vu5oVVNJelZRkIe','9515550203',NULL,1,'2026-11-21 15:04:42'),(23,'Diego Santiago','repartidor5@rutaoax.com','$2a$10$Ji5ERtwrLQZKQ.Z35C90R.ew/j0qu95IRFFYNHQ2MKTvmNXwQqaY.','9515550305',NULL,1,'2026-11-21 15:04:42'),(24,'Laura Díaz','negocio4@rutaoax.com','$2a$10$0BXIhliQPLZhg8PhS3iSa.0Nh/P.N/nbYiK.5JUezcLBCjZWrf34i','9515550204',NULL,1,'2026-11-21 15:04:42'),(25,'Fernando López','repartidor6@rutaoax.com','$2a$10$oHAa8Fla/56aCPb5ws44we9sTrbKtFSUmGBBpZxzEERetEBpo0ctq','9515550306',NULL,1,'2026-11-21 15:04:42'),(26,'María Bautista','negocio5@rutaoax.com','$2a$10$OvMpsYLIqGFcEZMf1QvbVuBRECFDz405137k11CGL1TWOzSqEdri6','9515550205',NULL,1,'2026-11-21 15:04:42'),(27,'Ricardo Jiménez','repartidor7@rutaoax.com','$2a$10$vXgkfQ7TABVo3H8.WUV10uT0KXoLHjvAmSBVqmz9jyutn6mCf9fIK','9515550307',NULL,1,'2026-11-21 15:04:42'),(28,'Claudia Reyes','negocio6@rutaoax.com','$2a$10$ToKpmpWN9.puL9wj/aHcJu/N4dIgrgHqgH45omCfoirqDp96W46ZO','9515550206',NULL,1,'2026-11-21 15:04:43'),(29,'Hugo Martínez','repartidor8@rutaoax.com','$2a$10$7umLmDREMa3CK9Yb4y7vLOqVHwhdxXWF7/RWO2w.wRNJKiRuODfC6','9515550308',NULL,1,'2026-11-21 15:04:43'),(30,'Arturo Sánchez','negocio7@rutaoax.com','$2a$10$9a0wwK6Xood6IvN9qaJ8ju/7L6mNm/lUK55UrmzqVXP6XUwGeC0Ua','9515550207',NULL,1,'2026-11-21 15:04:43'),(31,'Óscar Morales','repartidor9@rutaoax.com','$2a$10$Fwn4722Rcp5xi593zJrAx.t3PJVgBaOeFw8fXI8qrGS.N2oVq./TW','9515550309',NULL,1,'2026-11-21 15:04:43'),(32,'Adriana Flores','negocio8@rutaoax.com','$2a$10$mjZX4T.r3CFiv4b1a1nmsuEX1NFdsHpXdd1GBeWdBil6BCgT81DNG','9515550208',NULL,1,'2026-11-21 15:04:43'),(33,'Emilio García','repartidor10@rutaoax.com','$2a$10$gSTyiDo7mvYxQiFJinSI6.N9af8GcMUnrWxwMKCKm7IuWW01Nd2v6','9515550310',NULL,1,'2026-11-21 15:04:43'),(34,'Gerardo Velasco','negocio9@rutaoax.com','$2a$10$H9FfXk7oMC4Pdvv8DMJKa.cFyoSn9R1cVTaEABBzBUQdhzrcXU.OC','9515550209',NULL,1,'2026-11-21 15:04:43'),(35,'Samuel Pacheco','repartidor11@rutaoax.com','$2a$10$0ibQlLs6WmJMWhmHflROauzu4FlMWzx5lyElVJOJ3cTK2gpK8pBt.','9515550311',NULL,1,'2026-11-21 15:04:43'),(36,'Isabel Zárate','negocio10@rutaoax.com','$2a$10$1TJGnYHc2qHl4GaQ1uRbF.dBSyOeTyA1ig6MFIujv1DxuCwj0V2ZO','9515550210',NULL,1,'2026-11-21 15:04:43'),(37,'Manuel Gutiérrez','repartidor12@rutaoax.com','$2a$10$FvdrKr3aFKlEPHYv50VyiO5n9DcAdvC7pb7GtIiCG2ojfVZVX.d02','9515550312',NULL,1,'2026-11-21 15:04:43'),(38,'Usuario Prueba','prueba1795295133058@rutaoax.com','$2a$10$kc8lrFYZBc3FC4.cC9Rv2.dd1M8kpzZNexo8ecCaGo1F19t/WgmqW','9515550199',NULL,1,'2026-11-21 15:05:33'),(39,'Repartidor Prueba Editado','miembro1795295139373@rutaoax.com','$2a$10$owwoqJPUedYH2lBZYBTU/exgvQmqJSU8VysGrg17QqS.CUAGI/bUO',NULL,NULL,0,'2026-11-21 15:05:40'),(40,'Usuario Prueba','prueba1795296722318@rutaoax.com','$2a$10$6fI4eL9OPWrfMB5sEAaeEe7qnpVCTEPzGwjFYeSd4ZgsHZIgrInFu','9515550199',NULL,1,'2026-11-21 15:32:03'),(41,'Repartidor Prueba Editado','miembro1795296728073@rutaoax.com','$2a$10$Jhb23jub4Rbz2C09iIKePOaBX7Q3FvWgtgwsCrHbyZBuaPjHybjOu',NULL,NULL,0,'2026-11-21 15:32:08'),(42,'Usuario Prueba','prueba1795296876684@rutaoax.com','$2a$10$kdNyr7B/VIJ9Hy5E3qjzgO2dzV1HAK9FbObvlBpbF0ErDAtHx3T4e','9515550199',NULL,1,'2026-11-21 15:34:37'),(43,'Repartidor Prueba Editado','miembro1795296881856@rutaoax.com','$2a$10$mDsJzoReVCqUneX2LJZlteui8UrLnCQL9Npm891d/F6extSmGYV.i',NULL,NULL,0,'2026-11-21 15:34:42'),(44,'Repartidor Flujo','flujo1795296902560@rutaoax.com','$2a$10$WpBAoUFcArcEDFXcfwEjbei0YL.HcftuNDFFkHzqSHAdDghN.qE/a','9515550170',NULL,0,'2026-11-21 15:35:03'),(45,'Osvaldo Jose','josejosecuenta.7520@gmail.com','$2a$10$h.aY5bopf1Vr3G2Gm2EYbuQrLaZnJR6iZLy37fx4aixB3M/tviwfy',NULL,NULL,1,'2026-11-21 20:57:37'),(46,'Usuario Prueba','prueba1795573180943@rutaoax.com','$2a$10$u/fnQIXbPJX2p/Nc5jJynuFkCzaXJgNsHzAWGney23xOkElQJx/9e','9515550199',NULL,1,'2026-11-24 20:19:42');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehiculos`
--

DROP TABLE IF EXISTS `vehiculos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehiculos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `negocio_id` bigint NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `placa` varchar(20) NOT NULL,
  `capacidad` int NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'ACTIVO',
  `seguro_vence` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `placa` (`placa`),
  KEY `fk_veh_negocio` (`negocio_id`),
  CONSTRAINT `fk_veh_negocio` FOREIGN KEY (`negocio_id`) REFERENCES `negocios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_veh_cap` CHECK ((`capacidad` >= 1)),
  CONSTRAINT `ck_veh_estado` CHECK ((`estado` in (_utf8mb4'ACTIVO',_utf8mb4'MANTENIMIENTO'))),
  CONSTRAINT `ck_veh_tipo` CHECK ((`tipo` in (_utf8mb4'COMBI',_utf8mb4'MOTOCICLETA',_utf8mb4'CAMIONETA',_utf8mb4'BICICLETA',_utf8mb4'AUTOMOVIL')))
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculos`
--

LOCK TABLES `vehiculos` WRITE;
/*!40000 ALTER TABLE `vehiculos` DISABLE KEYS */;
INSERT INTO `vehiculos` VALUES (1,1,'MOTOCICLETA','OAX-214',10,'ACTIVO','2027-06-09'),(2,1,'CAMIONETA','OAX-880',30,'ACTIVO','2026-11-26'),(3,1,'BICICLETA','BIC-03',4,'ACTIVO',NULL),(4,1,'MOTOCICLETA','OAX-301',10,'ACTIVO','2027-03-21'),(5,1,'COMBI','OAX-502',40,'MANTENIMIENTO','2027-02-19'),(6,1,'AUTOMOVIL','OAX-777',15,'ACTIVO','2027-09-17'),(7,2,'MOTOCICLETA','OAX-120',8,'ACTIVO','2027-04-20'),(8,2,'CAMIONETA','OAX-455',25,'ACTIVO','2027-06-19'),(9,2,'BICICLETA','BIC-11',4,'ACTIVO',NULL),(10,2,'AUTOMOVIL','OAX-908',12,'ACTIVO','2027-01-20'),(11,3,'COMBI','THA-TMZ',22,'ACTIVO',NULL),(12,3,'MOTOCICLETA','OAX-TMZ',4,'ACTIVO',NULL),(15,5,'CAMIONETA','OAX-610',25,'ACTIVO','2027-02-19'),(16,6,'MOTOCICLETA','OAX-611',10,'ACTIVO','2027-03-11'),(17,7,'MOTOCICLETA','OAX-612',8,'ACTIVO','2027-03-31'),(18,8,'AUTOMOVIL','OAX-613',12,'ACTIVO','2027-04-20'),(19,9,'BICICLETA','BIC-14',4,'ACTIVO',NULL),(20,10,'MOTOCICLETA','OAX-615',10,'ACTIVO','2027-05-30'),(21,11,'CAMIONETA','OAX-616',30,'ACTIVO','2027-06-19'),(22,12,'COMBI','OAX-617',40,'ACTIVO','2027-07-09');
/*!40000 ALTER TABLE `vehiculos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-25  3:06:26
