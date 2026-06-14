package mx.com.excalibur.operacion.operational;

import java.util.List;

public record CashierConsole(
        OperationalDay jornada,
        OperationalAssignment asignacion,
        CashierSession caja,
        List<CashierMovement> movimientos,
        List<CashierMovement> movimientosTarjeta,
        List<TreasuryMovement> fondosTesoreria,
        List<TreasuryCardMovement> tarjetasTesoreria,
        List<CustomerRegistration> clientes
) {
}
