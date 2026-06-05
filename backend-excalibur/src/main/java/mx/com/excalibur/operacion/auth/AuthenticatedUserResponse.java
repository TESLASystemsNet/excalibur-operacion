package mx.com.excalibur.operacion.auth;

import java.util.List;

public record AuthenticatedUserResponse(
        Long userId,
        String username,
        boolean requiereCambioPassword,
        List<String> roles,
        List<String> permisos
) {
    public static AuthenticatedUserResponse from(AuthenticatedUser user) {
        return new AuthenticatedUserResponse(
                user.id(),
                user.getUsername(),
                user.requiereCambioPassword(),
                user.roles(),
                user.permisos()
        );
    }
}
