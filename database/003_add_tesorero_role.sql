INSERT INTO app.roles (nombre, descripcion)
VALUES ('TESORERO', 'Operacion de tesoreria y movimientos de efectivo')
ON CONFLICT DO NOTHING;

ALTER TABLE app.asignaciones_operativas
    DROP CONSTRAINT IF EXISTS ck_asignaciones_rol;

ALTER TABLE app.asignaciones_operativas
    ADD CONSTRAINT ck_asignaciones_rol
    CHECK (rol_operativo IN ('SUPERVISOR', 'TESORERO', 'CAJERO'));
