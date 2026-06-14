package mx.com.excalibur.operacion.operational;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.com.excalibur.operacion.common.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationalConfigService {

    private static final Logger log = LoggerFactory.getLogger(OperationalConfigService.class);

    private final OperationalConfigRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EgmSweepClient egmSweepClient;

    public OperationalConfigService(OperationalConfigRepository repository,
                                    PasswordEncoder passwordEncoder,
                                    EgmSweepClient egmSweepClient) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.egmSweepClient = egmSweepClient;
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

    public OperationalDay findCurrentOperationalDay() {
        return repository.findOpenOperationalDay().orElse(null);
    }

    public List<EgmMeterSnapshot> findCurrentOperationalDaySnapshots() {
        OperationalDay currentDay = findCurrentOperationalDay();
        return currentDay == null ? List.of() : repository.findEgmSnapshots(currentDay.id());
    }

    public TreasuryConsole findTreasuryConsole(Long userId) {
        OperationalDay currentDay = findCurrentOperationalDay();
        TreasurySession treasury = repository.findCurrentTreasurySession().orElse(null);
        List<OperationalAssignment> assignments = currentDay == null
                ? List.of()
                : repository.findAssignments(currentDay.fechaJornada()).stream()
                        .filter(item -> item.usuarioId().equals(userId) || "CAJERO".equals(item.rolOperativo()))
                        .toList();
        List<TreasuryMovement> movements = treasury == null
                ? repository.findTreasuryMovements(null)
                : repository.findTreasuryMovements(treasury.id());
        List<TreasuryCardMovement> cardMovements = treasury == null
                ? repository.findTreasuryCardMovements(null)
                : repository.findTreasuryCardMovements(treasury.id());
        List<CashierSession> closedCashiers = currentDay == null
                ? List.of()
                : repository.findClosedCashierSessions(currentDay.id());
        return new TreasuryConsole(currentDay, treasury, assignments, movements, cardMovements, closedCashiers);
    }

    public List<TreasuryMovement> findTreasuryMovements() {
        return repository.findTreasuryMovements(null);
    }

    public List<TreasuryLedgerEntry> findTreasuryLedger() {
        return repository.findTreasuryLedger();
    }

    public CashierConsole findCashierConsole(Long userId) {
        OperationalDay currentDay = findCurrentOperationalDay();
        CashierSession cashier = repository.findCurrentCashierSession(userId).orElse(null);
        OperationalAssignment assignment = null;
        List<TreasuryMovement> treasuryFunds = List.of();
        List<TreasuryCardMovement> treasuryCards = List.of();
        if (currentDay != null) {
            assignment = repository.findAssignments(currentDay.fechaJornada()).stream()
                    .filter(item -> item.usuarioId().equals(userId) && "CAJERO".equals(item.rolOperativo()))
                    .findFirst()
                    .orElse(null);
            if (assignment != null) {
                treasuryFunds = repository.findTreasuryMovementsForCashier(
                        assignment.estacionId(),
                        assignment.turnoId(),
                        currentDay.id()
                );
                treasuryCards = repository.findTreasuryCardMovementsForCashier(
                        assignment.estacionId(),
                        assignment.turnoId(),
                        currentDay.id()
                );
            }
        }
        List<CashierMovement> cashierMovements = cashier == null
                ? List.of()
                : repository.findCashierMovements(cashier.id());
        List<CashierMovement> cardMovements = cashier == null
                ? List.of()
                : repository.findCashierMovements(null);
        List<CustomerRegistration> customers = cashier == null
                ? List.of()
                : repository.findCashierCustomerRegistrations(cashier.id());
        return new CashierConsole(currentDay, assignment, cashier, cashierMovements, cardMovements, treasuryFunds, treasuryCards, customers);
    }

    @Transactional
    public OperationalDay openOperationalDay(OperationalDayOpenRequest request, Long userId, String ipOrigen) {
        LocalDate fechaJornada = request.fechaJornada() == null ? LocalDate.now() : request.fechaJornada();
        repository.findOpenOperationalDay().ifPresent(openDay -> {
            throw new BadRequestException("Ya existe una jornada abierta para " + openDay.fechaJornada());
        });
        repository.findOperationalDayByDate(fechaJornada).ifPresent(existingDay -> {
            throw new BadRequestException("La jornada de " + fechaJornada + " ya fue registrada con estado "
                    + existingDay.estado());
        });

        Long id = repository.openOperationalDay(fechaJornada, request.observaciones(), userId);
        captureEgmSnapshots(id, "APERTURA", userId);
        audit("REALIZAR_APERTURA", "JORNADA_OPERATIVA", id,
                "Apertura de jornada " + fechaJornada, userId, ipOrigen);
        audit("SNAPSHOT_APERTURA_EGM", "JORNADA_OPERATIVA", id,
                "Snapshots reales de apertura EGM generados", userId, ipOrigen);
        log.info("Jornada operativa abierta id={} fecha={} userId={}", id, fechaJornada, userId);
        return repository.findOperationalDayByDate(fechaJornada).orElseThrow();
    }

    @Transactional
    public OperationalCloseSummary closeOperationalDay(OperationalCloseRequest request, Long userId, String ipOrigen) {
        OperationalDay current = requireOpenOperationalDay();
        int openCashiers = repository.countOpenCashierSessions(current.id());
        if (openCashiers > 0) {
            throw new BadRequestException("No se puede cerrar operaciones: hay cajas abiertas o en precierre");
        }
        int openTreasury = repository.countOpenTreasurySessions(current.id());
        if (openTreasury > 0) {
            throw new BadRequestException("No se puede cerrar operaciones: tesoreria sigue abierta o en precierre");
        }
        int activeCaptures = repository.countActiveEgmCapturesForDay(current.id());
        if (activeCaptures > 0) {
            throw new BadRequestException("No se puede cerrar operaciones: hay tarjetas capturadas por EGM");
        }

        List<EgmMachine> egms = repository.findActiveEgms();
        if (egms.isEmpty()) {
            throw new BadRequestException("No hay EGMs activas configuradas para el barrido de cierre");
        }
        int invalidOpeningSnapshots = repository.countInvalidOpeningSnapshots(current.id());
        if (invalidOpeningSnapshots > 0) {
            throw new BadRequestException("No se puede cerrar operaciones: hay EGMs sin snapshot real de apertura");
        }

        Long closeId = repository.createOperationalClose(
                current.id(),
                request == null ? null : request.observaciones(),
                userId
        );
        for (EgmMachine egm : egms) {
            captureEgmSnapshot(current.id(), egm, "CIERRE", userId);
            repository.reconcileEgm(current.id(), egm);
        }
        boolean force = request != null && Boolean.TRUE.equals(request.forzarConDiferencias());
        repository.completeOperationalClose(current.id(), closeId, force, userId);
        audit("CIERRE_OPERACIONES", "JORNADA_OPERATIVA", current.id(),
                "Barrido EGM y conciliacion de cierre completados", userId, ipOrigen);
        log.info("Cierre de operaciones jornada={} egms={} userId={}", current.id(), egms.size(), userId);
        return findOperationalCloseSummary();
    }

    public List<EgmMeterSnapshot> testEgmSweep(Long userId, String ipOrigen) {
        OperationalDay current = requireOpenOperationalDay();
        List<EgmMachine> egms = repository.findActiveEgms();
        if (egms.isEmpty()) {
            throw new BadRequestException("No hay EGMs activas configuradas para el barrido");
        }
        for (EgmMachine egm : egms) {
            captureEgmSnapshot(current.id(), egm, "MANUAL", userId);
        }
        audit("PRUEBA_BARRIDO_EGM", "JORNADA_OPERATIVA", current.id(),
                "Prueba manual de barrido real EGM", userId, ipOrigen);
        return repository.findEgmSnapshots(current.id());
    }

    public OperationalCloseSummary findOperationalCloseSummary() {
        return repository.findLatestOperationalCloseSummary().orElse(null);
    }

    private void captureEgmSnapshots(Long jornadaId, String snapshotType, Long userId) {
        List<EgmMachine> egms = repository.findActiveEgms();
        for (EgmMachine egm : egms) {
            captureEgmSnapshot(jornadaId, egm, snapshotType, userId);
        }
    }

    private void captureEgmSnapshot(Long jornadaId, EgmMachine egm, String snapshotType, Long userId) {
        try {
            EgmMetersResponse response = egmSweepClient.readMeters(egm, snapshotType);
            repository.createSasMeterSnapshot(jornadaId, egm, snapshotType, response, userId);
        } catch (Exception ex) {
            String message = "Fallo al leer meters SAS EGM " + egm.egmAddr() + ": " + ex.getMessage();
            repository.createSnapshotFailure(jornadaId, egm, snapshotType, message, userId);
            log.warn("Barrido EGM fallo jornada={} egm={} tipo={} error={}",
                    jornadaId, egm.egmAddr(), snapshotType, ex.getMessage());
        }
    }

    @Transactional
    public TreasurySession openTreasury(TreasuryOpenRequest request, Long userId, String ipOrigen) {
        OperationalDay currentDay = requireOpenOperationalDay();
        validateTreasuryStation(request.estacionId());
        validateTreasuryAssignment(userId, request.estacionId(), currentDay.fechaJornada());
        repository.findCurrentTreasurySession().ifPresent(open -> {
            throw new BadRequestException("Ya existe una tesoreria abierta para la jornada "
                    + open.fechaJornada());
        });

        captureEgmSnapshots(currentDay.id(), "APERTURA", userId);
        int invalidOpeningSnapshots = repository.countInvalidOpeningSnapshots(currentDay.id());
        if (invalidOpeningSnapshots > 0) {
            throw new BadRequestException("No se puede abrir tesoreria: hay EGMs sin snapshot real de apertura");
        }

        Long id = repository.openTreasurySession(currentDay.id(), request, userId);
        audit("ABRIR_TESORERIA", "TESORERIA_JORNADA", id,
                "Apertura de tesoreria con saldo inicial " + request.saldoInicial()
                        + " y barrido inicial EGM completado", userId, ipOrigen);
        audit("SNAPSHOT_APERTURA_EGM", "TESORERIA_JORNADA", id,
                "Barrido inicial EGM automatico por apertura de tesoreria", userId, ipOrigen);
        log.info("Tesoreria abierta id={} jornada={} userId={}", id, currentDay.id(), userId);
        return repository.findTreasurySessionById(id).orElseThrow();
    }

    @Transactional
    public TreasuryMovement registerTreasuryMovement(TreasuryMovementRequest request, Long userId, String ipOrigen) {
        String tipo = normalizedType(request.tipo());
        validateIn(tipo, List.of("ENTRADA", "SALIDA"), "Tipo de movimiento de tesoreria invalido");
        return registerMoneyMovement(tipo, request, userId, ipOrigen);
    }

    @Transactional
    public TreasuryMovement sendInitialFundToCashier(TreasuryMovementRequest request, Long userId, String ipOrigen) {
        validateCashierStation(request.estacionCajaId());
        TreasuryMovementRequest normalized = new TreasuryMovementRequest(
                "FONDO_CAJA",
                request.concepto() == null || request.concepto().isBlank() ? "Fondo inicial a caja" : request.concepto(),
                request.monto(),
                request.estacionCajaId(),
                request.turnoId(),
                request.referencia(),
                request.observaciones()
        );
        return registerMoneyMovement("FONDO_CAJA", normalized, userId, ipOrigen);
    }

    @Transactional
    public TreasuryMovement receiveCashReturnFromCashier(TreasuryMovementRequest request, Long userId, String ipOrigen) {
        validateCashierStation(request.estacionCajaId());
        TreasuryMovementRequest normalized = new TreasuryMovementRequest(
                "DEVOLUCION_CAJA",
                request.concepto() == null || request.concepto().isBlank() ? "Devolucion de efectivo desde caja" : request.concepto(),
                request.monto(),
                request.estacionCajaId(),
                request.turnoId(),
                request.referencia(),
                request.observaciones()
        );
        return registerMoneyMovement("DEVOLUCION_CAJA", normalized, userId, ipOrigen);
    }

    @Transactional
    public TreasuryCardMovement deliverCardRangeToCashier(
            TreasuryCardMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        return registerCardMovement("ENTREGA_RANGO", request, userId, ipOrigen);
    }

    @Transactional
    public TreasuryCardMovement receiveUnusedCardsFromCashier(
            TreasuryCardMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        return registerCardMovement("DEVOLUCION_TARJETAS", request, userId, ipOrigen);
    }

    @Transactional
    public TreasurySession precloseTreasury(TreasuryCloseRequest request, Long userId, String ipOrigen) {
        TreasurySession current = requireCurrentTreasurySession();
        String observations = request == null ? null : request.observaciones();
        if (!"ABIERTA".equals(current.estado())) {
            throw new BadRequestException("La tesoreria debe estar abierta para precierre");
        }
        repository.precloseTreasurySession(current.id(), observations, userId);
        audit("PRECIERRE_TESORERIA", "TESORERIA_JORNADA", current.id(), observations, userId, ipOrigen);
        log.info("Precierre de tesoreria id={} userId={}", current.id(), userId);
        return repository.findTreasurySessionById(current.id()).orElseThrow();
    }

    @Transactional
    public TreasurySession closeTreasury(TreasuryCloseRequest request, Long userId, String ipOrigen) {
        TreasurySession current = requireCurrentTreasurySession();
        String observations = request == null ? null : request.observaciones();
        if (!List.of("ABIERTA", "PRECIERRE").contains(current.estado())) {
            throw new BadRequestException("La tesoreria no esta disponible para cierre");
        }
        repository.closeTreasurySession(current.id(), observations, userId);
        audit("CIERRE_TESORERIA", "TESORERIA_JORNADA", current.id(), observations, userId, ipOrigen);
        closeOperationalDay(new OperationalCloseRequest(
                observations == null || observations.isBlank()
                        ? "Cierre operativo automatico por cierre de tesoreria"
                        : observations,
                false
        ), userId, ipOrigen);
        log.info("Cierre de tesoreria id={} userId={}", current.id(), userId);
        return repository.findTreasurySessionById(current.id()).orElseThrow();
    }

    @Transactional
    public CashierSession openCashier(CashierOpenRequest request, Long userId, String ipOrigen) {
        OperationalDay currentDay = requireOpenOperationalDay();
        validateCashierStation(request.estacionId());
        OperationalAssignment assignment = repository.findActiveAssignment(
                        userId,
                        request.estacionId(),
                        request.turnoId(),
                        currentDay.fechaJornada(),
                        "CAJERO"
                )
                .orElseThrow(() -> new BadRequestException(
                        "El usuario logueado no tiene asignacion CAJERO activa para esta jornada, caja y turno"
                ));
        Long cajaId = repository.findCashierBoxIdForStation(request.estacionId())
                .orElseThrow(() -> new BadRequestException("La estacion no tiene caja contable activa asignada"));
        Optional<CashierSession> openCashier = repository.findOpenCashierSession(
                currentDay.id(),
                cajaId,
                request.turnoId()
        );
        if (openCashier.isPresent()) {
            return openCashier.get();
        }
        validateCashierOpeningAgainstTreasury(currentDay.id(), assignment, request);
        Long id = repository.openCashierSession(currentDay.id(), cajaId, request, userId);
        audit("ABRIR_CAJA", "CAJA_JORNADA", id,
                "Apertura de caja con fondo " + request.montoApertura(), userId, ipOrigen);
        log.info("Caja abierta id={} jornada={} userId={}", id, currentDay.id(), userId);
        return repository.findCashierSessionById(id).orElseThrow();
    }

    @Transactional
    public CashierMovement registerCashierReplenishment(
            CashierMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        return registerCashierMovement("REPOSICION", request, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerCashierReturn(CashierMovementRequest request, Long userId, String ipOrigen) {
        return registerCashierMovement("DEVOLUCION", request, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerCashierSale(CashierMovementRequest request, Long userId, String ipOrigen) {
        validateCardNumber(request.numeroTarjeta());
        validateCardNotCapturedByEgm(request.numeroTarjeta());
        CashierSession cashier = requireCurrentCashierSession(userId);
        if (!"ABIERTA".equals(cashier.estado())) {
            throw new BadRequestException("La caja debe estar abierta para registrar venta");
        }
        validateCashierMovementAmount("VENTA", request.monto());
        Long id = repository.createCashierSale(cashier.id(), cashier.estacionId(), request, userId);
        audit("VENTA", "MOVIMIENTO_CAJA", id,
                "Venta tarjeta " + request.numeroTarjeta() + " $" + request.monto(), userId, ipOrigen);
        log.info("Venta de caja creada id={} tarjeta={} userId={}", id, request.numeroTarjeta(), userId);
        return repository.findCashierMovements(cashier.id()).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public CashierMovement registerCashierPayout(CashierMovementRequest request, Long userId, String ipOrigen) {
        validateCardNumber(request.numeroTarjeta());
        validateCardNotCapturedByEgm(request.numeroTarjeta());
        validateCashierMovementAmount("PAGO", request.monto());
        BigDecimal balance = repository.findCardBalance(request.numeroTarjeta());
        if (request.monto().compareTo(balance) > 0) {
            throw new BadRequestException("El monto solicitado excede el saldo disponible de la tarjeta");
        }
        CashierMovementRequest normalized = new CashierMovementRequest(
                request.tipo(),
                request.numeroTarjeta(),
                request.monto(),
                request.maquina(),
                request.motivo() == null || request.motivo().isBlank() ? "Pago efectivo a cliente" : request.motivo(),
                request.referencia(),
                request.observaciones()
        );
        return registerCashierMovement("PAGO", normalized, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerManualPayment(CashierMovementRequest request, Long userId, String ipOrigen) {
        if (request.maquina() == null || request.maquina().isBlank()) {
            throw new BadRequestException("La maquina es obligatoria para pago manual");
        }
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new BadRequestException("El motivo es obligatorio para pago manual");
        }
        return registerCashierMovement("PAGO_MANUAL", request, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerSpecialTransaction(
            CashierMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new BadRequestException("La descripcion es obligatoria para transaccion especial");
        }
        return registerCashierMovement("TRANSACCION_ESPECIAL", request, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerCourtesy(CashierMovementRequest request, Long userId, String ipOrigen) {
        validateCardNumber(request.numeroTarjeta());
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new BadRequestException("El motivo es obligatorio para cortesia");
        }
        return registerCashierMovement("CORTESIA", request, userId, ipOrigen);
    }

    @Transactional
    public CashierMovement registerPromotional(CashierMovementRequest request, Long userId, String ipOrigen) {
        validateCardNumber(request.numeroTarjeta());
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new BadRequestException("El motivo es obligatorio para promocional");
        }
        return registerCashierMovement("PROMOCIONAL", request, userId, ipOrigen);
    }

    @Transactional
    public CustomerRegistration registerCustomerWithCard(
            CustomerRegistrationRequest request,
            Long userId,
            String ipOrigen
    ) {
        CashierSession cashier = requireCurrentCashierSession(userId);
        if (!"ABIERTA".equals(cashier.estado())) {
            throw new BadRequestException("La caja debe estar abierta para registrar clientes");
        }
        validateCustomerRequest(request);
        repository.findCustomerByDocument(request.documentoIdentidad()).ifPresent(existing -> {
            throw new BadRequestException("Ya existe un cliente con ese documento de identidad");
        });
        OperationalCard card = repository.findCardByNumber(request.numeroTarjeta())
                .orElseThrow(() -> new BadRequestException("La tarjeta no existe en inventario"));
        if (!"CLIENTE".equals(card.tipo())) {
            throw new BadRequestException("Solo se pueden asignar tarjetas tipo CLIENTE");
        }
        if (!"DISPONIBLE".equals(card.estado())) {
            throw new BadRequestException("La tarjeta no esta disponible para asignacion");
        }
        if (card.fechaVencimiento() != null && card.fechaVencimiento().isBefore(LocalDate.now())) {
            throw new BadRequestException("La tarjeta esta vencida");
        }

        Long customerId = repository.createCustomer(request, userId);
        boolean assigned = repository.markCardAssigned(card.id(), userId);
        if (!assigned) {
            throw new BadRequestException("La tarjeta ya no esta disponible");
        }
        Long assignmentId = repository.assignCardToCustomer(
                customerId,
                card.id(),
                cashier.id(),
                request.nip() == null || request.nip().isBlank() ? null : passwordEncoder.encode(request.nip()),
                request.observaciones(),
                userId
        );
        Long movementId = repository.createCashierMovement(
                cashier.id(),
                cashier.estacionId(),
                "ALTA_CLIENTE",
                new CashierMovementRequest(
                        null,
                        request.numeroTarjeta(),
                        BigDecimal.ZERO,
                        null,
                        "Alta de cliente",
                        "CLIENTE-" + customerId,
                        request.observaciones()
                ),
                userId
        );
        audit("ALTA_CLIENTE", "CLIENTE_TARJETA", assignmentId,
                "Cliente " + customerId + " tarjeta " + request.numeroTarjeta(), userId, ipOrigen);
        audit("ALTA_CLIENTE_MOVIMIENTO", "MOVIMIENTO_CAJA", movementId,
                "Registro de cliente desde caja", userId, ipOrigen);
        log.info("Cliente registrado id={} tarjeta={} caja={} userId={}",
                customerId, request.numeroTarjeta(), cashier.id(), userId);
        return repository.findCashierCustomerRegistrations(cashier.id()).stream()
                .filter(item -> item.asignacionId().equals(assignmentId))
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public CashierSession precloseCashier(CashierCloseRequest request, Long userId, String ipOrigen) {
        CashierSession current = requireCurrentCashierSession(userId);
        if (!"ABIERTA".equals(current.estado())) {
            throw new BadRequestException("La caja debe estar abierta para precierre");
        }
        String observations = request == null ? null : request.observaciones();
        repository.precloseCashierSession(current.id(), observations, userId);
        audit("PRECIERRE_CAJA", "CAJA_JORNADA", current.id(), observations, userId, ipOrigen);
        log.info("Precierre de caja id={} userId={}", current.id(), userId);
        return repository.findCashierSessionById(current.id()).orElseThrow();
    }

    @Transactional
    public CashierSession closeCashier(CashierCloseRequest request, Long userId, String ipOrigen) {
        CashierSession current = requireCurrentCashierSession(userId);
        if (!List.of("ABIERTA", "PRECIERRE").contains(current.estado())) {
            throw new BadRequestException("La caja no esta disponible para cierre");
        }
        int activeCaptures = repository.countActiveEgmCapturesForCashier(current.id());
        if (activeCaptures > 0) {
            throw new BadRequestException("No se puede cerrar caja: hay tarjetas capturadas por EGM");
        }
        CashierCloseRequest normalized = request == null
                ? new CashierCloseRequest(current.saldoActual(), current.tarjetasActuales(), null)
                : request;
        repository.closeCashierSession(current.id(), normalized, userId);
        audit("CIERRE_CAJA", "CAJA_JORNADA", current.id(), normalized.observaciones(), userId, ipOrigen);
        log.info("Cierre de caja id={} userId={}", current.id(), userId);
        return repository.findCashierSessionById(current.id()).orElseThrow();
    }

    private TreasuryMovement registerMoneyMovement(
            String tipo,
            TreasuryMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        TreasurySession treasury = requireCurrentTreasurySession();
        if (!"ABIERTA".equals(treasury.estado())) {
            throw new BadRequestException("La tesoreria debe estar abierta para registrar movimientos");
        }
        if (List.of("FONDO_CAJA", "SALIDA").contains(tipo) && treasury.saldoActual().compareTo(request.monto()) < 0) {
            throw new BadRequestException("Saldo insuficiente en tesoreria");
        }
        if (request.concepto() == null || request.concepto().isBlank()) {
            throw new BadRequestException("El concepto del movimiento es obligatorio");
        }
        BigDecimal delta = List.of("FONDO_CAJA", "SALIDA").contains(tipo)
                ? request.monto().negate()
                : request.monto();
        Long id = repository.createTreasuryMovement(treasury.id(), tipo, request, userId);
        repository.applyTreasuryBalance(treasury.id(), delta);
        audit(tipo, "MOVIMIENTO_TESORERIA", id,
                request.concepto() + " $" + request.monto(), userId, ipOrigen);
        log.info("Movimiento de tesoreria creado id={} tipo={} userId={}", id, tipo, userId);
        return repository.findTreasuryMovements(treasury.id()).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private CashierMovement registerCashierMovement(
            String tipo,
            CashierMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        CashierSession cashier = requireCurrentCashierSession(userId);
        if (!"ABIERTA".equals(cashier.estado())) {
            throw new BadRequestException("La caja debe estar abierta para registrar movimientos");
        }
        if (request.numeroTarjeta() != null && !request.numeroTarjeta().isBlank()) {
            validateCardNotCapturedByEgm(request.numeroTarjeta());
        }
        validateCashierMovementAmount(tipo, request.monto());
        BigDecimal delta = cashierDelta(tipo, request.monto());
        if (delta.signum() < 0 && cashier.saldoActual().compareTo(delta.abs()) < 0) {
            throw new BadRequestException("Saldo insuficiente en caja");
        }
        Long id = repository.createCashierMovement(cashier.id(), cashier.estacionId(), tipo, request, userId);
        if (delta.signum() != 0) {
            repository.applyCashierBalance(cashier.id(), delta);
        }
        audit(tipo, "MOVIMIENTO_CAJA", id,
                "Movimiento de caja $" + request.monto(), userId, ipOrigen);
        log.info("Movimiento de caja creado id={} tipo={} userId={}", id, tipo, userId);
        return repository.findCashierMovements(cashier.id()).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private TreasuryCardMovement registerCardMovement(
            String tipo,
            TreasuryCardMovementRequest request,
            Long userId,
            String ipOrigen
    ) {
        TreasurySession treasury = requireCurrentTreasurySession();
        if (!"ABIERTA".equals(treasury.estado())) {
            throw new BadRequestException("La tesoreria debe estar abierta para mover tarjetas");
        }
        validateCashierStation(request.estacionCajaId());
        int quantity = cardRangeQuantity(request.numeroInicial(), request.numeroFinal());
        Long id = repository.createTreasuryCardMovement(treasury.id(), tipo, request, quantity, userId);
        audit(tipo, "MOVIMIENTO_TARJETAS_TESORERIA", id,
                request.numeroInicial() + "-" + request.numeroFinal(), userId, ipOrigen);
        log.info("Movimiento de tarjetas creado id={} tipo={} cantidad={} userId={}", id, tipo, quantity, userId);
        return repository.findTreasuryCardMovements(treasury.id()).stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
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

    private static void validateCustomerRequest(CustomerRegistrationRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new BadRequestException("El nombre del cliente es obligatorio");
        }
        validateCardNumber(request.numeroTarjeta());
        if (request.email() != null && !request.email().isBlank() && !request.email().contains("@")) {
            throw new BadRequestException("El email del cliente no es valido");
        }
    }

    private static void validateIn(String value, List<String> allowed, String message) {
        if (value == null || !allowed.contains(value.trim().toUpperCase())) {
            throw new BadRequestException(message);
        }
    }

    private OperationalDay requireOpenOperationalDay() {
        return repository.findOpenOperationalDay()
                .orElseThrow(() -> new BadRequestException("No hay jornada operativa abierta"));
    }

    private TreasurySession requireCurrentTreasurySession() {
        return repository.findCurrentTreasurySession()
                .orElseThrow(() -> new BadRequestException("No hay tesoreria abierta"));
    }

    private CashierSession requireCurrentCashierSession(Long userId) {
        return repository.findCurrentCashierSession(userId)
                .orElseThrow(() -> new BadRequestException("No hay caja abierta para el usuario logueado"));
    }

    private void validateTreasuryStation(Long stationId) {
        if (stationId == null || !repository.existsActiveWorkstation(stationId, "TESORERIA")) {
            throw new BadRequestException("Selecciona una tesoreria activa");
        }
    }

    private void validateTreasuryAssignment(Long userId, Long stationId, LocalDate fechaJornada) {
        if (!repository.existsActiveAssignment(userId, stationId, fechaJornada, "TESORERO")) {
            throw new BadRequestException("El usuario logueado no tiene asignacion TESORERO activa para esta jornada y tesoreria");
        }
    }

    private void validateCashierStation(Long stationId) {
        if (stationId == null || !repository.existsActiveWorkstation(stationId, "CAJA")) {
            throw new BadRequestException("Selecciona una caja activa");
        }
    }

    private void validateCashierOpeningAgainstTreasury(
            Long jornadaId,
            OperationalAssignment assignment,
            CashierOpenRequest request
    ) {
        BigDecimal netFunds = repository.findTreasuryMovementsForCashier(
                        assignment.estacionId(),
                        assignment.turnoId(),
                        jornadaId
                )
                .stream()
                .map(item -> "DEVOLUCION_CAJA".equals(item.tipo()) ? item.monto().negate() : item.monto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (request.montoApertura().compareTo(netFunds) > 0) {
            throw new BadRequestException("El monto de apertura excede el fondo enviado por tesoreria");
        }
    }

    private void validateCardNotCapturedByEgm(String numeroTarjeta) {
        if (numeroTarjeta != null && !numeroTarjeta.isBlank()
                && repository.existsActiveEgmCapture(numeroTarjeta)) {
            throw new BadRequestException("La tarjeta esta capturada por EGM; debe cobrarse antes de usarla en el sistema");
        }
    }

    private static void validateCashierMovementAmount(String tipo, BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("El monto es obligatorio");
        }
        boolean allowZero = List.of("CORTESIA", "PROMOCIONAL").contains(tipo);
        if (allowZero && amount.signum() < 0) {
            throw new BadRequestException("El monto no puede ser negativo");
        }
        if (!allowZero && amount.signum() <= 0) {
            throw new BadRequestException("El monto debe ser mayor a cero");
        }
    }

    private static BigDecimal cashierDelta(String tipo, BigDecimal amount) {
        return switch (tipo) {
            case "REPOSICION", "VENTA", "TRANSACCION_ESPECIAL" -> amount;
            case "DEVOLUCION", "PAGO", "PAGO_MANUAL" -> amount.negate();
            default -> BigDecimal.ZERO;
        };
    }

    private static void validateCardNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("El numero de tarjeta es obligatorio");
        }
    }

    private static String normalizedType(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static int cardRangeQuantity(String startValue, String endValue) {
        long start = parseCardNumber(startValue);
        long end = parseCardNumber(endValue);
        if (end < start) {
            throw new BadRequestException("El numero final debe ser mayor o igual al inicial");
        }
        long quantity = end - start + 1;
        if (quantity > 10000) {
            throw new BadRequestException("Rango de tarjetas demasiado grande");
        }
        return Math.toIntExact(quantity);
    }

    private static long parseCardNumber(String value) {
        if (value == null || value.isBlank() || !value.trim().matches("\\d+")) {
            throw new BadRequestException("El numero de tarjeta debe ser numerico");
        }
        return Long.parseLong(value.trim());
    }
}
