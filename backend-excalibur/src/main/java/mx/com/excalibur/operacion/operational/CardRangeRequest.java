package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CardRangeRequest(
        @NotBlank String numeroInicial,
        @NotBlank String numeroFinal,
        @NotBlank String tipo,
        LocalDate fechaVencimiento,
        @NotNull Integer maximoTarjetas
) {
}
