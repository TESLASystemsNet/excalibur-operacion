package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TreasuryMovementRequest(
        String tipo,
        String concepto,
        @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
        Long estacionCajaId,
        Long turnoId,
        String referencia,
        String observaciones
) {
}
