CREATE TABLE IF NOT EXISTS app.clientes_operativos (
    id_cliente BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    apellido_paterno VARCHAR(120),
    apellido_materno VARCHAR(120),
    telefono VARCHAR(40),
    email VARCHAR(160),
    fecha_nacimiento DATE,
    documento_identidad VARCHAR(80),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    observaciones TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_clientes_nombre_no_vacio CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_clientes_estado CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'INACTIVO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_clientes_documento
    ON app.clientes_operativos (upper(documento_identidad))
    WHERE documento_identidad IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_clientes_nombre
    ON app.clientes_operativos (upper(nombre), upper(apellido_paterno), upper(apellido_materno));

CREATE TABLE IF NOT EXISTS app.cliente_tarjetas (
    id_cliente_tarjeta BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente BIGINT NOT NULL REFERENCES app.clientes_operativos(id_cliente),
    id_tarjeta BIGINT NOT NULL REFERENCES app.tarjetas_operativas(id_tarjeta),
    id_caja_jornada BIGINT REFERENCES app.cajas_jornada(id_caja_jornada),
    tipo_asignacion VARCHAR(30) NOT NULL DEFAULT 'ALTA_CLIENTE',
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    nip_hash VARCHAR(120),
    observaciones TEXT,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_cancelacion TIMESTAMPTZ,
    cancelado_por BIGINT REFERENCES app.usuarios(id_usuario),
    motivo_cancelacion TEXT,
    CONSTRAINT ck_cliente_tarjetas_tipo CHECK (tipo_asignacion IN ('ALTA_CLIENTE', 'REPOSICION')),
    CONSTRAINT ck_cliente_tarjetas_estado CHECK (estado IN ('ACTIVA', 'CANCELADA', 'BLOQUEADA'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_cliente_tarjeta_activa
    ON app.cliente_tarjetas (id_tarjeta)
    WHERE estado = 'ACTIVA';

CREATE INDEX IF NOT EXISTS ix_cliente_tarjetas_cliente
    ON app.cliente_tarjetas (id_cliente, fecha_asignacion DESC);

ALTER TABLE app.movimientos_caja DROP CONSTRAINT IF EXISTS ck_movimientos_caja_tipo;

ALTER TABLE app.movimientos_caja
    ADD CONSTRAINT ck_movimientos_caja_tipo CHECK (
        tipo IN (
            'REPOSICION',
            'DEVOLUCION',
            'VENTA',
            'PAGO',
            'PAGO_MANUAL',
            'TRANSACCION_ESPECIAL',
            'CORTESIA',
            'PROMOCIONAL',
            'ALTA_CLIENTE'
        )
    );
