package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EgmDailyReconciliation(
        Long id,
        Long jornadaId,
        Long egmId,
        String egmAddr,
        String egmNombre,
        BigDecimal coinInDelta,
        BigDecimal coinOutDelta,
        BigDecimal jackpotDelta,
        BigDecimal cancelledDelta,
        BigDecimal billsAcceptedDelta,
        BigDecimal currentCreditsCierre,
        BigDecimal hostLoads,
        BigDecimal hostCashouts,
        BigDecimal gananciaCalculada,
        BigDecimal perdidaCalculada,
        BigDecimal cajaReportado,
        BigDecimal diferenciaVsCaja,
        String estado,
        String detalle,
        OffsetDateTime fechaCalculo
) {
}
