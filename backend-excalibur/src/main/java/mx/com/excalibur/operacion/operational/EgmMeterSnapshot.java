package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EgmMeterSnapshot(
        Long id,
        Long jornadaId,
        Long egmId,
        String egmAddr,
        String egmNombre,
        String tipoSnapshot,
        String estado,
        String proveedor,
        BigDecimal coinIn,
        BigDecimal coinOut,
        BigDecimal jackpot,
        BigDecimal handpayCancelled,
        BigDecimal cancelled,
        BigDecimal gamesPlayed,
        BigDecimal gamesWon,
        BigDecimal gamesLost,
        BigDecimal billsAccepted,
        BigDecimal currentCredits,
        String rawResponse,
        String mensaje,
        OffsetDateTime fechaSnapshot
) {
}
