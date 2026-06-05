package mx.com.excalibur.operacion.roles;

public record RoleResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo
) {
}
