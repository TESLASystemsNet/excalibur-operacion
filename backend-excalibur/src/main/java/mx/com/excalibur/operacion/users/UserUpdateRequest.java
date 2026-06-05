package mx.com.excalibur.operacion.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserUpdateRequest(
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefono,
        @Size(max = 100) String nombre,
        @Size(max = 100) String apellidoPaterno,
        @Size(max = 100) String apellidoMaterno,
        Boolean activo,
        Boolean bloqueado,
        @Size(max = 255) String motivoBloqueo,
        Boolean requiereCambioPassword,
        List<String> roles
) {
}
