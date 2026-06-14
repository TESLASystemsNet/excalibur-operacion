package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

public record RfidEgmLoadResponse(
        boolean success,
        String uid,
        String txid,
        String operationId,
        String egmAddr,
        String estadoRfid,
        String estadoEgm,
        BigDecimal saldoAutorizado,
        BigDecimal saldoDisponible,
        Integer creditos,
        Long creditsBefore,
        Long creditsAfter,
        Long deltaCredits,
        String aftTxid,
        String mensaje
) {
}
