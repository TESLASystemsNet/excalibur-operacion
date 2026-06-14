package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TreasurySession(
        Long id,
        Long jornadaId,
        LocalDate fechaJornada,
        Long estacionId,
        String estacionNombre,
        String estado,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        OffsetDateTime fechaApertura,
        String aperturaUsername,
        OffsetDateTime fechaPrecierre,
        String precierreUsername,
        OffsetDateTime fechaCierre,
        String cierreUsername,
        String observaciones
) {
}
