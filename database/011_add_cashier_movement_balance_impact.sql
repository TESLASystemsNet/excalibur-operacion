ALTER TABLE app.movimientos_caja
    ADD COLUMN IF NOT EXISTS impacto_saldo NUMERIC(14,2);

