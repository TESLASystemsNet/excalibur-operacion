package mx.com.excalibur.operacion.rfid;

import jakarta.validation.constraints.NotBlank;

public record RfidBalanceRequest(@NotBlank String uid) {
}
