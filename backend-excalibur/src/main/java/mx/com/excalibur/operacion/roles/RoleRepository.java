package mx.com.excalibur.operacion.roles;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepository {

    private final JdbcClient jdbcClient;

    public RoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RoleResponse> findAllActive() {
        return jdbcClient.sql("""
                SELECT id_rol, nombre, descripcion, activo
                FROM app.roles
                WHERE activo = true
                ORDER BY nombre
                """)
                .query((rs, rowNum) -> new RoleResponse(
                        rs.getLong("id_rol"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBoolean("activo")
                ))
                .list();
    }

    public List<Long> findActiveIdsByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("""
                SELECT id_rol
                FROM app.roles
                WHERE activo = true
                  AND upper(nombre) IN (:names)
                ORDER BY id_rol
                """)
                .param("names", names.stream().map(String::toUpperCase).toList())
                .query(Long.class)
                .list();
    }
}
