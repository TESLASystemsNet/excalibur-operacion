package mx.com.excalibur.operacion.operational;

import java.time.OffsetDateTime;

public record TreasuryCardMovement(
        Long id,
        Long tesoreriaJornadaId,
        Long estacionCajaId,
        String estacionCajaNombre,
        Long turnoId,
        String turnoNombre,
        String tipo,
        String numeroInicial,
        String numeroFinal,
        Integer cantidad,
        String referencia,
        String observaciones,
        Long registradoPor,
        String registradoUsername,
        OffsetDateTime fechaMovimiento
) {
}
