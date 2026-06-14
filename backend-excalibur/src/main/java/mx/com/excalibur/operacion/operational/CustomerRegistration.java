package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CustomerRegistration(
        Long clienteId,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String email,
        LocalDate fechaNacimiento,
        String documentoIdentidad,
        String estadoCliente,
        Long tarjetaId,
        String numeroTarjeta,
        String estadoTarjeta,
        Long asignacionId,
        Long cajaJornadaId,
        String cajaNombre,
        Long turnoId,
        String turnoNombre,
        Long asignadoPor,
        String asignadoUsername,
        OffsetDateTime fechaAsignacion,
        String observaciones
) {
}
