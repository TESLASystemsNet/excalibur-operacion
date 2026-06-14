package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record CashierCloseRequest(
        @DecimalMin(value = "0.00") BigDecimal montoDeclarado,
        @Min(0) Integer tarjetasDevueltas,
        String observaciones
) {
}
