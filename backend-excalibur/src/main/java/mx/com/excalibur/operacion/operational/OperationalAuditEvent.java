package mx.com.excalibur.operacion.operational;

import java.time.OffsetDateTime;

public record OperationalAuditEvent(
        Long id,
        String accion,
        String entidad,
        Long entidadId,
        String detalle,
        Long usuarioId,
        String username,
        OffsetDateTime fechaEvento,
        String ipOrigen
) {
}
