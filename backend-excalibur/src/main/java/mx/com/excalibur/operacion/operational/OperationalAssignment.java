package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OperationalAssignment(
        Long id,
        Long usuarioId,
        String username,
        String nombreUsuario,
        Long estacionId,
        String estacionNombre,
        String estacionTipo,
        Long turnoId,
        String turnoNombre,
        LocalDate fechaOperacion,
        String rolOperativo,
        boolean activa,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
