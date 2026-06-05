package mx.com.excalibur.operacion.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
