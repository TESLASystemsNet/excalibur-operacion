CREATE TABLE IF NOT EXISTS app.capturas_rfid_egm (
    id_captura BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    txid VARCHAR(80) NOT NULL,
    uid VARCHAR(60) NOT NULL,
    id_tarjeta BIGINT NOT NULL REFERENCES app.tarjetas_operativas(id_tarjeta),
    id_cliente_tarjeta BIGINT NOT NULL REFERENCES app.cliente_tarjetas(id_cliente_tarjeta),
    id_caja_jornada BIGINT NOT NULL REFERENCES app.cajas_jornada(id_caja_jornada),
    egm_addr VARCHAR(20),
    saldo_autorizado NUMERIC(14,2) NOT NULL,
    creditos INTEGER NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'AUTORIZADA',
    resultado_egm VARCHAR(60),
    mensaje TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion TIMESTAMPTZ,
    fecha_cierre TIMESTAMPTZ,
    CONSTRAINT ck_capturas_rfid_egm_estado CHECK (estado IN ('AUTORIZADA', 'CONFIRMADA', 'CERRADA', 'CANCELADA')),
    CONSTRAINT ck_capturas_rfid_egm_saldo CHECK (saldo_autorizado > 0),
    CONSTRAINT ck_capturas_rfid_egm_creditos CHECK (creditos > 0),
    CONSTRAINT ck_capturas_rfid_egm_uid_no_vacio CHECK (btrim(uid) <> ''),
    CONSTRAINT ck_capturas_rfid_egm_txid_no_vacio CHECK (btrim(txid) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_capturas_rfid_egm_txid
    ON app.capturas_rfid_egm (txid);

CREATE UNIQUE INDEX IF NOT EXISTS ux_capturas_rfid_egm_uid_activa
    ON app.capturas_rfid_egm (uid)
    WHERE estado IN ('AUTORIZADA', 'CONFIRMADA');

CREATE INDEX IF NOT EXISTS ix_capturas_rfid_egm_uid_fecha
    ON app.capturas_rfid_egm (uid, fecha_creacion DESC);
