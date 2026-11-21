CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  telefono VARCHAR(20),
  avatar_url VARCHAR(255),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario_roles (
  usuario_id BIGINT NOT NULL,
  rol_id BIGINT NOT NULL,
  PRIMARY KEY (usuario_id, rol_id),
  CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE negocios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  direccion VARCHAR(200),
  telefono VARCHAR(20),
  propietario_id BIGINT NOT NULL UNIQUE,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_negocio_prop FOREIGN KEY (propietario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE repartidores (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL UNIQUE,
  negocio_id BIGINT NULL,
  CONSTRAINT fk_rep_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  CONSTRAINT fk_rep_negocio FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehiculos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  negocio_id BIGINT NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  placa VARCHAR(20) NOT NULL UNIQUE,
  capacidad INT NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  seguro_vence DATE NULL,
  CONSTRAINT fk_veh_negocio FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE CASCADE,
  CONSTRAINT ck_veh_tipo CHECK (tipo IN ('COMBI','MOTOCICLETA','CAMIONETA','BICICLETA','AUTOMOVIL')),
  CONSTRAINT ck_veh_estado CHECK (estado IN ('ACTIVO','MANTENIMIENTO')),
  CONSTRAINT ck_veh_cap CHECK (capacidad >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recorridos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  repartidor_id BIGINT NOT NULL,
  vehiculo_id BIGINT NULL,
  fecha DATE NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PLANEADO',
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_rec_rep_fecha UNIQUE (repartidor_id, fecha),
  CONSTRAINT fk_rec_rep FOREIGN KEY (repartidor_id) REFERENCES repartidores(id) ON DELETE CASCADE,
  CONSTRAINT fk_rec_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id) ON DELETE SET NULL,
  CONSTRAINT ck_rec_estado CHECK (estado IN ('PLANEADO','EN_CURSO','FINALIZADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pedidos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  negocio_id BIGINT NULL,
  cliente_id BIGINT NULL,
  cliente_nombre VARCHAR(100) NOT NULL,
  cliente_telefono VARCHAR(20),
  direccion VARCHAR(200) NOT NULL,
  zona VARCHAR(60) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  articulos INT NOT NULL DEFAULT 1,
  total DECIMAL(10,2) NOT NULL DEFAULT 0,
  notas VARCHAR(255),
  origen VARCHAR(150),
  pago_repartidor DECIMAL(8,2) NULL,
  modalidad VARCHAR(10) NOT NULL DEFAULT 'NEGOCIO',
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  recorrido_id BIGINT NULL,
  orden_parada INT NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ped_negocio FOREIGN KEY (negocio_id) REFERENCES negocios(id) ON DELETE SET NULL,
  CONSTRAINT fk_ped_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE SET NULL,
  CONSTRAINT fk_ped_recorrido FOREIGN KEY (recorrido_id) REFERENCES recorridos(id) ON DELETE SET NULL,
  CONSTRAINT ck_ped_modalidad CHECK (modalidad IN ('NEGOCIO','ABIERTO')),
  CONSTRAINT ck_ped_estado CHECK (estado IN ('PENDIENTE','ASIGNADO','EN_CAMINO','ENTREGADO','NO_ENTREGADO')),
  CONSTRAINT ck_ped_dueno CHECK (modalidad = 'ABIERTO' OR negocio_id IS NOT NULL),
  INDEX idx_ped_negocio_estado (negocio_id, estado),
  INDEX idx_ped_cliente (cliente_id),
  INDEX idx_ped_abiertos (modalidad, estado),
  INDEX idx_ped_recorrido (recorrido_id, orden_parada),
  INDEX idx_ped_zona (zona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mensajes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pedido_id BIGINT NOT NULL,
  remitente_id BIGINT NOT NULL,
  destinatario_id BIGINT NOT NULL,
  texto VARCHAR(1000) NOT NULL,
  leido BOOLEAN NOT NULL DEFAULT FALSE,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_msg_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_rem FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_dest FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  INDEX idx_msg_pedido (pedido_id, creado_en),
  INDEX idx_msg_dest (destinatario_id, leido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tokens_revocados (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  jti VARCHAR(64) NOT NULL UNIQUE,
  expira_en DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  token_hash VARCHAR(100) NOT NULL UNIQUE,
  expira_en DATETIME NOT NULL,
  usado BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_prt_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notificaciones_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pedido_id BIGINT NULL,
  usuario_id BIGINT NULL,
  canal VARCHAR(10) NOT NULL,
  destino VARCHAR(150) NOT NULL,
  evento VARCHAR(40) NOT NULL,
  estado VARCHAR(10) NOT NULL,
  detalle VARCHAR(255),
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_not_canal CHECK (canal IN ('EMAIL','SMS','WHATSAPP')),
  CONSTRAINT ck_not_estado CHECK (estado IN ('ENVIADO','FALLIDO')),
  INDEX idx_not_creado (creado_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
