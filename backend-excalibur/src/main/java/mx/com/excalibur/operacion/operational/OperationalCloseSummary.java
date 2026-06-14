package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OperationalCloseSummary(
        Long id,
        OperationalDay jornada,
        String estado,
        Integer porcentaje,
        Integer totalEgms,
        Integer egmsOk,
        Integer egmsDiferencia,
        Integer egmsIncompletas,
        BigDecimal totalCajaReportado,
        BigDecimal totalEgmCalculado,
        BigDecimal diferenciaTotal,
        String observaciones,
        String iniciadoUsername,
        OffsetDateTime fechaInicio,
        OffsetDateTime fechaFin,
        List<CashierSession> cajasCerradas,
        List<EgmMeterSnapshot> snapshots,
        List<EgmDailyReconciliation> conciliaciones
) {
}
