package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashierMovementRequest(
        String tipo,
        String numeroTarjeta,
        @NotNull @DecimalMin(value = "0.00") BigDecimal monto,
        String maquina,
        String motivo,
        String referencia,
        String observaciones
) {
}
