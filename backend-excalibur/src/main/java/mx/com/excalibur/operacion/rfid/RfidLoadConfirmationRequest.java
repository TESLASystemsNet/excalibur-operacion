package mx.com.excalibur.operacion.rfid;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RfidLoadConfirmationRequest(
        @NotBlank String txid,
        @NotBlank String uid,
        String resultado,
        BigDecimal montoCargado,
        Integer creditosEnviados,
        String mensaje
) {
}
