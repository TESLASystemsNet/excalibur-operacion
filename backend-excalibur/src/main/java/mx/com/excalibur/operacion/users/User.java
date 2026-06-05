package mx.com.excalibur.operacion.users;

import java.time.OffsetDateTime;
import java.util.List;

public record User(
        Long id,
        String username,
        String email,
        String telefono,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String passwordHash,
        boolean activo,
        boolean bloqueado,
        String motivoBloqueo,
        int intentosFallidos,
        boolean requiereCambioPassword,
        OffsetDateTime ultimoLogin,
        OffsetDateTime ultimoLogout,
        boolean mfaHabilitado,
        String mfaTipo,
        boolean eliminado,
        int version,
        List<String> roles,
        List<String> permisos
) {
}
