package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OperationalAssignmentRequest(
        @NotNull Long usuarioId,
        @NotNull Long estacionId,
        @NotNull Long turnoId,
        @NotNull LocalDate fechaOperacion,
        @NotBlank String rolOperativo,
        Boolean activa
) {
}
