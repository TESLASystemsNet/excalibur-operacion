package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TreasuryMovement(
        Long id,
        Long tesoreriaJornadaId,
        Long estacionCajaId,
        String estacionCajaNombre,
        Long turnoId,
        String turnoNombre,
        String tipo,
        String concepto,
        BigDecimal monto,
        String referencia,
        String observaciones,
        Long registradoPor,
        String registradoUsername,
        OffsetDateTime fechaMovimiento
) {
}
