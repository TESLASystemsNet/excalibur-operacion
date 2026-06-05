package mx.com.excalibur.operacion.operational;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mx.com.excalibur.operacion.common.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationalConfigService {

    private static final Logger log = LoggerFactory.getLogger(OperationalConfigService.class);

    private final OperationalConfigRepository repository;

    public OperationalConfigService(OperationalConfigRepository repository) {
        this.repository = repository;
    }

    public List<OperationSchedule> findOperationSchedules() {
        return repository.findOperationSchedules();
    }

    @Transactional
    public OperationSchedule createOperationSchedule(OperationScheduleRequest request, Long userId, String ipOrigen) {
        validateTimeRange(request.horaInicio(), request.horaFin());
        Long id = repository.createOperationSchedule(request, userId);
        audit("CREAR", "HORARIO_OPERACION", id, request.nombre(), userId, ipOrigen);
        log.info("Horario de operacion creado id={} userId={}", id, userId);
        return repository.findOperationSchedules().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public void updateOperationSchedule(Long id, OperationScheduleRequest request, Long userId, String ipOrigen) {
        validateTimeRange(request.horaInicio(), request.horaFin());
        repository.updateOperationSchedule(id, request, userId);
        audit("ACTUALIZAR", "HORARIO_OPERACION", id, request.nombre(), userId, ipOrigen);
        log.info("Horario de operacion actualizado id={} userId={}", id, userId);
    }

    @Transactional
    public void deleteOperationSchedule(Long id, Long userId, String ipOrigen) {
        repository.deleteOperationSchedule(id);
        audit("ELIMINAR", "HORARIO_OPERACION", id, null, userId, ipOrigen);
        log.info("Horario de operacion eliminado id={} userId={}", id, userId);
    }

    public List<CashierShift> findCashierShifts() {
        return repository.findCashierShifts();
    }

    @Transactional
    public CashierShift createCashierShift(CashierShiftRequest request, Long userId, String ipOrigen) {
        validateTimeRange(request.horaInicio(), request.horaFin());
        Long id = repository.createCashierShift(request, userId);
        audit("CREAR", "TURNO_CAJA", id, request.nombre(), userId, ipOrigen);
        log.info("Turno de caja creado id={} userId={}", id, userId);
        return repository.findCashierShifts().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public void updateCashierShift(Long id, CashierShiftRequest request, Long userId, String ipOrigen) {
        validateTimeRange(request.horaInicio(), request.horaFin());
        repository.updateCashierShift(id, request, userId);
        audit("ACTUALIZAR", "TURNO_CAJA", id, request.nombre(), userId, ipOrigen);
        log.info("Turno de caja actualizado id={} userId={}", id, userId);
    }

    @Transactional
    public void deleteCashierShift(Long id, Long userId, String ipOrigen) {
        repository.deleteCashierShift(id);
        audit("ELIMINAR", "TURNO_CAJA", id, null, userId, ipOrigen);
        log.info("Turno de caja eliminado id={} userId={}", id, userId);
    }

    public List<Workstation> findWorkstations() {
        return repository.findWorkstations();
    }

    @Transactional
    public Workstation createWorkstation(WorkstationRequest request, Long userId, String ipOrigen) {
        validateIn(request.tipo(), List.of("CAJA", "TESORERIA"), "Tipo de estacion invalido");
        Long id = repository.createWorkstation(request, userId);
        audit("CREAR", "ESTACION_OPERATIVA", id, request.nombre(), userId, ipOrigen);
        log.info("Estacion operativa creada id={} userId={}", id, userId);
        return repository.findWorkstations().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public void updateWorkstation(Long id, WorkstationRequest request, Long userId, String ipOrigen) {
        validateIn(request.tipo(), List.of("CAJA", "TESORERIA"), "Tipo de estacion invalido");
        repository.updateWorkstation(id, request, userId);
        audit("ACTUALIZAR", "ESTACION_OPERATIVA", id, request.nombre(), userId, ipOrigen);
        log.info("Estacion operativa actualizada id={} userId={}", id, userId);
    }

    @Transactional
    public void deleteWorkstation(Long id, Long userId, String ipOrigen) {
        repository.deleteWorkstation(id);
        audit("ELIMINAR", "ESTACION_OPERATIVA", id, null, userId, ipOrigen);
        log.info("Estacion operativa eliminada id={} userId={}", id, userId);
    }

    public List<OperationalAssignment> findAssignments(LocalDate fechaOperacion) {
        return repository.findAssignments(fechaOperacion);
    }

    @Transactional
    public OperationalAssignment createAssignment(OperationalAssignmentRequest request, Long userId, String ipOrigen) {
        validateIn(request.rolOperativo(), List.of("SUPERVISOR", "TESORERO", "CAJERO"), "Rol operativo invalido");
        Long id = repository.createAssignment(request, userId);
        audit("CREAR", "ASIGNACION_OPERATIVA", id, request.rolOperativo(), userId, ipOrigen);
        log.info("Asignacion operativa creada id={} userId={}", id, userId);
        return repository.findAssignments(request.fechaOperacion()).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public void updateAssignment(Long id, OperationalAssignmentRequest request, Long userId, String ipOrigen) {
        validateIn(request.rolOperativo(), List.of("SUPERVISOR", "TESORERO", "CAJERO"), "Rol operativo invalido");
        repository.updateAssignment(id, request, userId);
        audit("ACTUALIZAR", "ASIGNACION_OPERATIVA", id, request.rolOperativo(), userId, ipOrigen);
        log.info("Asignacion operativa actualizada id={} userId={}", id, userId);
    }

    @Transactional
    public void deleteAssignment(Long id, Long userId, String ipOrigen) {
        repository.deleteAssignment(id);
        audit("ELIMINAR", "ASIGNACION_OPERATIVA", id, null, userId, ipOrigen);
        log.info("Asignacion operativa eliminada id={} userId={}", id, userId);
    }

    public List<OperationalCard> findCards(String search) {
        return repository.findCards(search);
    }

    @Transactional
    public OperationalCard createCard(OperationalCardRequest request, Long userId, String ipOrigen) {
        validateCard(request);
        Long id = repository.createCard(request, userId);
        audit("CREAR", "TARJETA_OPERATIVA", id, request.numeroTarjeta(), userId, ipOrigen);
        log.info("Tarjeta operativa creada id={} userId={}", id, userId);
        return repository.findCards(request.numeroTarjeta()).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public void updateCard(Long id, OperationalCardRequest request, Long userId, String ipOrigen) {
        validateCard(request);
        repository.updateCard(id, request, userId);
        audit("ACTUALIZAR", "TARJETA_OPERATIVA", id, request.numeroTarjeta(), userId, ipOrigen);
        log.info("Tarjeta operativa actualizada id={} userId={}", id, userId);
    }

    @Transactional
    public void deleteCard(Long id, Long userId, String ipOrigen) {
        repository.deleteCard(id);
        audit("ELIMINAR", "TARJETA_OPERATIVA", id, null, userId, ipOrigen);
        log.info("Tarjeta operativa eliminada id={} userId={}", id, userId);
    }

    @Transactional
    public List<OperationalCard> createCardRange(CardRangeRequest request, Long userId, String ipOrigen) {
        validateIn(request.tipo(), List.of("CLIENTE", "GENERICA"), "Tipo de tarjeta invalido");
        long start = parseCardNumber(request.numeroInicial());
        long end = parseCardNumber(request.numeroFinal());
        if (end < start) {
            throw new BadRequestException("El numero final debe ser mayor o igual al inicial");
        }
        long total = end - start + 1;
        if (request.maximoTarjetas() == null || request.maximoTarjetas() < 1 || total > request.maximoTarjetas()) {
            throw new BadRequestException("Rango de tarjetas demasiado grande");
        }
        List<OperationalCard> created = new ArrayList<>();
        int width = request.numeroInicial().trim().length();
        for (long current = start; current <= end; current++) {
            String number = String.format("%0" + width + "d", current);
            OperationalCardRequest card = new OperationalCardRequest(
                    number,
                    request.tipo(),
                    request.fechaVencimiento(),
                    "DISPONIBLE"
            );
            Long id = repository.createCard(card, userId);
            created.add(repository.findCards(number).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow());
        }
        audit("CREAR_RANGO", "TARJETA_OPERATIVA", null,
                request.numeroInicial() + "-" + request.numeroFinal(), userId, ipOrigen);
        log.info("Rango de tarjetas creado total={} userId={}", created.size(), userId);
        return created;
    }

    public List<OperationalAuditEvent> findAuditEvents(String entidad, Long entidadId) {
        return repository.findAuditEvents(entidad, entidadId);
    }

    private void audit(String accion, String entidad, Long entidadId, String detalle, Long userId, String ipOrigen) {
        repository.audit(accion, entidad, entidadId, detalle, userId, ipOrigen);
    }

    private static void validateTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (start.equals(end)) {
            throw new BadRequestException("La hora inicio y fin no pueden ser iguales");
        }
    }

    private static void validateCard(OperationalCardRequest request) {
        validateIn(request.tipo(), List.of("CLIENTE", "GENERICA"), "Tipo de tarjeta invalido");
        if (request.estado() != null && !request.estado().isBlank()) {
            validateIn(request.estado(), List.of("DISPONIBLE", "ASIGNADA", "BLOQUEADA", "VENCIDA", "INACTIVA"),
                    "Estado de tarjeta invalido");
        }
    }

    private static void validateIn(String value, List<String> allowed, String message) {
        if (value == null || !allowed.contains(value.trim().toUpperCase())) {
            throw new BadRequestException(message);
        }
    }

    private static long parseCardNumber(String value) {
        if (value == null || value.isBlank() || !value.trim().matches("\\d+")) {
            throw new BadRequestException("El numero de tarjeta debe ser numerico");
        }
        return Long.parseLong(value.trim());
    }
}
