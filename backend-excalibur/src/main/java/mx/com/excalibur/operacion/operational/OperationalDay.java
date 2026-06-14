package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OperationalDay(
        Long id,
        LocalDate fechaJornada,
        String estado,
        OffsetDateTime fechaApertura,
        OffsetDateTime fechaCierre,
        Long aperturaPor,
        String aperturaUsername,
        String aperturaNombre,
        Long cierrePor,
        String cierreUsername,
        String observaciones
) {
}
