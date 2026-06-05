package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record OperationScheduleRequest(
        @NotBlank String nombre,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        Boolean activo
) {
}
