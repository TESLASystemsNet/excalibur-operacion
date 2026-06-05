package mx.com.excalibur.operacion.auth;

import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        Long userId,
        String username,
        boolean requiereCambioPassword,
        List<String> roles,
        List<String> permisos
) {
}
