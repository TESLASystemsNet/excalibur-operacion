package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OperationalCard(
        Long id,
        String numeroTarjeta,
        String tipo,
        LocalDate fechaVencimiento,
        String estado,
        String clienteNombre,
        Boolean capturadaEgm,
        String capturaEgmTxid,
        OffsetDateTime capturaEgmFecha,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
