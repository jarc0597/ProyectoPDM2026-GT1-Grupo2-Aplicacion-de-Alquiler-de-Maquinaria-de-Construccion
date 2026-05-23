
-- =========================================================
-- Proyecto PDM115 - Aplicacion de Alquiler de Maquinaria
-- Script de base de datos - MySQL 8+
-- =========================================================

DROP DATABASE IF EXISTS alquiler_maquinaria_pdm;
CREATE DATABASE alquiler_maquinaria_pdm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alquiler_maquinaria_pdm;

SET NAMES utf8mb4;

-- -----------------------------
-- Tablas de control de acceso
-- -----------------------------
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150) NOT NULL
);

CREATE TABLE opciones_menu (
    id_opcion INT AUTO_INCREMENT PRIMARY KEY,
    codigo_opcion VARCHAR(10) NOT NULL UNIQUE,
    nombre_opcion VARCHAR(80) NOT NULL,
    descripcion VARCHAR(150) NOT NULL
);

CREATE TABLE roles_opciones_menu (
    id_rol INT NOT NULL,
    id_opcion INT NOT NULL,
    PRIMARY KEY (id_rol, id_opcion),
    CONSTRAINT fk_rom_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol),
    CONSTRAINT fk_rom_opcion FOREIGN KEY (id_opcion) REFERENCES opciones_menu(id_opcion)
);

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    clave_hash VARCHAR(255) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    id_rol INT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- -----------------------------
-- Tablas del negocio
-- -----------------------------
CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    dui VARCHAR(10) NOT NULL UNIQUE,
    nit VARCHAR(17),
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    direccion TEXT NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE categorias_maquinaria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(180) NOT NULL
);

CREATE TABLE estados_maquinaria (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(120) NOT NULL
);

CREATE TABLE maquinaria (
    id_maquinaria INT AUTO_INCREMENT PRIMARY KEY,
    codigo_interno VARCHAR(20) NOT NULL UNIQUE,
    nombre_equipo VARCHAR(100) NOT NULL,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(60) NOT NULL,
    capacidad VARCHAR(60),
    descripcion TEXT NOT NULL,
    costo_hora DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    costo_dia DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 1,
    imagen_url VARCHAR(255),
    id_categoria INT NOT NULL,
    id_estado INT NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_maquinaria_categoria FOREIGN KEY (id_categoria) REFERENCES categorias_maquinaria(id_categoria),
    CONSTRAINT fk_maquinaria_estado FOREIGN KEY (id_estado) REFERENCES estados_maquinaria(id_estado),
    CONSTRAINT chk_maquinaria_costos CHECK (costo_hora >= 0 AND costo_dia >= 0),
    CONSTRAINT chk_maquinaria_stock CHECK (stock >= 0)
);

CREATE TABLE reservas (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    codigo_reserva VARCHAR(20) NOT NULL UNIQUE,
    id_cliente INT NOT NULL,
    fecha_reserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    direccion_uso VARCHAR(220) NOT NULL,
    observaciones VARCHAR(255),
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    impuesto DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_estimado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado_reserva VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    metodo_entrega VARCHAR(20) NOT NULL DEFAULT 'RETIRO',
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_reservas_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT chk_reservas_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_reservas_estado CHECK (estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO','FINALIZADA','CANCELADA')),
    CONSTRAINT chk_reservas_metodo CHECK (metodo_entrega IN ('RETIRO','ENTREGA_SITIO')),
    CONSTRAINT chk_reservas_pago CHECK (estado_pago IN ('PENDIENTE','PAGADO_PARCIAL','PAGADO'))
);

CREATE TABLE reserva_detalle (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL,
    id_maquinaria INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    tarifa_aplicada DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    modalidad_tarifa VARCHAR(10) NOT NULL,
    subtotal_detalle DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_detalle_reserva FOREIGN KEY (id_reserva) REFERENCES reservas(id_reserva) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_maquinaria FOREIGN KEY (id_maquinaria) REFERENCES maquinaria(id_maquinaria),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_modalidad CHECK (modalidad_tarifa IN ('HORA','DIA'))
);

CREATE TABLE notificaciones (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_reserva INT NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    fecha_programada DATETIME NOT NULL,
    fecha_envio DATETIME,
    tipo_notificacion VARCHAR(20) NOT NULL DEFAULT 'RECORDATORIO',
    estado_notificacion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_notif_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT fk_notif_reserva FOREIGN KEY (id_reserva) REFERENCES reservas(id_reserva) ON DELETE CASCADE,
    CONSTRAINT chk_notif_tipo CHECK (tipo_notificacion IN ('RECORDATORIO','DEVOLUCION','INFORMATIVA')),
    CONSTRAINT chk_notif_estado CHECK (estado_notificacion IN ('PENDIENTE','ENVIADA','ERROR'))
);

CREATE TABLE mantenimientos_maquinaria (
    id_mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_maquinaria INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    tipo_mantenimiento VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    costo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado_mantenimiento VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADO',
    CONSTRAINT fk_mant_maquinaria FOREIGN KEY (id_maquinaria) REFERENCES maquinaria(id_maquinaria),
    CONSTRAINT chk_mant_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_mant_costo CHECK (costo >= 0),
    CONSTRAINT chk_mant_estado CHECK (estado_mantenimiento IN ('PROGRAMADO','EN_PROCESO','FINALIZADO'))
);

-- -----------------------------
-- Índices
-- -----------------------------
CREATE INDEX idx_maquinaria_categoria ON maquinaria(id_categoria);
CREATE INDEX idx_maquinaria_estado ON maquinaria(id_estado);
CREATE INDEX idx_reservas_cliente ON reservas(id_cliente);
CREATE INDEX idx_reservas_fechas ON reservas(fecha_inicio, fecha_fin);
CREATE INDEX idx_detalle_maquinaria ON reserva_detalle(id_maquinaria);
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado_notificacion);
CREATE INDEX idx_mantenimientos_maquinaria ON mantenimientos_maquinaria(id_maquinaria);

