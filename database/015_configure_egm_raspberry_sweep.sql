ALTER TABLE app.egms_operativas
    ADD COLUMN IF NOT EXISTS raspberry_base_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS denominacion NUMERIC(10,4) NOT NULL DEFAULT 0.10,
    ADD COLUMN IF NOT EXISTS timeout_segundos INTEGER NOT NULL DEFAULT 120;

UPDATE app.egms_operativas
SET raspberry_base_url = COALESCE(raspberry_base_url, 'http://127.0.0.1:8080'),
    denominacion = COALESCE(denominacion, 0.10),
    timeout_segundos = COALESCE(timeout_segundos, 120)
WHERE egm_addr = '01';

CREATE INDEX IF NOT EXISTS ix_egms_operativas_raspberry
    ON app.egms_operativas (raspberry_base_url)
    WHERE activo = TRUE;
