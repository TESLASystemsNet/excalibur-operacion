CREATE TABLE IF NOT EXISTS app.cajas_operativas (
    id_caja BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT REFERENCES app.usuarios(id_usuario),
    actualizado_por BIGINT REFERENCES app.usuarios(id_usuario),
    CONSTRAINT ck_cajas_operativas_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_cajas_operativas_nombre
    ON app.cajas_operativas (upper(nombre));

CREATE UNIQUE INDEX IF NOT EXISTS ux_cajas_operativas_principal
    ON app.cajas_operativas (principal)
    WHERE principal = TRUE;

INSERT INTO app.cajas_operativas (nombre, descripcion, principal, activa)
VALUES ('Caja Principal', 'Caja contable global para operacion general', TRUE, TRUE)
ON CONFLICT DO NOTHING;

ALTER TABLE app.estaciones_operativas
    ADD COLUMN IF NOT EXISTS id_caja BIGINT REFERENCES app.cajas_operativas(id_caja);

UPDATE app.estaciones_operativas
SET id_caja = (SELECT id_caja FROM app.cajas_operativas WHERE principal = TRUE LIMIT 1)
WHERE tipo = 'CAJA'
  AND id_caja IS NULL;

CREATE INDEX IF NOT EXISTS ix_estaciones_operativas_caja
    ON app.estaciones_operativas (id_caja, activa);

ALTER TABLE app.cajas_jornada
    ADD COLUMN IF NOT EXISTS id_caja BIGINT REFERENCES app.cajas_operativas(id_caja);

UPDATE app.cajas_jornada cj
SET id_caja = COALESCE(
    e.id_caja,
    (SELECT id_caja FROM app.cajas_operativas WHERE principal = TRUE LIMIT 1)
)
FROM app.estaciones_operativas e
WHERE e.id_estacion = cj.id_estacion
  AND cj.id_caja IS NULL;

ALTER TABLE app.cajas_jornada
    ALTER COLUMN id_caja SET NOT NULL;

DROP INDEX IF EXISTS app.ux_caja_jornada_estacion_turno;
DROP INDEX IF EXISTS app.ux_caja_abierta_por_estacion;

CREATE UNIQUE INDEX IF NOT EXISTS ux_caja_jornada_caja_turno
    ON app.cajas_jornada (id_jornada, id_caja, id_turno);

CREATE UNIQUE INDEX IF NOT EXISTS ux_caja_abierta_por_caja
    ON app.cajas_jornada (id_jornada, id_caja, id_turno)
    WHERE estado IN ('ABIERTA', 'PRECIERRE');

ALTER TABLE app.movimientos_caja
    ADD COLUMN IF NOT EXISTS id_estacion BIGINT REFERENCES app.estaciones_operativas(id_estacion);

UPDATE app.movimientos_caja m
SET id_estacion = cj.id_estacion
FROM app.cajas_jornada cj
WHERE cj.id_caja_jornada = m.id_caja_jornada
  AND m.id_estacion IS NULL;

CREATE INDEX IF NOT EXISTS ix_movimientos_caja_estacion
    ON app.movimientos_caja (id_estacion, fecha_movimiento DESC);
