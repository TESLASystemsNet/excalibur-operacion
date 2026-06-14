CREATE OR REPLACE FUNCTION app.registrar_venta_caja(
    p_id_caja_jornada BIGINT,
    p_numero_tarjeta VARCHAR,
    p_monto NUMERIC,
    p_referencia VARCHAR,
    p_observaciones TEXT,
    p_registrado_por BIGINT
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_movimiento_id BIGINT;
    v_caja_estado VARCHAR(20);
    v_tarjeta_estado VARCHAR(20);
    v_tarjeta_tipo VARCHAR(20);
    v_capturada_egm BOOLEAN;
    v_fecha_vencimiento DATE;
BEGIN
    IF p_id_caja_jornada IS NULL THEN
        RAISE EXCEPTION 'La caja es obligatoria';
    END IF;

    IF p_numero_tarjeta IS NULL OR btrim(p_numero_tarjeta) = '' THEN
        RAISE EXCEPTION 'El numero de tarjeta es obligatorio';
    END IF;

    IF p_monto IS NULL OR p_monto <= 0 THEN
        RAISE EXCEPTION 'El monto de venta debe ser mayor a cero';
    END IF;

    SELECT estado
      INTO v_caja_estado
      FROM app.cajas_jornada
     WHERE id_caja_jornada = p_id_caja_jornada
     FOR UPDATE;

    IF v_caja_estado IS NULL THEN
        RAISE EXCEPTION 'La caja no existe';
    END IF;

    IF v_caja_estado <> 'ABIERTA' THEN
        RAISE EXCEPTION 'La caja debe estar abierta para registrar venta';
    END IF;

    SELECT t.estado, t.tipo, t.capturada_egm, t.fecha_vencimiento
      INTO v_tarjeta_estado, v_tarjeta_tipo, v_capturada_egm, v_fecha_vencimiento
      FROM app.tarjetas_operativas t
     WHERE t.numero_tarjeta = btrim(p_numero_tarjeta)
     LIMIT 1;

    IF v_tarjeta_estado IS NULL THEN
        RAISE EXCEPTION 'La tarjeta no existe en inventario operativo';
    END IF;

    IF v_tarjeta_tipo <> 'CLIENTE' THEN
        RAISE EXCEPTION 'Solo se pueden registrar ventas a tarjetas tipo CLIENTE';
    END IF;

    IF COALESCE(v_capturada_egm, FALSE) THEN
        RAISE EXCEPTION 'La tarjeta esta capturada por EGM; debe cobrarse antes de usarla en el sistema';
    END IF;

    IF v_fecha_vencimiento IS NOT NULL AND v_fecha_vencimiento < CURRENT_DATE THEN
        RAISE EXCEPTION 'La tarjeta esta vencida';
    END IF;

    IF v_tarjeta_estado NOT IN ('DISPONIBLE', 'ASIGNADA') THEN
        RAISE EXCEPTION 'La tarjeta no esta activa para venta';
    END IF;

    INSERT INTO app.movimientos_caja (
        id_caja_jornada,
        tipo,
        numero_tarjeta,
        monto,
        motivo,
        referencia,
        observaciones,
        registrado_por
    )
    VALUES (
        p_id_caja_jornada,
        'VENTA',
        btrim(p_numero_tarjeta),
        p_monto,
        'Venta de creditos',
        NULLIF(btrim(p_referencia), ''),
        NULLIF(btrim(p_observaciones), ''),
        p_registrado_por
    )
    RETURNING id_movimiento_caja INTO v_movimiento_id;

    UPDATE app.cajas_jornada
       SET saldo_actual = saldo_actual + p_monto,
           fecha_actualizacion = CURRENT_TIMESTAMP
     WHERE id_caja_jornada = p_id_caja_jornada;

    RETURN v_movimiento_id;
END;
$$;
