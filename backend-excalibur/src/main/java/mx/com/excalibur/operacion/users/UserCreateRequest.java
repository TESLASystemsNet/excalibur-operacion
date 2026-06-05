package mx.com.excalibur.operacion.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserCreateRequest(
        @NotBlank @Size(max = 100) String username,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefono,
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 100) String apellidoPaterno,
        @Size(max = 100) String apellidoMaterno,
        @NotBlank @Size(min = 8, max = 100) String password,
        Boolean activo,
        Boolean requiereCambioPassword,
        List<String> roles
) {
}
