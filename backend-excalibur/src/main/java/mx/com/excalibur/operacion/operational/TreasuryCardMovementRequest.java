package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TreasuryCardMovementRequest(
        @NotNull Long estacionCajaId,
        Long turnoId,
        @NotBlank String numeroInicial,
        @NotBlank String numeroFinal,
        String referencia,
        String observaciones
) {
}
