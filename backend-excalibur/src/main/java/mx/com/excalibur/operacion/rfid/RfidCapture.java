package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

record RfidCapture(
        Long id,
        String txid,
        String uid,
        Long tarjetaId,
        Long clienteTarjetaId,
        Long cajaJornadaId,
        String egmAddr,
        BigDecimal saldoAutorizado,
        int creditos,
        String estado
) {
}
