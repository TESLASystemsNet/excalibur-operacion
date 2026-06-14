package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record CustomerRegistrationRequest(
        @NotBlank String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String email,
        LocalDate fechaNacimiento,
        String documentoIdentidad,
        @NotBlank String numeroTarjeta,
        @Pattern(regexp = "\\d{4}", message = "El NIP debe ser de 4 digitos")
        String nip,
        String observaciones
) {
}