-- -----------------------------
-- Triggers de negocio
-- -----------------------------
DELIMITER $$

CREATE TRIGGER trg_detalle_before_insert
BEFORE INSERT ON reserva_detalle
FOR EACH ROW
BEGIN
    DECLARE v_fecha_inicio DATE;
    DECLARE v_fecha_fin DATE;
    DECLARE v_stock INT;
    DECLARE v_estado VARCHAR(30);
    DECLARE v_existentes INT;

    SELECT fecha_inicio, fecha_fin INTO v_fecha_inicio, v_fecha_fin
    FROM reservas
    WHERE id_reserva = NEW.id_reserva;

    SELECT stock, em.nombre_estado
      INTO v_stock, v_estado
    FROM maquinaria m
    INNER JOIN estados_maquinaria em ON em.id_estado = m.id_estado
    WHERE m.id_maquinaria = NEW.id_maquinaria;

    IF NEW.cantidad > v_stock THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad solicitada supera el stock disponible.';
    END IF;

    IF v_estado IN ('Mantenimiento', 'Fuera de servicio') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La maquinaria no esta disponible para reserva por su estado actual.';
    END IF;

    SELECT COUNT(*)
      INTO v_existentes
    FROM reserva_detalle rd
    INNER JOIN reservas r ON r.id_reserva = rd.id_reserva
    WHERE rd.id_maquinaria = NEW.id_maquinaria
      AND r.estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO')
      AND v_fecha_inicio <= r.fecha_fin
      AND v_fecha_fin >= r.fecha_inicio;

    IF v_existentes > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ya existe una reserva activa para esa maquinaria en el rango de fechas indicado.';
    END IF;

    SET NEW.subtotal_detalle = NEW.cantidad * NEW.tarifa_aplicada;
END$$

CREATE TRIGGER trg_detalle_before_update
BEFORE UPDATE ON reserva_detalle
FOR EACH ROW
BEGIN
    DECLARE v_fecha_inicio DATE;
    DECLARE v_fecha_fin DATE;
    DECLARE v_stock INT;
    DECLARE v_estado VARCHAR(30);
    DECLARE v_existentes INT;

    SELECT fecha_inicio, fecha_fin INTO v_fecha_inicio, v_fecha_fin
    FROM reservas
    WHERE id_reserva = NEW.id_reserva;

    SELECT stock, em.nombre_estado
      INTO v_stock, v_estado
    FROM maquinaria m
    INNER JOIN estados_maquinaria em ON em.id_estado = m.id_estado
    WHERE m.id_maquinaria = NEW.id_maquinaria;

    IF NEW.cantidad > v_stock THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad solicitada supera el stock disponible.';
    END IF;

    IF v_estado IN ('Mantenimiento', 'Fuera de servicio') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La maquinaria no esta disponible para reserva por su estado actual.';
    END IF;

    SELECT COUNT(*)
      INTO v_existentes
    FROM reserva_detalle rd
    INNER JOIN reservas r ON r.id_reserva = rd.id_reserva
    WHERE rd.id_maquinaria = NEW.id_maquinaria
      AND rd.id_detalle <> OLD.id_detalle
      AND r.estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO')
      AND v_fecha_inicio <= r.fecha_fin
      AND v_fecha_fin >= r.fecha_inicio;

    IF v_existentes > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ya existe una reserva activa para esa maquinaria en el rango de fechas indicado.';
    END IF;

    SET NEW.subtotal_detalle = NEW.cantidad * NEW.tarifa_aplicada;
END$$

CREATE TRIGGER trg_recalcular_total_insert
AFTER INSERT ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reservas
       SET subtotal = (SELECT COALESCE(SUM(subtotal_detalle),0) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva),
           impuesto = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 0.13 FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 2),
           total_estimado = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 1.13 FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 2)
     WHERE id_reserva = NEW.id_reserva;
END$$

CREATE TRIGGER trg_recalcular_total_update
AFTER UPDATE ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reservas
       SET subtotal = (SELECT COALESCE(SUM(subtotal_detalle),0) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva),
           impuesto = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 0.13 FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 2),
           total_estimado = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 1.13 FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 2)
     WHERE id_reserva = NEW.id_reserva;
END$$

CREATE TRIGGER trg_recalcular_total_delete
AFTER DELETE ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reservas
       SET subtotal = (SELECT COALESCE(SUM(subtotal_detalle),0) FROM reserva_detalle WHERE id_reserva = OLD.id_reserva),
           impuesto = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 0.13 FROM reserva_detalle WHERE id_reserva = OLD.id_reserva), 2),
           total_estimado = ROUND((SELECT COALESCE(SUM(subtotal_detalle),0) * 1.13 FROM reserva_detalle WHERE id_reserva = OLD.id_reserva), 2)
     WHERE id_reserva = OLD.id_reserva;
END$$

DELIMITER ;
