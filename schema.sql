-- Script de creación de la base de datos
-- Motor: MySQL 8
-- Crea la base, el usuario soporte_user (contraseña 123456) y las tablas con datos de prueba.
-- Reiniciar los datos de prueba.

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS sistema_soporte
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sistema_soporte;

-- Usuario de la aplicación. Si ya existe, no se toca.
CREATE USER IF NOT EXISTS 'soporte_user'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON sistema_soporte.* TO 'soporte_user'@'localhost';
FLUSH PRIVILEGES;

-- Se eliminan las tablas en orden inverso a sus dependencias
DROP TABLE IF EXISTS historial_cambios;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS usuarios;

-- Tabla de usuarios soporta los 3 roles
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,   -- SHA-256 64 caracteres
    rol ENUM('CLIENTE', 'TECNICO', 'ADMINISTRADOR') NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de tickets
-- tecnico_id es NULL hasta que el administrador asigna el ticket
CREATE TABLE tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    estado ENUM('Abierto', 'Asignado', 'En proceso', 'Resuelto', 'Cerrado') NOT NULL DEFAULT 'Abierto',
    cliente_id INT NOT NULL,
    tecnico_id INT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tickets_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_tickets_tecnico FOREIGN KEY (tecnico_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Historial de cambios de estado de los tickets
CREATE TABLE historial_cambios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT NOT NULL,
    usuario_id INT NOT NULL,               -- Quién realizó el cambio
    estado_anterior VARCHAR(50) NOT NULL,
    estado_nuevo VARCHAR(50) NOT NULL,
    comentario TEXT,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DATOS DE PRUEBA
-- Todos los usuarios tienen la contraseña: 123456
-- Hash SHA-256 de '123456':
--   8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

INSERT INTO usuarios (id, nombre, correo, password_hash, rol) VALUES
(1, 'Admin Principal', 'admin@soporte.com',          '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'ADMINISTRADOR'),
(2, 'Técnico Juan',    'juan.tecnico@soporte.com',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'TECNICO'),
(3, 'Cliente Maria',   'maria.cliente@gmail.com',    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CLIENTE'),
(4, 'Técnico Carlos',  'carlos.tecnico@soporte.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'TECNICO'),
(5, 'Cliente Lucia',   'lucia.cliente@gmail.com',    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'CLIENTE');

-- Tickets en distintos estados
--   1 Cerrado | 2 Resuelto (el cliente puede aprobarlo) | 3 En proceso
--   4 Asignado | 5 y 6 Abiertos sin técnico (el admin puede asignarlos)
INSERT INTO tickets (id, titulo, descripcion, estado, cliente_id, tecnico_id, fecha_creacion) VALUES
(1, 'Problema con correo',           'No puedo enviar correos desde mi laptop.',                      'Cerrado',    3, 2,    NOW() - INTERVAL 72 HOUR),
(2, 'Impresora no responde',         'La impresora del segundo piso no imprime desde ayer.',          'Resuelto',   3, 2,    NOW() - INTERVAL 48 HOUR),
(3, 'No funciona el WiFi',           'La red WiFi de la oficina se cae cada pocos minutos.',          'En proceso', 5, 4,    NOW() - INTERVAL 24 HOUR),
(4, 'Acceso a carpeta compartida',   'Necesito permisos de lectura en la carpeta de contabilidad.',   'Asignado',   5, 4,    NOW() - INTERVAL 5 HOUR),
(5, 'Pantalla parpadea',             'El monitor de mi escritorio parpadea al abrir programas.',      'Abierto',    3, NULL, NOW() - INTERVAL 2 HOUR),
(6, 'Cambio de contraseña',          'Olvidé mi contraseña del sistema de facturación.',              'Abierto',    5, NULL, NOW() - INTERVAL 1 HOUR);

-- Historial coherente con el estado actual de cada ticket
INSERT INTO historial_cambios (ticket_id, usuario_id, estado_anterior, estado_nuevo, comentario, fecha_cambio) VALUES
(1, 1, 'Abierto',    'Asignado',   'Asignado al técnico Técnico Juan',   NOW() - INTERVAL 70 HOUR),
(1, 2, 'Asignado',   'En proceso', 'Estado actualizado por el técnico',  NOW() - INTERVAL 60 HOUR),
(1, 2, 'En proceso', 'Resuelto',   'Estado actualizado por el técnico',  NOW() - INTERVAL 50 HOUR),
(1, 3, 'Resuelto',   'Cerrado',    'Solución aprobada por el cliente',   NOW() - INTERVAL 48 HOUR),
(2, 1, 'Abierto',    'Asignado',   'Asignado al técnico Técnico Juan',   NOW() - INTERVAL 46 HOUR),
(2, 2, 'Asignado',   'En proceso', 'Estado actualizado por el técnico',  NOW() - INTERVAL 40 HOUR),
(2, 2, 'En proceso', 'Resuelto',   'Estado actualizado por el técnico',  NOW() - INTERVAL 4 HOUR),
(3, 1, 'Abierto',    'Asignado',   'Asignado al técnico Técnico Carlos', NOW() - INTERVAL 22 HOUR),
(3, 4, 'Asignado',   'En proceso', 'Estado actualizado por el técnico',  NOW() - INTERVAL 20 HOUR),
(4, 1, 'Abierto',    'Asignado',   'Asignado al técnico Técnico Carlos', NOW() - INTERVAL 4 HOUR);
