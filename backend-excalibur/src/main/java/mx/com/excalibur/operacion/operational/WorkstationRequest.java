package mx.com.excalibur.operacion.operational;

import jakarta.validation.constraints.NotBlank;

public record WorkstationRequest(
        @NotBlank String nombre,
        @NotBlank String tipo,
        String sala,
        String ubicacion,
        Boolean activa
) {
}
