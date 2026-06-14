package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CashierSession(
        Long id,
        Long jornadaId,
        LocalDate fechaJornada,
        Long cajaId,
        String cajaNombre,
        Long estacionId,
        String estacionNombre,
        Long turnoId,
        String turnoNombre,
        String estado,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        Integer tarjetasIniciales,
        Integer tarjetasActuales,
        OffsetDateTime fechaApertura,
        String aperturaUsername,
        OffsetDateTime fechaPrecierre,
        String precierreUsername,
        OffsetDateTime fechaCierre,
        String cierreUsername,
        BigDecimal montoDeclaradoCierre,
        Integer tarjetasDevueltasCierre,
        String observaciones
) {
}
