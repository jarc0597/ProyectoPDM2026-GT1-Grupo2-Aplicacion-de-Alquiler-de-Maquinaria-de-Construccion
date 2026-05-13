
-- =========================================================
-- Proyecto PDM115 - Aplicacion de Alquiler de Maquinaria
-- Script de base de datos - SQLite 3
-- =========================================================

PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS mantenimientos_maquinaria;
DROP TABLE IF EXISTS reserva_detalle;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS maquinaria;
DROP TABLE IF EXISTS estados_maquinaria;
DROP TABLE IF EXISTS categorias_maquinaria;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS roles_opciones_menu;
DROP TABLE IF EXISTS opciones_menu;
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
    id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_rol TEXT NOT NULL UNIQUE,
    descripcion TEXT NOT NULL
);

CREATE TABLE opciones_menu (
    id_opcion INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_opcion TEXT NOT NULL UNIQUE,
    nombre_opcion TEXT NOT NULL,
    descripcion TEXT NOT NULL
);

CREATE TABLE roles_opciones_menu (
    id_rol INTEGER NOT NULL,
    id_opcion INTEGER NOT NULL,
    PRIMARY KEY (id_rol, id_opcion),
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol),
    FOREIGN KEY (id_opcion) REFERENCES opciones_menu(id_opcion)
);

CREATE TABLE usuarios (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombres TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    telefono TEXT,
    clave_hash TEXT NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    id_rol INTEGER NOT NULL,
    fecha_creacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

CREATE TABLE clientes (
    id_cliente INTEGER PRIMARY KEY AUTOINCREMENT,
    nombres TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    dui TEXT NOT NULL UNIQUE,
    nit TEXT,
    telefono TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    direccion TEXT NOT NULL,
    fecha_registro TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1))
);

CREATE TABLE categorias_maquinaria (
    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_categoria TEXT NOT NULL UNIQUE,
    descripcion TEXT NOT NULL
);

CREATE TABLE estados_maquinaria (
    id_estado INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_estado TEXT NOT NULL UNIQUE,
    descripcion TEXT NOT NULL
);

CREATE TABLE maquinaria (
    id_maquinaria INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_interno TEXT NOT NULL UNIQUE,
    nombre_equipo TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    capacidad TEXT,
    descripcion TEXT NOT NULL,
    costo_hora REAL NOT NULL DEFAULT 0 CHECK (costo_hora >= 0),
    costo_dia REAL NOT NULL DEFAULT 0 CHECK (costo_dia >= 0),
    stock INTEGER NOT NULL DEFAULT 1 CHECK (stock >= 0),
    imagen_url TEXT,
    id_categoria INTEGER NOT NULL,
    id_estado INTEGER NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    FOREIGN KEY (id_categoria) REFERENCES categorias_maquinaria(id_categoria),
    FOREIGN KEY (id_estado) REFERENCES estados_maquinaria(id_estado)
);

CREATE TABLE reservas (
    id_reserva INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_reserva TEXT NOT NULL UNIQUE,
    id_cliente INTEGER NOT NULL,
    fecha_reserva TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT NOT NULL,
    direccion_uso TEXT NOT NULL,
    observaciones TEXT,
    subtotal REAL NOT NULL DEFAULT 0,
    impuesto REAL NOT NULL DEFAULT 0,
    total_estimado REAL NOT NULL DEFAULT 0,
    estado_reserva TEXT NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO','FINALIZADA','CANCELADA')),
    metodo_entrega TEXT NOT NULL DEFAULT 'RETIRO'
        CHECK (metodo_entrega IN ('RETIRO','ENTREGA_SITIO')),
    estado_pago TEXT NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado_pago IN ('PENDIENTE','PAGADO_PARCIAL','PAGADO')),
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CHECK (date(fecha_fin) >= date(fecha_inicio))
);

CREATE TABLE reserva_detalle (
    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
    id_reserva INTEGER NOT NULL,
    id_maquinaria INTEGER NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    tarifa_aplicada REAL NOT NULL DEFAULT 0 CHECK (tarifa_aplicada >= 0),
    modalidad_tarifa TEXT NOT NULL CHECK (modalidad_tarifa IN ('HORA','DIA')),
    subtotal_detalle REAL NOT NULL DEFAULT 0 CHECK (subtotal_detalle >= 0),
    FOREIGN KEY (id_reserva) REFERENCES reservas(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (id_maquinaria) REFERENCES maquinaria(id_maquinaria)
);

CREATE TABLE notificaciones (
    id_notificacion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cliente INTEGER NOT NULL,
    id_reserva INTEGER NOT NULL,
    titulo TEXT NOT NULL,
    mensaje TEXT NOT NULL,
    fecha_programada TEXT NOT NULL,
    fecha_envio TEXT,
    tipo_notificacion TEXT NOT NULL DEFAULT 'RECORDATORIO'
        CHECK (tipo_notificacion IN ('RECORDATORIO','DEVOLUCION','INFORMATIVA')),
    estado_notificacion TEXT NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado_notificacion IN ('PENDIENTE','ENVIADA','ERROR')),
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_reserva) REFERENCES reservas(id_reserva) ON DELETE CASCADE
);

CREATE TABLE mantenimientos_maquinaria (
    id_mantenimiento INTEGER PRIMARY KEY AUTOINCREMENT,
    id_maquinaria INTEGER NOT NULL,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT,
    tipo_mantenimiento TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    costo REAL NOT NULL DEFAULT 0 CHECK (costo >= 0),
    estado_mantenimiento TEXT NOT NULL DEFAULT 'PROGRAMADO'
        CHECK (estado_mantenimiento IN ('PROGRAMADO','EN_PROCESO','FINALIZADO')),
    FOREIGN KEY (id_maquinaria) REFERENCES maquinaria(id_maquinaria),
    CHECK (fecha_fin IS NULL OR date(fecha_fin) >= date(fecha_inicio))
);

