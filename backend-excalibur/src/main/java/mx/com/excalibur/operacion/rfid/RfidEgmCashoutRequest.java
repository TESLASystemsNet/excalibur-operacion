package mx.com.excalibur.operacion.rfid;

import jakarta.validation.constraints.NotBlank;

public record RfidEgmCashoutRequest(
        @NotBlank String uid,
        String egmAddr,
        String requestOrigen
) {
}
