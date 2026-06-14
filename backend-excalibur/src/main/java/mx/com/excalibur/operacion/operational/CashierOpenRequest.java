package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashierOpenRequest(
        @NotNull Long estacionId,
        @NotNull Long turnoId,
        @NotNull @DecimalMin(value = "0.00") BigDecimal montoApertura,
        @NotNull @Min(0) Integer tarjetasApertura,
        String observaciones
) {
}
