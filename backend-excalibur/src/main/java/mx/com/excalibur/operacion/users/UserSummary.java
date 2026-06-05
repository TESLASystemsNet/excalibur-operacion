package mx.com.excalibur.operacion.users;

import java.time.OffsetDateTime;
import java.util.List;

public record UserSummary(
        Long id,
        String username,
        String email,
        String telefono,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        boolean activo,
        boolean bloqueado,
        boolean eliminado,
        boolean requiereCambioPassword,
        OffsetDateTime ultimoLogin,
        List<String> roles
) {
}
