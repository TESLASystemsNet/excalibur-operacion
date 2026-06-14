package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;

public record EgmMachine(
        Long id,
        String egmAddr,
        String nombre,
        String sala,
        boolean activo,
        String raspberryBaseUrl,
        BigDecimal denominacion,
        Integer timeoutSegundos
) {
}
