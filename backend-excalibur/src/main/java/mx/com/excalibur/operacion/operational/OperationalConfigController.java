package mx.com.excalibur.operacion.operational;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import mx.com.excalibur.operacion.auth.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operational-config")
public class OperationalConfigController {

    private final OperationalConfigService service;

    public OperationalConfigController(OperationalConfigService service) {
        this.service = service;
    }

    @GetMapping("/operation-schedules")
    public List<OperationSchedule> findOperationSchedules() {
        return service.findOperationSchedules();
    }

    @PostMapping("/operation-schedules")
    public ResponseEntity<OperationSchedule> createOperationSchedule(
            @Valid @RequestBody OperationScheduleRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        OperationSchedule created = service.createOperationSchedule(request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.created(URI.create("/api/operational-config/operation-schedules/" + created.id()))
                .body(created);
    }

    @PutMapping("/operation-schedules/{id}")
    public ResponseEntity<Void> updateOperationSchedule(
            @PathVariable Long id,
            @Valid @RequestBody OperationScheduleRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.updateOperationSchedule(id, request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/operation-schedules/{id}")
    public ResponseEntity<Void> deleteOperationSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.deleteOperationSchedule(id, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cashier-shifts")
    public List<CashierShift> findCashierShifts() {
        return service.findCashierShifts();
    }

    @PostMapping("/cashier-shifts")
    public ResponseEntity<CashierShift> createCashierShift(
            @Valid @RequestBody CashierShiftRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        CashierShift created = service.createCashierShift(request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.created(URI.create("/api/operational-config/cashier-shifts/" + created.id()))
                .body(created);
    }

    @PutMapping("/cashier-shifts/{id}")
    public ResponseEntity<Void> updateCashierShift(
            @PathVariable Long id,
            @Valid @RequestBody CashierShiftRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.updateCashierShift(id, request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cashier-shifts/{id}")
    public ResponseEntity<Void> deleteCashierShift(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.deleteCashierShift(id, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workstations")
    public List<Workstation> findWorkstations() {
        return service.findWorkstations();
    }

    @PostMapping("/workstations")
    public ResponseEntity<Workstation> createWorkstation(
            @Valid @RequestBody WorkstationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        Workstation created = service.createWorkstation(request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.created(URI.create("/api/operational-config/workstations/" + created.id()))
                .body(created);
    }

    @PutMapping("/workstations/{id}")
    public ResponseEntity<Void> updateWorkstation(
            @PathVariable Long id,
            @Valid @RequestBody WorkstationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.updateWorkstation(id, request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/workstations/{id}")
    public ResponseEntity<Void> deleteWorkstation(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.deleteWorkstation(id, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments")
    public List<OperationalAssignment> findAssignments(@RequestParam(required = false) LocalDate fechaOperacion) {
        return service.findAssignments(fechaOperacion);
    }

    @PostMapping("/assignments")
    public ResponseEntity<OperationalAssignment> createAssignment(
            @Valid @RequestBody OperationalAssignmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        OperationalAssignment created = service.createAssignment(request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.created(URI.create("/api/operational-config/assignments/" + created.id()))
                .body(created);
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<Void> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody OperationalAssignmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.updateAssignment(id, request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.deleteAssignment(id, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public List<OperationalCard> findCards(@RequestParam(required = false) String search) {
        return service.findCards(search);
    }

    @PostMapping("/cards")
    public ResponseEntity<OperationalCard> createCard(
            @Valid @RequestBody OperationalCardRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        OperationalCard created = service.createCard(request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.created(URI.create("/api/operational-config/cards/" + created.id()))
                .body(created);
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<Void> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody OperationalCardRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.updateCard(id, request, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        service.deleteCard(id, currentUser.id(), ip(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cards/range")
    public List<OperationalCard> createCardRange(
            @Valid @RequestBody CardRangeRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest servletRequest
    ) {
        return service.createCardRange(request, currentUser.id(), ip(servletRequest));
    }

    @GetMapping("/audit")
    public List<OperationalAuditEvent> findAuditEvents(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) Long entidadId
    ) {
        return service.findAuditEvents(entidad, entidadId);
    }

    private static String ip(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
