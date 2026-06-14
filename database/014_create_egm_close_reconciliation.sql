CREATE TABLE IF NOT EXISTS app.egms_operativas (
    id_egm BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    egm_addr VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    sala VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_egms_addr_no_vacio CHECK (btrim(egm_addr) <> ''),
    CONSTRAINT ck_egms_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_egms_operativas_addr
    ON app.egms_operativas (upper(egm_addr));

CREATE INDEX IF NOT EXISTS ix_egms_operativas_activo
    ON app.egms_operativas (activo, egm_addr);

INSERT INTO app.egms_operativas (egm_addr, nombre, sala)
SELECT '01', 'EGM 01', 'Play Valley'
WHERE NOT EXISTS (SELECT 1 FROM app.egms_operativas);

CREATE TABLE IF NOT EXISTS app.cierres_operacion (
    id_cierre_operacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_jornada BIGINT NOT NULL REFERENCES app.jornadas_operativas(id_jornada),
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_PROCESO',
    porcentaje INTEGER NOT NULL DEFAULT 0,
    total_egms INTEGER NOT NULL DEFAULT 0,
    egms_ok INTEGER NOT NULL DEFAULT 0,
    egms_diferencia INTEGER NOT NULL DEFAULT 0,
    egms_incompletas INTEGER NOT NULL DEFAULT 0,
    total_caja_reportado NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_egm_calculado NUMERIC(14,2) NOT NULL DEFAULT 0,
    diferencia_total NUMERIC(14,2) NOT NULL DEFAULT 0,
    observaciones TEXT,
    iniciado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMPTZ,
    CONSTRAINT ck_cierres_operacion_estado CHECK (estado IN ('EN_PROCESO', 'COMPLETO', 'CON_DIFERENCIAS', 'INCOMPLETO', 'FALLIDO')),
    CONSTRAINT ck_cierres_operacion_porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT ux_cierres_operacion_jornada UNIQUE (id_jornada)
);

CREATE INDEX IF NOT EXISTS ix_cierres_operacion_estado
    ON app.cierres_operacion (estado, fecha_inicio DESC);

CREATE TABLE IF NOT EXISTS app.egm_meter_snapshots (
    id_snapshot BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_jornada BIGINT NOT NULL REFERENCES app.jornadas_operativas(id_jornada),
    id_egm BIGINT NOT NULL REFERENCES app.egms_operativas(id_egm),
    tipo_snapshot VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'OK',
    proveedor VARCHAR(30) NOT NULL DEFAULT 'INTERNO_SIMULADO',
    coin_in NUMERIC(18,2) NOT NULL DEFAULT 0,
    coin_out NUMERIC(18,2) NOT NULL DEFAULT 0,
    jackpot NUMERIC(18,2) NOT NULL DEFAULT 0,
    handpay_cancelled NUMERIC(18,2) NOT NULL DEFAULT 0,
    cancelled NUMERIC(18,2) NOT NULL DEFAULT 0,
    games_played NUMERIC(18,2) NOT NULL DEFAULT 0,
    games_won NUMERIC(18,2) NOT NULL DEFAULT 0,
    games_lost NUMERIC(18,2) NOT NULL DEFAULT 0,
    bills_accepted NUMERIC(18,2) NOT NULL DEFAULT 0,
    current_credits NUMERIC(18,2) NOT NULL DEFAULT 0,
    raw_response TEXT,
    mensaje TEXT,
    fecha_snapshot TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_snapshot_tipo CHECK (tipo_snapshot IN ('APERTURA', 'CIERRE', 'REINTENTO', 'MANUAL')),
    CONSTRAINT ck_snapshot_estado CHECK (estado IN ('OK', 'SIN_RESPUESTA', 'PARCIAL', 'ERROR_CRC', 'NO_SOPORTADO', 'SIMULADO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_egm_snapshot_jornada_tipo
    ON app.egm_meter_snapshots (id_jornada, id_egm, tipo_snapshot)
    WHERE tipo_snapshot IN ('APERTURA', 'CIERRE');

CREATE INDEX IF NOT EXISTS ix_egm_snapshots_jornada
    ON app.egm_meter_snapshots (id_jornada, tipo_snapshot, fecha_snapshot DESC);

CREATE TABLE IF NOT EXISTS app.egm_daily_reconciliations (
    id_reconciliacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_jornada BIGINT NOT NULL REFERENCES app.jornadas_operativas(id_jornada),
    id_egm BIGINT NOT NULL REFERENCES app.egms_operativas(id_egm),
    id_snapshot_apertura BIGINT REFERENCES app.egm_meter_snapshots(id_snapshot),
    id_snapshot_cierre BIGINT REFERENCES app.egm_meter_snapshots(id_snapshot),
    coin_in_delta NUMERIC(18,2) NOT NULL DEFAULT 0,
    coin_out_delta NUMERIC(18,2) NOT NULL DEFAULT 0,
    jackpot_delta NUMERIC(18,2) NOT NULL DEFAULT 0,
    cancelled_delta NUMERIC(18,2) NOT NULL DEFAULT 0,
    bills_accepted_delta NUMERIC(18,2) NOT NULL DEFAULT 0,
    current_credits_cierre NUMERIC(18,2) NOT NULL DEFAULT 0,
    host_loads NUMERIC(18,2) NOT NULL DEFAULT 0,
    host_cashouts NUMERIC(18,2) NOT NULL DEFAULT 0,
    ganancia_calculada NUMERIC(18,2) NOT NULL DEFAULT 0,
    perdida_calculada NUMERIC(18,2) NOT NULL DEFAULT 0,
    caja_reportado NUMERIC(18,2) NOT NULL DEFAULT 0,
    diferencia_vs_caja NUMERIC(18,2) NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL DEFAULT 'CUADRADO',
    detalle TEXT,
    fecha_calculo TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_egm_reconciliacion_jornada UNIQUE (id_jornada, id_egm),
    CONSTRAINT ck_egm_reconciliacion_estado CHECK (estado IN ('CUADRADO', 'DIFERENCIA', 'INCOMPLETO', 'REQUIERE_REVISION'))
);

CREATE INDEX IF NOT EXISTS ix_egm_reconciliaciones_jornada
    ON app.egm_daily_reconciliations (id_jornada, estado);
