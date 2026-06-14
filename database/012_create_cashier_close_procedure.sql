CREATE TABLE IF NOT EXISTS app.cierres_caja (
    id_cierre_caja BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_caja_jornada BIGINT NOT NULL REFERENCES app.cajas_jornada(id_caja_jornada),
    saldo_sistema NUMERIC(14,2) NOT NULL DEFAULT 0,
    monto_declarado NUMERIC(14,2) NOT NULL DEFAULT 0,
    diferencia NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_ventas NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_pagos NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_pagos_manual NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_reposiciones NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_devoluciones NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_especiales NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_cortesias NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_promocionales NUMERIC(14,2) NOT NULL DEFAULT 0,
    movimientos_registrados INTEGER NOT NULL DEFAULT 0,
    tarjetas_asignadas INTEGER NOT NULL DEFAULT 0,
    tarjetas_devueltas INTEGER NOT NULL DEFAULT 0,
    observaciones TEXT,
    cerrado_por BIGINT NOT NULL REFERENCES app.usuarios(id_usuario),
    fecha_cierre TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_cierres_caja_jornada UNIQUE (id_caja_jornada),
    CONSTRAINT ck_cierres_caja_monto_declarado CHECK (monto_declarado >= 0),
    CONSTRAINT ck_cierres_caja_tarjetas_devueltas CHECK (tarjetas_devueltas >= 0)
);

CREATE INDEX IF NOT EXISTS ix_cierres_caja_fecha
    ON app.cierres_caja (fecha_cierre DESC);

CREATE OR REPLACE FUNCTION app.cerrar_caja_jornada(
    p_id_caja_jornada BIGINT,
    p_monto_declarado NUMERIC,
    p_tarjetas_devueltas INTEGER,
    p_observaciones TEXT,
    p_cerrado_por BIGINT
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR(20);
    v_saldo_sistema NUMERIC(14,2);
    v_capturas_activas INTEGER;
    v_tarjetas_asignadas INTEGER;
    v_cierre_id BIGINT;
BEGIN
    IF p_id_caja_jornada IS NULL THEN
        RAISE EXCEPTION 'La caja es obligatoria';
    END IF;

    IF p_monto_declarado IS NULL OR p_monto_declarado < 0 THEN
        RAISE EXCEPTION 'El monto declarado debe ser mayor o igual a cero';
    END IF;

    IF p_tarjetas_devueltas IS NULL OR p_tarjetas_devueltas < 0 THEN
        RAISE EXCEPTION 'Las tarjetas devueltas deben ser mayor o igual a cero';
    END IF;

    SELECT estado, saldo_actual
      INTO v_estado, v_saldo_sistema
      FROM app.cajas_jornada
     WHERE id_caja_jornada = p_id_caja_jornada
     FOR UPDATE;

    IF v_estado IS NULL THEN
        RAISE EXCEPTION 'La caja no existe';
    END IF;

    IF v_estado NOT IN ('ABIERTA', 'PRECIERRE') THEN
        RAISE EXCEPTION 'La caja no esta disponible para cierre';
    END IF;

    SELECT COUNT(*)
      INTO v_capturas_activas
      FROM app.capturas_rfid_egm
     WHERE id_caja_jornada = p_id_caja_jornada
       AND estado IN ('AUTORIZADA', 'CONFIRMADA');

    IF v_capturas_activas > 0 THEN
        RAISE EXCEPTION 'No se puede cerrar caja con tarjetas capturadas por EGM';
    END IF;

    SELECT COUNT(*)
      INTO v_tarjetas_asignadas
      FROM app.cliente_tarjetas
     WHERE id_caja_jornada = p_id_caja_jornada
       AND estado = 'ACTIVA';

    INSERT INTO app.cierres_caja (
        id_caja_jornada,
        saldo_sistema,
        monto_declarado,
        diferencia,
        total_ventas,
        total_pagos,
        total_pagos_manual,
        total_reposiciones,
        total_devoluciones,
        total_especiales,
        total_cortesias,
        total_promocionales,
        movimientos_registrados,
        tarjetas_asignadas,
        tarjetas_devueltas,
        observaciones,
        cerrado_por
    )
    SELECT
        p_id_caja_jornada,
        v_saldo_sistema,
        p_monto_declarado,
        p_monto_declarado - v_saldo_sistema,
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'VENTA'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'PAGO'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'PAGO_MANUAL'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'REPOSICION'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'DEVOLUCION'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'TRANSACCION_ESPECIAL'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'CORTESIA'), 0),
        COALESCE(SUM(monto) FILTER (WHERE tipo = 'PROMOCIONAL'), 0),
        COUNT(m.id_movimiento_caja)::INTEGER,
        v_tarjetas_asignadas,
        p_tarjetas_devueltas,
        NULLIF(btrim(p_observaciones), ''),
        p_cerrado_por
      FROM app.movimientos_caja m
     WHERE m.id_caja_jornada = p_id_caja_jornada
    ON CONFLICT (id_caja_jornada) DO UPDATE SET
        saldo_sistema = EXCLUDED.saldo_sistema,
        monto_declarado = EXCLUDED.monto_declarado,
        diferencia = EXCLUDED.diferencia,
        total_ventas = EXCLUDED.total_ventas,
        total_pagos = EXCLUDED.total_pagos,
        total_pagos_manual = EXCLUDED.total_pagos_manual,
        total_reposiciones = EXCLUDED.total_reposiciones,
        total_devoluciones = EXCLUDED.total_devoluciones,
        total_especiales = EXCLUDED.total_especiales,
        total_cortesias = EXCLUDED.total_cortesias,
        total_promocionales = EXCLUDED.total_promocionales,
        movimientos_registrados = EXCLUDED.movimientos_registrados,
        tarjetas_asignadas = EXCLUDED.tarjetas_asignadas,
        tarjetas_devueltas = EXCLUDED.tarjetas_devueltas,
        observaciones = EXCLUDED.observaciones,
        cerrado_por = EXCLUDED.cerrado_por,
        fecha_cierre = CURRENT_TIMESTAMP
    RETURNING id_cierre_caja INTO v_cierre_id;

    UPDATE app.cajas_jornada
       SET estado = 'CERRADA',
           fecha_cierre = CURRENT_TIMESTAMP,
           cierre_por = p_cerrado_por,
           monto_declarado_cierre = p_monto_declarado,
           tarjetas_devueltas_cierre = p_tarjetas_devueltas,
           observaciones = COALESCE(NULLIF(btrim(p_observaciones), ''), observaciones),
           fecha_actualizacion = CURRENT_TIMESTAMP
     WHERE id_caja_jornada = p_id_caja_jornada;

    RETURN v_cierre_id;
END;
$$;