CREATE INDEX idx_maquinaria_categoria ON maquinaria(id_categoria);
CREATE INDEX idx_maquinaria_estado ON maquinaria(id_estado);
CREATE INDEX idx_reservas_cliente ON reservas(id_cliente);
CREATE INDEX idx_reservas_fechas ON reservas(fecha_inicio, fecha_fin);
CREATE INDEX idx_detalle_maquinaria ON reserva_detalle(id_maquinaria);
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado_notificacion);
CREATE INDEX idx_mantenimientos_maquinaria ON mantenimientos_maquinaria(id_maquinaria);

CREATE TRIGGER trg_detalle_before_insert
BEFORE INSERT ON reserva_detalle
FOR EACH ROW
BEGIN
    SELECT
        CASE
            WHEN NEW.cantidad > (SELECT stock FROM maquinaria WHERE id_maquinaria = NEW.id_maquinaria)
            THEN RAISE(ABORT, 'La cantidad solicitada supera el stock disponible.')
        END;

    SELECT
        CASE
            WHEN (SELECT em.nombre_estado
                    FROM maquinaria m
                    JOIN estados_maquinaria em ON em.id_estado = m.id_estado
                   WHERE m.id_maquinaria = NEW.id_maquinaria)
                 IN ('Mantenimiento', 'Fuera de servicio')
            THEN RAISE(ABORT, 'La maquinaria no esta disponible para reserva por su estado actual.')
        END;

    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1
                  FROM reserva_detalle rd
                  JOIN reservas r_existente ON r_existente.id_reserva = rd.id_reserva
                  JOIN reservas r_nueva ON r_nueva.id_reserva = NEW.id_reserva
                 WHERE rd.id_maquinaria = NEW.id_maquinaria
                   AND r_existente.estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO')
                   AND date(r_nueva.fecha_inicio) <= date(r_existente.fecha_fin)
                   AND date(r_nueva.fecha_fin) >= date(r_existente.fecha_inicio)
            )
            THEN RAISE(ABORT, 'Ya existe una reserva activa para esa maquinaria en el rango de fechas indicado.')
        END;
END;

CREATE TRIGGER trg_detalle_before_update
BEFORE UPDATE ON reserva_detalle
FOR EACH ROW
BEGIN
    SELECT
        CASE
            WHEN NEW.cantidad > (SELECT stock FROM maquinaria WHERE id_maquinaria = NEW.id_maquinaria)
            THEN RAISE(ABORT, 'La cantidad solicitada supera el stock disponible.')
        END;

    SELECT
        CASE
            WHEN (SELECT em.nombre_estado
                    FROM maquinaria m
                    JOIN estados_maquinaria em ON em.id_estado = m.id_estado
                   WHERE m.id_maquinaria = NEW.id_maquinaria)
                 IN ('Mantenimiento', 'Fuera de servicio')
            THEN RAISE(ABORT, 'La maquinaria no esta disponible para reserva por su estado actual.')
        END;

    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1
                  FROM reserva_detalle rd
                  JOIN reservas r_existente ON r_existente.id_reserva = rd.id_reserva
                  JOIN reservas r_nueva ON r_nueva.id_reserva = NEW.id_reserva
                 WHERE rd.id_maquinaria = NEW.id_maquinaria
                   AND rd.id_detalle <> OLD.id_detalle
                   AND r_existente.estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO')
                   AND date(r_nueva.fecha_inicio) <= date(r_existente.fecha_fin)
                   AND date(r_nueva.fecha_fin) >= date(r_existente.fecha_inicio)
            )
            THEN RAISE(ABORT, 'Ya existe una reserva activa para esa maquinaria en el rango de fechas indicado.')
        END;
END;

CREATE TRIGGER trg_detalle_subtotal_insert
AFTER INSERT ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reserva_detalle
       SET subtotal_detalle = ROUND(NEW.cantidad * NEW.tarifa_aplicada, 2)
     WHERE id_detalle = NEW.id_detalle;

    UPDATE reservas
       SET subtotal = COALESCE((SELECT ROUND(SUM(subtotal_detalle), 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0),
           impuesto = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 0.13, 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0),
           total_estimado = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 1.13, 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0)
     WHERE id_reserva = NEW.id_reserva;
END;

CREATE TRIGGER trg_detalle_subtotal_update
AFTER UPDATE ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reserva_detalle
       SET subtotal_detalle = ROUND(NEW.cantidad * NEW.tarifa_aplicada, 2)
     WHERE id_detalle = NEW.id_detalle;

    UPDATE reservas
       SET subtotal = COALESCE((SELECT ROUND(SUM(subtotal_detalle), 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0),
           impuesto = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 0.13, 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0),
           total_estimado = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 1.13, 2) FROM reserva_detalle WHERE id_reserva = NEW.id_reserva), 0)
     WHERE id_reserva = NEW.id_reserva;
END;

CREATE TRIGGER trg_detalle_subtotal_delete
AFTER DELETE ON reserva_detalle
FOR EACH ROW
BEGIN
    UPDATE reservas
       SET subtotal = COALESCE((SELECT ROUND(SUM(subtotal_detalle), 2) FROM reserva_detalle WHERE id_reserva = OLD.id_reserva), 0),
           impuesto = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 0.13, 2) FROM reserva_detalle WHERE id_reserva = OLD.id_reserva), 0),
           total_estimado = COALESCE((SELECT ROUND(SUM(subtotal_detalle) * 1.13, 2) FROM reserva_detalle WHERE id_reserva = OLD.id_reserva), 0)
     WHERE id_reserva = OLD.id_reserva;
END;
