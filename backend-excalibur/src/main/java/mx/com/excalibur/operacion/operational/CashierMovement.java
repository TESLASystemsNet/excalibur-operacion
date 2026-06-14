package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CashierMovement(
        Long id,
        Long cajaJornadaId,
        Long estacionId,
        String estacionNombre,
        String tipo,
        String numeroTarjeta,
        BigDecimal monto,
        BigDecimal impactoSaldo,
        String maquina,
        String motivo,
        String referencia,
        String observaciones,
        Long registradoPor,
        String registradoUsername,
        OffsetDateTime fechaMovimiento
) {
}
