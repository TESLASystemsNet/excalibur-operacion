package mx.com.excalibur.operacion.users;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<UserSummary> findAll(String search, Boolean includeDeleted) {
        String normalizedSearch = normalizeSearch(search);
        String query = """
                SELECT u.id_usuario, u.username, u.email, u.telefono, u.nombre, u.apellido_paterno,
                       u.apellido_materno, u.activo, u.bloqueado, u.eliminado,
                       u.requiere_cambio_password, u.ultimo_login
                FROM app.usuarios u
                WHERE (:includeDeleted = true OR u.eliminado = false)
                """;
        if (normalizedSearch != null) {
            query += """
                      AND (lower(u.username) LIKE lower(:searchPattern)
                           OR lower(u.nombre) LIKE lower(:searchPattern)
                           OR lower(coalesce(u.email, '')) LIKE lower(:searchPattern))
                    """;
        }
        query += """
                ORDER BY u.id_usuario
                """;
        JdbcClient.StatementSpec statementSpec = jdbcClient.sql(query)
                .param("includeDeleted", Boolean.TRUE.equals(includeDeleted))
                .param("searchPattern", "%" + normalizedSearch + "%");
        if (normalizedSearch == null) {
            statementSpec = jdbcClient.sql(query)
                    .param("includeDeleted", Boolean.TRUE.equals(includeDeleted));
        }
        return statementSpec
                .query((rs, rowNum) -> new UserSummary(
                        rs.getLong("id_usuario"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("telefono"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getBoolean("activo"),
                        rs.getBoolean("bloqueado"),
                        rs.getBoolean("eliminado"),
                        rs.getBoolean("requiere_cambio_password"),
                        offsetDateTime(rs, "ultimo_login"),
                        findRolesByUserId(rs.getLong("id_usuario"))
                ))
                .list();
    }

    public Optional<User> findById(Long id) {
        return jdbcClient.sql("""
                SELECT *
                FROM app.usuarios
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .query(userRowMapper())
                .optional();
    }

    public Optional<User> findByUsername(String username) {
        return jdbcClient.sql("""
                SELECT *
                FROM app.usuarios
                WHERE lower(username) = lower(:username)
                  AND eliminado = false
                """)
                .param("username", username)
                .query(userRowMapper())
                .optional();
    }

    public Long create(UserCreateRequest request, String passwordHash) {
        return jdbcClient.sql("""
                INSERT INTO app.usuarios (
                    username, email, telefono, nombre, apellido_paterno, apellido_materno,
                    password_hash, password_algoritmo, password_actualizado_en,
                    activo, bloqueado, intentos_fallidos, requiere_cambio_password, eliminado
                )
                VALUES (
                    :username, :email, :telefono, :nombre, :apellidoPaterno, :apellidoMaterno,
                    :passwordHash, 'BCRYPT', CURRENT_TIMESTAMP,
                    :activo, false, 0, :requiereCambioPassword, false
                )
                RETURNING id_usuario
                """)
                .params(Map.of(
                        "username", request.username().trim(),
                        "passwordHash", passwordHash,
                        "activo", request.activo() == null || request.activo(),
                        "requiereCambioPassword", request.requiereCambioPassword() == null || request.requiereCambioPassword()
                ))
                .param("email", blankToNull(request.email()))
                .param("telefono", blankToNull(request.telefono()))
                .param("nombre", request.nombre().trim())
                .param("apellidoPaterno", blankToNull(request.apellidoPaterno()))
                .param("apellidoMaterno", blankToNull(request.apellidoMaterno()))
                .query(Long.class)
                .single();
    }

    public void update(Long id, UserUpdateRequest request) {
        jdbcClient.sql("""
                UPDATE app.usuarios
                SET email = COALESCE(:email, email),
                    telefono = COALESCE(:telefono, telefono),
                    nombre = COALESCE(:nombre, nombre),
                    apellido_paterno = COALESCE(:apellidoPaterno, apellido_paterno),
                    apellido_materno = COALESCE(:apellidoMaterno, apellido_materno),
                    activo = COALESCE(:activo, activo),
                    bloqueado = COALESCE(:bloqueado, bloqueado),
                    motivo_bloqueo = :motivoBloqueo,
                    requiere_cambio_password = COALESCE(:requiereCambioPassword, requiere_cambio_password),
                    fecha_actualizacion = CURRENT_TIMESTAMP,
                    version = version + 1
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .param("email", blankToNull(request.email()))
                .param("telefono", blankToNull(request.telefono()))
                .param("nombre", blankToNull(request.nombre()))
                .param("apellidoPaterno", blankToNull(request.apellidoPaterno()))
                .param("apellidoMaterno", blankToNull(request.apellidoMaterno()))
                .param("activo", request.activo())
                .param("bloqueado", request.bloqueado())
                .param("motivoBloqueo", blankToNull(request.motivoBloqueo()))
                .param("requiereCambioPassword", request.requiereCambioPassword())
                .update();
    }

    public void changePassword(Long id, String passwordHash, boolean requiereCambioPassword) {
        jdbcClient.sql("""
                UPDATE app.usuarios
                SET password_hash = :passwordHash,
                    password_algoritmo = 'BCRYPT',
                    password_actualizado_en = CURRENT_TIMESTAMP,
                    requiere_cambio_password = :requiereCambioPassword,
                    intentos_fallidos = 0,
                    fecha_actualizacion = CURRENT_TIMESTAMP,
                    version = version + 1
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .param("passwordHash", passwordHash)
                .param("requiereCambioPassword", requiereCambioPassword)
                .update();
    }

    public void softDelete(Long id, Long deletedBy) {
        jdbcClient.sql("""
                UPDATE app.usuarios
                SET activo = false,
                    eliminado = true,
                    fecha_eliminacion = CURRENT_TIMESTAMP,
                    eliminado_por = :deletedBy,
                    fecha_actualizacion = CURRENT_TIMESTAMP,
                    version = version + 1
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .param("deletedBy", deletedBy)
                .update();
    }

    public void recordSuccessfulLogin(Long id) {
        jdbcClient.sql("""
                UPDATE app.usuarios
                SET ultimo_login = CURRENT_TIMESTAMP,
                    intentos_fallidos = 0
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .update();
    }

    public void recordFailedLogin(Long id) {
        jdbcClient.sql("""
                UPDATE app.usuarios
                SET intentos_fallidos = intentos_fallidos + 1
                WHERE id_usuario = :id
                """)
                .param("id", id)
                .update();
    }

    public void replaceRoles(Long userId, List<Long> roleIds, Long assignedBy) {
        jdbcClient.sql("UPDATE app.usuario_roles SET activo = false WHERE id_usuario = :userId")
                .param("userId", userId)
                .update();

        for (Long roleId : roleIds) {
            jdbcClient.sql("""
                    INSERT INTO app.usuario_roles (id_usuario, id_rol, asignado_por, activo)
                    VALUES (:userId, :roleId, :assignedBy, true)
                    ON CONFLICT (id_usuario, id_rol)
                    DO UPDATE SET activo = true,
                                  fecha_asignacion = CURRENT_TIMESTAMP,
                                  asignado_por = EXCLUDED.asignado_por
                    """)
                    .param("userId", userId)
                    .param("roleId", roleId)
                    .param("assignedBy", assignedBy)
                    .update();
        }
    }

    public List<String> findRolesByUserId(Long userId) {
        return jdbcClient.sql("""
                SELECT r.nombre
                FROM app.usuario_roles ur
                JOIN app.roles r ON r.id_rol = ur.id_rol
                WHERE ur.id_usuario = :userId
                  AND ur.activo = true
                  AND r.activo = true
                ORDER BY r.nombre
                """)
                .param("userId", userId)
                .query(String.class)
                .list();
    }

    public List<String> findPermissionsByUserId(Long userId) {
        return jdbcClient.sql("""
                SELECT DISTINCT p.codigo
                FROM app.usuario_roles ur
                JOIN app.rol_permisos rp ON rp.id_rol = ur.id_rol
                JOIN app.permisos p ON p.id_permiso = rp.id_permiso
                WHERE ur.id_usuario = :userId
                  AND ur.activo = true
                  AND rp.activo = true
                  AND p.activo = true
                ORDER BY p.codigo
                """)
                .param("userId", userId)
                .query(String.class)
                .list();
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            Long userId = rs.getLong("id_usuario");
            return new User(
                    userId,
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("nombre"),
                    rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno"),
                    rs.getString("password_hash"),
                    rs.getBoolean("activo"),
                    rs.getBoolean("bloqueado"),
                    rs.getString("motivo_bloqueo"),
                    rs.getInt("intentos_fallidos"),
                    rs.getBoolean("requiere_cambio_password"),
                    offsetDateTime(rs, "ultimo_login"),
                    offsetDateTime(rs, "ultimo_logout"),
                    rs.getBoolean("mfa_habilitado"),
                    rs.getString("mfa_tipo"),
                    rs.getBoolean("eliminado"),
                    rs.getInt("version"),
                    findRolesByUserId(userId),
                    findPermissionsByUserId(userId)
            );
        };
    }

    private static OffsetDateTime offsetDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
