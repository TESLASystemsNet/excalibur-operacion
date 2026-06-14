CREATE INDEX IF NOT EXISTS ix_movimientos_tesoreria_fecha
    ON app.movimientos_tesoreria (fecha_movimiento DESC);

CREATE INDEX IF NOT EXISTS ix_movimientos_tarjetas_tesoreria_fecha
    ON app.movimientos_tarjetas_tesoreria (fecha_movimiento DESC);

CREATE OR REPLACE VIEW app.v_movimientos_tesoreria_detalle AS
SELECT
    m.id_movimiento_tesoreria,
    m.id_tesoreria_jornada,
    tj.id_jornada,
    j.fecha_jornada,
    tj.id_estacion AS id_estacion_tesoreria,
    et.nombre AS estacion_tesoreria_nombre,
    m.id_estacion_caja,
    ec.nombre AS estacion_caja_nombre,
    m.id_turno,
    tc.nombre AS turno_nombre,
    m.tipo,
    m.concepto,
    m.monto,
    m.referencia,
    m.observaciones,
    m.registrado_por,
    u.username AS registrado_username,
    m.fecha_movimiento
FROM app.movimientos_tesoreria m
JOIN app.tesorerias_jornada tj ON tj.id_tesoreria_jornada = m.id_tesoreria_jornada
JOIN app.jornadas_operativas j ON j.id_jornada = tj.id_jornada
JOIN app.estaciones_operativas et ON et.id_estacion = tj.id_estacion
LEFT JOIN app.estaciones_operativas ec ON ec.id_estacion = m.id_estacion_caja
LEFT JOIN app.turnos_caja tc ON tc.id_turno = m.id_turno
JOIN app.usuarios u ON u.id_usuario = m.registrado_por;

CREATE OR REPLACE VIEW app.v_bitacora_tesoreria AS
SELECT
    'MOV-' || m.id_movimiento_tesoreria AS id_evento,
    'EFECTIVO' AS categoria,
    m.tipo,
    m.concepto AS detalle,
    COALESCE(m.estacion_caja_nombre, m.estacion_tesoreria_nombre) AS estacion,
    m.turno_nombre AS turno,
    m.monto,
    NULL::INTEGER AS cantidad,
    m.referencia,
    m.observaciones,
    m.registrado_username AS username,
    m.fecha_movimiento AS fecha_evento
FROM app.v_movimientos_tesoreria_detalle m
UNION ALL
SELECT
    'TAR-' || mt.id_movimiento_tarjetas AS id_evento,
    'TARJETAS' AS categoria,
    mt.tipo,
    mt.numero_inicial || ' - ' || mt.numero_final AS detalle,
    e.nombre AS estacion,
    t.nombre AS turno,
    NULL::NUMERIC(14,2) AS monto,
    mt.cantidad,
    mt.referencia,
    mt.observaciones,
    u.username,
    mt.fecha_movimiento AS fecha_evento
FROM app.movimientos_tarjetas_tesoreria mt
JOIN app.estaciones_operativas e ON e.id_estacion = mt.id_estacion_caja
LEFT JOIN app.turnos_caja t ON t.id_turno = mt.id_turno
JOIN app.usuarios u ON u.id_usuario = mt.registrado_por
UNION ALL
SELECT
    'BIT-' || b.id_bitacora AS id_evento,
    'BITACORA' AS categoria,
    b.accion AS tipo,
    COALESCE(b.detalle, b.entidad) AS detalle,
    NULL AS estacion,
    NULL AS turno,
    NULL::NUMERIC(14,2) AS monto,
    NULL::INTEGER AS cantidad,
    b.entidad || COALESCE(' #' || b.entidad_id, '') AS referencia,
    b.ip_origen AS observaciones,
    u.username,
    b.fecha_evento
FROM app.bitacora_operativa b
LEFT JOIN app.usuarios u ON u.id_usuario = b.id_usuario
WHERE b.entidad IN ('TESORERIA_JORNADA', 'MOVIMIENTO_TESORERIA', 'MOVIMIENTO_TARJETAS_TESORERIA');
