ALTER TABLE app.tarjetas_operativas
    ADD COLUMN IF NOT EXISTS capturada_egm BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS captura_egm_txid VARCHAR(80),
    ADD COLUMN IF NOT EXISTS captura_egm_fecha TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS ix_tarjetas_operativas_capturada_egm
    ON app.tarjetas_operativas (capturada_egm)
    WHERE capturada_egm = TRUE;

UPDATE app.tarjetas_operativas t
SET capturada_egm = TRUE,
    captura_egm_txid = c.txid,
    captura_egm_fecha = c.fecha_creacion,
    fecha_actualizacion = CURRENT_TIMESTAMP
FROM app.capturas_rfid_egm c
WHERE c.id_tarjeta = t.id_tarjeta
  AND c.estado IN ('AUTORIZADA', 'CONFIRMADA');

UPDATE app.tarjetas_operativas t
SET capturada_egm = FALSE,
    captura_egm_txid = NULL,
    captura_egm_fecha = NULL,
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE capturada_egm = TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM app.capturas_rfid_egm c
      WHERE c.id_tarjeta = t.id_tarjeta
        AND c.estado IN ('AUTORIZADA', 'CONFIRMADA')
  );
