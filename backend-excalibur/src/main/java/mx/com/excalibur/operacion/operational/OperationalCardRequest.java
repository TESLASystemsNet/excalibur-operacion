package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record OperationalCardRequest(
        @NotBlank String numeroTarjeta,
        @NotBlank String tipo,
        LocalDate fechaVencimiento,
        String estado
) {
}
