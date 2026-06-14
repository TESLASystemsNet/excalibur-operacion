package mx.com.excalibur.operacion.operational;

import java.time.OffsetDateTime;

public record Workstation(
        Long id,
        Long cajaId,
        String cajaNombre,
        String nombre,
        String tipo,
        String sala,
        String ubicacion,
        boolean activa,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
