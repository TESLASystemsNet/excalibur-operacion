package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;

public record OperationalDayOpenRequest(
        LocalDate fechaJornada,
        String observaciones
) {
}
