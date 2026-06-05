package mx.com.excalibur.operacion.users;

import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String telefono,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
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
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.email(),
                user.telefono(),
                user.nombre(),
                user.apellidoPaterno(),
                user.apellidoMaterno(),
                user.activo(),
                user.bloqueado(),
                user.motivoBloqueo(),
                user.intentosFallidos(),
                user.requiereCambioPassword(),
                user.ultimoLogin(),
                user.ultimoLogout(),
                user.mfaHabilitado(),
                user.mfaTipo(),
                user.eliminado(),
                user.version(),
                user.roles(),
                user.permisos()
        );
    }
}
