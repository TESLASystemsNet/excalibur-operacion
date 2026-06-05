CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.horarios_operacion (
    id_horario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_horarios_operacion_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_horarios_operacion_nombre
    ON app.horarios_operacion (upper(nombre));

CREATE TABLE IF NOT EXISTS app.turnos_caja (
    id_turno BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_turnos_caja_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_turnos_caja_nombre
    ON app.turnos_caja (upper(nombre));

CREATE TABLE IF NOT EXISTS app.estaciones_operativas (
    id_estacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    sala VARCHAR(100),
    ubicacion VARCHAR(150),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_estaciones_nombre_no_vacio CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_estaciones_tipo CHECK (tipo IN ('CAJA', 'TESORERIA'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_estaciones_operativas_nombre
    ON app.estaciones_operativas (upper(nombre));

CREATE INDEX IF NOT EXISTS ix_estaciones_operativas_tipo_activa
    ON app.estaciones_operativas (tipo, activa);

CREATE TABLE IF NOT EXISTS app.asignaciones_operativas (
    id_asignacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    id_estacion BIGINT NOT NULL REFERENCES app.estaciones_operativas(id_estacion),
    id_turno BIGINT NOT NULL REFERENCES app.turnos_caja(id_turno),
    fecha_operacion DATE NOT NULL,
    rol_operativo VARCHAR(20) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_asignaciones_rol CHECK (rol_operativo IN ('SUPERVISOR', 'TESORERO', 'CAJERO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_asignacion_operativa_activa
    ON app.asignaciones_operativas (id_usuario, id_estacion, id_turno, fecha_operacion, rol_operativo)
    WHERE activa = TRUE;

CREATE INDEX IF NOT EXISTS ix_asignaciones_fecha_estacion
    ON app.asignaciones_operativas (fecha_operacion, id_estacion, activa);

CREATE TABLE IF NOT EXISTS app.tarjetas_operativas (
    id_tarjeta BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_tarjeta VARCHAR(60) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_vencimiento DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_tarjetas_numero_no_vacio CHECK (btrim(numero_tarjeta) <> ''),
    CONSTRAINT ck_tarjetas_tipo CHECK (tipo IN ('CLIENTE', 'GENERICA')),
    CONSTRAINT ck_tarjetas_estado CHECK (estado IN ('DISPONIBLE', 'ASIGNADA', 'BLOQUEADA', 'VENCIDA', 'INACTIVA'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_tarjetas_operativas_numero
    ON app.tarjetas_operativas (numero_tarjeta);

CREATE INDEX IF NOT EXISTS ix_tarjetas_tipo_estado
    ON app.tarjetas_operativas (tipo, estado);

CREATE TABLE IF NOT EXISTS app.bitacora_operativa (
    id_bitacora BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    accion VARCHAR(60) NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    entidad_id BIGINT,
    detalle TEXT,
    id_usuario BIGINT REFERENCES app.usuarios(id_usuario),
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(80),
    CONSTRAINT ck_bitacora_accion_no_vacio CHECK (btrim(accion) <> ''),
    CONSTRAINT ck_bitacora_entidad_no_vacio CHECK (btrim(entidad) <> '')
);

CREATE INDEX IF NOT EXISTS ix_bitacora_operativa_entidad
    ON app.bitacora_operativa (entidad, entidad_id, fecha_evento DESC);

CREATE INDEX IF NOT EXISTS ix_bitacora_operativa_usuario
    ON app.bitacora_operativa (id_usuario, fecha_evento DESC);
