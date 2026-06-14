package mx.com.excalibur.operacion.rfid;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RfidCashoutReturnRequest(
        @NotBlank String uid,
        @NotBlank String txid,
        BigDecimal montoDevuelto,
        Integer creditosDevueltos,
        String mensaje
) {
}
