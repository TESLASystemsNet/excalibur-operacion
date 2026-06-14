package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

public record RfidBalanceResponse(
        boolean success,
        String uid,
        String nombre,
        BigDecimal saldo,
        boolean bloqueada,
        String mensaje
) {
}
