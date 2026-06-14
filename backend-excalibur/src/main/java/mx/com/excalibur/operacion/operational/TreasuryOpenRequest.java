package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TreasuryOpenRequest(
        @NotNull Long estacionId,
        @NotNull @DecimalMin(value = "0.00") BigDecimal saldoInicial,
        String observaciones
) {
}
