package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OperationalCard(
        Long id,
        String numeroTarjeta,
        String tipo,
        LocalDate fechaVencimiento,
        String estado,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
