package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

public record RfidLoadAuthorizationResponse(
        boolean success,
        String txid,
        String uid,
        BigDecimal saldoAutorizado,
        int creditos,
        String estado,
        String mensaje
) {
}
