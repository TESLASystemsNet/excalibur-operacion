package mx.com.excalibur.operacion.operational;

import java.util.List;

public record TreasuryConsole(
        OperationalDay jornada,
        TreasurySession tesoreria,
        List<OperationalAssignment> asignaciones,
        List<TreasuryMovement> movimientos,
        List<TreasuryCardMovement> movimientosTarjetas,
        List<CashierSession> cajasCerradas
) {
}
