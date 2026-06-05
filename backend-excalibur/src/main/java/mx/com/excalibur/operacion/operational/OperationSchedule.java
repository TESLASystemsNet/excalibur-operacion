package mx.com.excalibur.operacion.operational;

import java.time.LocalTime;
import java.time.OffsetDateTime;

public record OperationSchedule(
        Long id,
        String nombre,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean activo,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
