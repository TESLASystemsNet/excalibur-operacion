package mx.com.excalibur.operacion.operational;

public record OperationalCloseRequest(
        String observaciones,
        Boolean forzarConDiferencias
) {
}
