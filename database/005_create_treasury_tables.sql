CREATE TABLE IF NOT EXISTS app.tesorerias_jornada (
    id_tesoreria_jornada BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_jornada BIGINT NOT NULL REFERENCES app.jornadas_operativas(id_jornada),
    id_estacion BIGINT NOT NULL REFERENCES app.estaciones_operativas(id_estacion),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    saldo_inicial NUMERIC(14,2) NOT NULL DEFAULT 0,
    saldo_actual NUMERIC(14,2) NOT NULL DEFAULT 0,
    fecha_apertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    apertura_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_precierre TIMESTAMPTZ,
    precierre_por BIGINT REFERENCES app.usuarios(id_usuario),
    fecha_cierre TIMESTAMPTZ,
    cierre_por BIGINT REFERENCES app.usuarios(id_usuario),
    observaciones TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    CONSTRAINT ck_tesorerias_jornada_estado CHECK (estado IN ('ABIERTA', 'PRECIERRE', 'CERRADA')),
    CONSTRAINT ck_tesorerias_jornada_saldo_inicial CHECK (saldo_inicial >= 0),
    CONSTRAINT ck_tesorerias_jornada_saldo_actual CHECK (saldo_actual >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_tesoreria_jornada_estacion
    ON app.tesorerias_jornada (id_jornada, id_estacion);

CREATE UNIQUE INDEX IF NOT EXISTS ux_tesoreria_abierta_por_jornada
    ON app.tesorerias_jornada (id_jornada)
    WHERE estado IN ('ABIERTA', 'PRECIERRE');

CREATE INDEX IF NOT EXISTS ix_tesorerias_jornada_estado
    ON app.tesorerias_jornada (estado, fecha_apertura DESC);

CREATE TABLE IF NOT EXISTS app.movimientos_tesoreria (
    id_movimiento_tesoreria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tesoreria_jornada BIGINT NOT NULL REFERENCES app.tesorerias_jornada(id_tesoreria_jornada),
    id_estacion_caja BIGINT REFERENCES app.estaciones_operativas(id_estacion),
    id_turno BIGINT REFERENCES app.turnos_caja(id_turno),
    tipo VARCHAR(20) NOT NULL,
    concepto VARCHAR(120) NOT NULL,
    monto NUMERIC(14,2) NOT NULL,
    referencia VARCHAR(120),
    observaciones TEXT,
    registrado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_movimiento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_movimientos_tesoreria_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA', 'FONDO_CAJA', 'DEVOLUCION_CAJA')),
    CONSTRAINT ck_movimientos_tesoreria_monto CHECK (monto > 0),
    CONSTRAINT ck_movimientos_tesoreria_concepto_no_vacio CHECK (btrim(concepto) <> '')
);

CREATE INDEX IF NOT EXISTS ix_movimientos_tesoreria_jornada
    ON app.movimientos_tesoreria (id_tesoreria_jornada, fecha_movimiento DESC);

CREATE TABLE IF NOT EXISTS app.movimientos_tarjetas_tesoreria (
    id_movimiento_tarjetas BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tesoreria_jornada BIGINT NOT NULL REFERENCES app.tesorerias_jornada(id_tesoreria_jornada),
    id_estacion_caja BIGINT NOT NULL REFERENCES app.estaciones_operativas(id_estacion),
    id_turno BIGINT REFERENCES app.turnos_caja(id_turno),
    tipo VARCHAR(30) NOT NULL,
    numero_inicial VARCHAR(60) NOT NULL,
    numero_final VARCHAR(60) NOT NULL,
    cantidad INTEGER NOT NULL,
    referencia VARCHAR(120),
    observaciones TEXT,
    registrado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_movimiento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_movimientos_tarjetas_tipo CHECK (tipo IN ('ENTREGA_RANGO', 'DEVOLUCION_TARJETAS')),
    CONSTRAINT ck_movimientos_tarjetas_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_movimientos_tarjetas_inicial_no_vacio CHECK (btrim(numero_inicial) <> ''),
    CONSTRAINT ck_movimientos_tarjetas_final_no_vacio CHECK (btrim(numero_final) <> '')
);

CREATE INDEX IF NOT EXISTS ix_movimientos_tarjetas_tesoreria_jornada
    ON app.movimientos_tarjetas_tesoreria (id_tesoreria_jornada, fecha_movimiento DESC);
