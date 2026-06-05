package mx.com.excalibur.operacion.operational;

import java.time.OffsetDateTime;

public record Workstation(
        Long id,
        String nombre,
        String tipo,
        String sala,
        String ubicacion,
        boolean activa,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
