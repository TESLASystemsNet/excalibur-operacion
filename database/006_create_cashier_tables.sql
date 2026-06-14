CREATE TABLE IF NOT EXISTS app.cajas_jornada (
    id_caja_jornada BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_jornada BIGINT NOT NULL REFERENCES app.jornadas_operativas(id_jornada),
    id_caja BIGINT NOT NULL REFERENCES app.cajas_operativas(id_caja),
    id_estacion BIGINT NOT NULL REFERENCES app.estaciones_operativas(id_estacion),
    id_turno BIGINT NOT NULL REFERENCES app.turnos_caja(id_turno),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    saldo_inicial NUMERIC(14,2) NOT NULL DEFAULT 0,
    saldo_actual NUMERIC(14,2) NOT NULL DEFAULT 0,
    tarjetas_iniciales INTEGER NOT NULL DEFAULT 0,
    tarjetas_actuales INTEGER NOT NULL DEFAULT 0,
    fecha_apertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    apertura_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_precierre TIMESTAMPTZ,
    precierre_por BIGINT REFERENCES app.usuarios(id_usuario),
    fecha_cierre TIMESTAMPTZ,
    cierre_por BIGINT REFERENCES app.usuarios(id_usuario),
    monto_declarado_cierre NUMERIC(14,2),
    tarjetas_devueltas_cierre INTEGER,
    observaciones TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    CONSTRAINT ck_cajas_jornada_estado CHECK (estado IN ('ABIERTA', 'PRECIERRE', 'CERRADA')),
    CONSTRAINT ck_cajas_jornada_saldo_inicial CHECK (saldo_inicial >= 0),
    CONSTRAINT ck_cajas_jornada_saldo_actual CHECK (saldo_actual >= 0),
    CONSTRAINT ck_cajas_jornada_tarjetas_iniciales CHECK (tarjetas_iniciales >= 0),
    CONSTRAINT ck_cajas_jornada_tarjetas_actuales CHECK (tarjetas_actuales >= 0),
    CONSTRAINT ck_cajas_jornada_monto_cierre CHECK (monto_declarado_cierre IS NULL OR monto_declarado_cierre >= 0),
    CONSTRAINT ck_cajas_jornada_tarjetas_cierre CHECK (tarjetas_devueltas_cierre IS NULL OR tarjetas_devueltas_cierre >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_caja_jornada_estacion_turno
    ON app.cajas_jornada (id_jornada, id_caja, id_turno);

CREATE UNIQUE INDEX IF NOT EXISTS ux_caja_abierta_por_estacion
    ON app.cajas_jornada (id_jornada, id_caja, id_turno)
    WHERE estado IN ('ABIERTA', 'PRECIERRE');

CREATE INDEX IF NOT EXISTS ix_cajas_jornada_estado
    ON app.cajas_jornada (estado, fecha_apertura DESC);

CREATE TABLE IF NOT EXISTS app.movimientos_caja (
    id_movimiento_caja BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_caja_jornada BIGINT NOT NULL REFERENCES app.cajas_jornada(id_caja_jornada),
    id_estacion BIGINT REFERENCES app.estaciones_operativas(id_estacion),
    tipo VARCHAR(30) NOT NULL,
    numero_tarjeta VARCHAR(60),
    monto NUMERIC(14,2) NOT NULL DEFAULT 0,
    maquina VARCHAR(60),
    motivo VARCHAR(180),
    referencia VARCHAR(120),
    observaciones TEXT,
    registrado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_movimiento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_movimientos_caja_tipo CHECK (
        tipo IN (
            'REPOSICION',
            'DEVOLUCION',
            'VENTA',
            'PAGO',
            'PAGO_MANUAL',
            'TRANSACCION_ESPECIAL',
            'CORTESIA',
            'PROMOCIONAL'
        )
    ),
    CONSTRAINT ck_movimientos_caja_monto CHECK (monto >= 0)
);

CREATE INDEX IF NOT EXISTS ix_movimientos_caja_jornada
    ON app.movimientos_caja (id_caja_jornada, fecha_movimiento DESC);

CREATE INDEX IF NOT EXISTS ix_movimientos_caja_estacion
    ON app.movimientos_caja (id_estacion, fecha_movimiento DESC);

CREATE INDEX IF NOT EXISTS ix_movimientos_caja_tarjeta
    ON app.movimientos_caja (numero_tarjeta, fecha_movimiento DESC);
