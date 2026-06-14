package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TreasuryLedgerEntry(
        String id,
        String categoria,
        String tipo,
        String detalle,
        String estacion,
        String turno,
        BigDecimal monto,
        Integer cantidad,
        String referencia,
        String observaciones,
        String username,
        OffsetDateTime fechaEvento
) {
}
