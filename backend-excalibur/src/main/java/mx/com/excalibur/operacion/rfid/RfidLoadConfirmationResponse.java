package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

public record RfidLoadConfirmationResponse(
        boolean success,
        String txid,
        String uid,
        String estado,
        BigDecimal saldoDisponible,
        String mensaje
) {
}
