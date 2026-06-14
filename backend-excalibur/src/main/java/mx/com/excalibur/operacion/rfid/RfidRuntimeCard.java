package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;

record RfidRuntimeCard(
        Long tarjetaId,
        String numeroTarjeta,
        String tarjetaEstado,
        Long clienteTarjetaId,
        Long cajaJornadaId,
        String cajaEstado,
        Long registradoPor,
        String clienteNombre,
        BigDecimal saldo
) {
}
