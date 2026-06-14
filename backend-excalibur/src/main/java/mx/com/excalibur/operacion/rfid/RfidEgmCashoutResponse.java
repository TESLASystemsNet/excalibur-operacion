package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

public record RfidEgmCashoutResponse(
        boolean success,
        String uid,
        String txid,
        String operationId,
        String egmAddr,
        String estadoRfid,
        String estadoEgm,
        BigDecimal montoDevuelto,
        BigDecimal saldoDisponible,
        Integer creditosDevueltos,
        Long creditsBefore,
        Long creditsAfter,
        String aftTxid,
        String mensaje
) {
}
