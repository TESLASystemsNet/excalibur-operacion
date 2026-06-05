CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.usuarios (
    id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    username VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    telefono VARCHAR(20),

    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100),
    apellido_materno VARCHAR(100),

    password_hash VARCHAR(255) NOT NULL,
    password_algoritmo VARCHAR(50) NOT NULL DEFAULT 'BCRYPT',
    password_actualizado_en TIMESTAMPTZ,

    activo BOOLEAN NOT NULL DEFAULT TRUE,
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_bloqueo VARCHAR(255),
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,

    requiere_cambio_password BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login TIMESTAMPTZ,
    ultimo_logout TIMESTAMPTZ,

    mfa_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_tipo VARCHAR(50),

    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,
    creado_por BIGINT,
    actualizado_por BIGINT,

    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_eliminacion TIMESTAMPTZ,
    eliminado_por BIGINT,

    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT ck_usuarios_username_no_vacio CHECK (btrim(username) <> ''),
    CONSTRAINT ck_usuarios_nombre_no_vacio CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_usuarios_intentos_fallidos CHECK (intentos_fallidos >= 0),
    CONSTRAINT ck_usuarios_version CHECK (version >= 0),
    CONSTRAINT ck_usuarios_mfa_tipo CHECK (
        mfa_tipo IS NULL OR mfa_tipo IN ('EMAIL', 'SMS', 'TOTP', 'WHATSAPP')
    ),
    CONSTRAINT fk_usuarios_creado_por
        FOREIGN KEY (creado_por) REFERENCES app.usuarios(id_usuario),
    CONSTRAINT fk_usuarios_actualizado_por
        FOREIGN KEY (actualizado_por) REFERENCES app.usuarios(id_usuario),
    CONSTRAINT fk_usuarios_eliminado_por
        FOREIGN KEY (eliminado_por) REFERENCES app.usuarios(id_usuario)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_username_activo
    ON app.usuarios (lower(username))
    WHERE eliminado = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_email_activo
    ON app.usuarios (lower(email))
    WHERE email IS NOT NULL AND eliminado = FALSE;

CREATE INDEX IF NOT EXISTS ix_usuarios_estado
    ON app.usuarios (activo, bloqueado, eliminado);

CREATE TABLE IF NOT EXISTS app.roles (
    id_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,

    CONSTRAINT ck_roles_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_roles_nombre
    ON app.roles (upper(nombre));

CREATE TABLE IF NOT EXISTS app.permisos (
    id_permiso BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    modulo VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ,

    CONSTRAINT ck_permisos_codigo_no_vacio CHECK (btrim(codigo) <> ''),
    CONSTRAINT ck_permisos_modulo_no_vacio CHECK (btrim(modulo) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_permisos_codigo
    ON app.permisos (upper(codigo));

CREATE INDEX IF NOT EXISTS ix_permisos_modulo
    ON app.permisos (modulo);

CREATE TABLE IF NOT EXISTS app.usuario_roles (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id_usuario, id_rol),

    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (id_usuario) REFERENCES app.usuarios(id_usuario),
    CONSTRAINT fk_usuario_roles_rol
        FOREIGN KEY (id_rol) REFERENCES app.roles(id_rol),
    CONSTRAINT fk_usuario_roles_asignado_por
        FOREIGN KEY (asignado_por) REFERENCES app.usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS ix_usuario_roles_rol
    ON app.usuario_roles (id_rol);

CREATE TABLE IF NOT EXISTS app.rol_permisos (
    id_rol BIGINT NOT NULL,
    id_permiso BIGINT NOT NULL,
    fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id_rol, id_permiso),

    CONSTRAINT fk_rol_permisos_rol
        FOREIGN KEY (id_rol) REFERENCES app.roles(id_rol),
    CONSTRAINT fk_rol_permisos_permiso
        FOREIGN KEY (id_permiso) REFERENCES app.permisos(id_permiso),
    CONSTRAINT fk_rol_permisos_asignado_por
        FOREIGN KEY (asignado_por) REFERENCES app.usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS ix_rol_permisos_permiso
    ON app.rol_permisos (id_permiso);

INSERT INTO app.roles (nombre, descripcion)
VALUES
    ('ADMIN', 'Administrador general del sistema'),
    ('GERENTE', 'Gestion operativa y reportes'),
    ('SUPERVISOR', 'Supervision de operacion de sala'),
    ('TESORERO', 'Operacion de tesoreria y movimientos de efectivo'),
    ('CAJERO', 'Operacion de caja y tarjetas'),
    ('AUDITOR', 'Consulta de auditoria y reportes'),
    ('SOPORTE', 'Soporte tecnico del sistema')
ON CONFLICT DO NOTHING;

INSERT INTO app.permisos (codigo, descripcion, modulo)
VALUES
    ('USUARIOS_CREAR', 'Crear usuarios del sistema', 'USUARIOS'),
    ('USUARIOS_EDITAR', 'Editar usuarios del sistema', 'USUARIOS'),
    ('USUARIOS_ELIMINAR', 'Dar de baja usuarios del sistema', 'USUARIOS'),
    ('USUARIOS_VER', 'Consultar usuarios del sistema', 'USUARIOS'),
    ('ROLES_GESTIONAR', 'Administrar roles y permisos', 'SEGURIDAD'),
    ('CAJA_ABRIR', 'Abrir caja operativa', 'CAJA'),
    ('CAJA_CERRAR', 'Cerrar caja operativa', 'CAJA'),
    ('SALDO_CARGAR', 'Cargar saldo a tarjeta', 'TARJETAS'),
    ('SALDO_RETIRAR', 'Retirar saldo de tarjeta', 'TARJETAS'),
    ('REPORTES_VER', 'Consultar reportes', 'REPORTES'),
    ('CONFIGURACION_EDITAR', 'Editar configuracion del sistema', 'CONFIGURACION')
ON CONFLICT DO NOTHING;

INSERT INTO app.rol_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM app.roles r
CROSS JOIN app.permisos p
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;
