TRUNCATE TABLE
    app.capturas_rfid_egm,
    app.cliente_tarjetas,
    app.clientes_operativos,
    app.movimientos_caja,
    app.cierres_caja,
    app.movimientos_tarjetas_tesoreria,
    app.movimientos_tesoreria,
    app.cierres_operacion,
    app.egm_daily_reconciliations,
    app.egm_meter_snapshots,
    app.egms_operativas,
    app.cajas_jornada,
    app.tesorerias_jornada,
    app.jornadas_operativas,
    app.asignaciones_operativas,
    app.tarjetas_operativas,
    app.estaciones_operativas,
    app.turnos_caja,
    app.horarios_operacion,
    app.bitacora_operativa
RESTART IDENTITY CASCADE;

UPDATE app.usuarios
SET creado_por = NULL,
    actualizado_por = NULL,
    eliminado_por = NULL
WHERE creado_por IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE)
   OR actualizado_por IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE)
   OR eliminado_por IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE);

UPDATE app.usuario_roles
SET asignado_por = NULL
WHERE asignado_por IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE);

UPDATE app.rol_permisos
SET asignado_por = NULL
WHERE asignado_por IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE);

DELETE FROM app.usuario_roles
WHERE id_usuario IN (SELECT id_usuario FROM app.usuarios WHERE activo = FALSE OR eliminado = TRUE);

DELETE FROM app.usuarios
WHERE activo = FALSE
   OR eliminado = TRUE;

INSERT INTO app.cajas_operativas (nombre, descripcion, principal, activa)
VALUES ('Caja Principal', 'Caja contable global para operacion general', TRUE, TRUE)
ON CONFLICT DO NOTHING;

UPDATE app.estaciones_operativas
SET id_caja = (SELECT id_caja FROM app.cajas_operativas WHERE principal = TRUE LIMIT 1)
WHERE tipo = 'CAJA'
  AND id_caja IS NULL;
