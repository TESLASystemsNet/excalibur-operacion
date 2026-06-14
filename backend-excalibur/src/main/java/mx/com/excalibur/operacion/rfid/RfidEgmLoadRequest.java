package mx.com.excalibur.operacion.rfid;

import jakarta.validation.constraints.NotBlank;

public record RfidEgmLoadRequest(
        @NotBlank String uid,
        @NotBlank String egmAddr,
        String requestOrigen
) {
}
