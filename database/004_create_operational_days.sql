CREATE TABLE IF NOT EXISTS app.jornadas_operativas (
    id_jornada BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha_jornada DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    fecha_apertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMPTZ,
    apertura_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    cierre_por BIGINT REFERENCES app.usuarios(id_usuario),
    observaciones TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    CONSTRAINT ck_jornadas_estado CHECK (estado IN ('ABIERTA', 'CERRANDO', 'CERRADA'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_jornadas_operativas_fecha
    ON app.jornadas_operativas (fecha_jornada);

CREATE UNIQUE INDEX IF NOT EXISTS ux_jornadas_operativas_abierta
    ON app.jornadas_operativas (estado)
    WHERE estado = 'ABIERTA';

CREATE INDEX IF NOT EXISTS ix_jornadas_operativas_fecha_estado
    ON app.jornadas_operativas (fecha_jornada DESC, estado);
