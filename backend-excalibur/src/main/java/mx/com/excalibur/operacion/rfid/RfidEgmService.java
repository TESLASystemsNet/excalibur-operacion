package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import mx.com.excalibur.operacion.common.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RfidEgmService {

    private static final BigDecimal DENOMINATION = new BigDecimal("0.10");

    private final RfidEgmRepository repository;
    private final RestClient egmBackendClient;

    public RfidEgmService(RfidEgmRepository repository,
                          RestClient.Builder restClientBuilder,
                          @Value("${excalibur.egm-backend.base-url}") String egmBackendBaseUrl) {
        this.repository = repository;
        this.egmBackendClient = restClientBuilder.baseUrl(egmBackendBaseUrl).build();
    }

    public RfidBalanceResponse checkBalance(RfidBalanceRequest request) {
        String uid = normalizeUid(request.uid());
        if (repository.findActiveCapture(uid).isPresent()) {
            return new RfidBalanceResponse(false, uid, "", BigDecimal.ZERO, true,
                    "Tarjeta con captura activa pendiente de cobro");
        }
        RfidRuntimeCard card = repository.findRuntimeCard(uid)
                .orElse(null);
        if (card == null) {
            return new RfidBalanceResponse(false, uid, "", BigDecimal.ZERO, false,
                    "Tarjeta no encontrada o sin cliente activo");
        }
        String rejection = validateCardForUse(card);
        if (rejection != null) {
            return new RfidBalanceResponse(false, uid, card.clienteNombre(), card.saldo(), false, rejection);
        }
        if (card.saldo().compareTo(BigDecimal.ZERO) <= 0) {
            return new RfidBalanceResponse(true, uid, card.clienteNombre(), BigDecimal.ZERO, false, "Sin saldo disponible");
        }
        return new RfidBalanceResponse(true, uid, card.clienteNombre(), money(card.saldo()), false, "Saldo disponible");
    }

    @Transactional
    public RfidLoadAuthorizationResponse authorizeLoad(RfidLoadRequest request) {
        String uid = normalizeUid(request.uid());
        repository.findActiveCapture(uid).ifPresent(capture -> {
            throw new BadRequestException("La tarjeta ya tiene captura activa pendiente de cobro");
        });
        RfidRuntimeCard card = findRuntimeCard(uid);
        String rejection = validateCardForUse(card);
        if (rejection != null) {
            throw new BadRequestException(rejection);
        }
        if (card.saldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La tarjeta no tiene saldo disponible");
        }
        int credits = amountToCredits(card.saldo());
        String txid = "RFID-" + UUID.randomUUID();
        Long captureId = repository.createCapture(txid, uid, request.egmAddr(), card, credits);
        repository.audit("RFID_SOLICITAR_CARGA", "CAPTURA_RFID_EGM", captureId,
                "Tarjeta " + uid + " saldo " + money(card.saldo()) + " creditos " + credits,
                card.registradoPor());
        return new RfidLoadAuthorizationResponse(true, txid, uid, money(card.saldo()), credits,
                "AUTORIZADA", "Carga autorizada");
    }

    @Transactional
    public RfidLoadConfirmationResponse confirmLoad(RfidLoadConfirmationRequest request) {
        String uid = normalizeUid(request.uid());
        RfidCapture capture = repository.findActiveCapture(uid)
                .filter(item -> item.txid().equals(request.txid()))
                .orElseThrow(() -> new BadRequestException("No hay captura activa para confirmar"));
        boolean loadApplied = isLoadApplied(request.resultado());
        String captureStatus = loadApplied ? "CONFIRMADA" : "CANCELADA";
        repository.finishCaptureLoad(request.txid(), captureStatus, request.resultado(), request.mensaje());
        BigDecimal balance = repository.findRuntimeCard(uid)
                .map(RfidRuntimeCard::saldo)
                .orElse(BigDecimal.ZERO);
        repository.audit("RFID_CONFIRMAR_CARGA", "CAPTURA_RFID_EGM", capture.id(),
                "Resultado " + request.resultado() + " estado " + captureStatus, null);
        return new RfidLoadConfirmationResponse(loadApplied, request.txid(), uid, captureStatus,
                money(balance), loadApplied ? "Carga confirmada" : "Carga cancelada por fallo EGM");
    }

    public RfidActiveCaptureResponse findActiveCapture(String uidValue) {
        String uid = normalizeUid(uidValue);
        return repository.findActiveCapture(uid)
                .map(capture -> new RfidActiveCaptureResponse(true, capture.uid(), capture.txid(),
                        capture.creditos(), capture.estado(), "Captura activa"))
                .orElseGet(() -> new RfidActiveCaptureResponse(false, uid, "", 0, "SIN_CAPTURA",
                        "Sin captura activa"));
    }

    public RfidEgmLoadResponse loadEgm(RfidEgmLoadRequest request) {
        RfidLoadAuthorizationResponse authorization = authorizeLoad(
                new RfidLoadRequest(request.uid(), request.egmAddr(), request.requestOrigen())
        );
        EgmLoadCreditResponse egmResult;
        try {
            egmResult = egmBackendClient.post()
                    .uri("/api/egms/{egmAddr}/aft/load-credit", request.egmAddr())
                    .body(new EgmLoadCreditRequest(
                            authorization.txid(),
                            authorization.creditos(),
                            DENOMINATION,
                            resolveSource(request.requestOrigen()),
                            authorization.uid(),
                            "RFID_BALANCE_LOAD"
                    ))
                    .retrieve()
                    .body(EgmLoadCreditResponse.class);
        } catch (RestClientException ex) {
            egmResult = EgmLoadCreditResponse.failed(authorization.txid(), request.egmAddr(), ex.getMessage());
        }

        if (egmResult == null) {
            egmResult = EgmLoadCreditResponse.failed(authorization.txid(), request.egmAddr(), "Respuesta EGM vacia");
        }
        RfidLoadConfirmationResponse confirmation = confirmLoad(new RfidLoadConfirmationRequest(
                authorization.txid(),
                authorization.uid(),
                egmResult.status(),
                isLoadApplied(egmResult.status()) ? authorization.saldoAutorizado() : BigDecimal.ZERO,
                authorization.creditos(),
                egmResult.message()
        ));
        return new RfidEgmLoadResponse(
                confirmation.success(),
                authorization.uid(),
                authorization.txid(),
                egmResult.operationId(),
                request.egmAddr(),
                confirmation.estado(),
                egmResult.status(),
                authorization.saldoAutorizado(),
                confirmation.saldoDisponible(),
                authorization.creditos(),
                egmResult.creditsBefore(),
                egmResult.creditsAfter(),
                egmResult.deltaCredits(),
                egmResult.txId(),
                confirmation.mensaje()
        );
    }

    public RfidEgmCashoutResponse cashoutEgm(RfidEgmCashoutRequest request) {
        String uid = normalizeUid(request.uid());
        RfidCapture capture = repository.findActiveCapture(uid)
                .orElseThrow(() -> new BadRequestException("No hay captura activa para cobrar"));
        String egmAddr = resolveEgmAddr(request.egmAddr(), capture.egmAddr());
        EgmCashoutResponse egmResult;
        try {
            egmResult = egmBackendClient.post()
                    .uri("/api/egms/{egmAddr}/aft/cashout-to-host", egmAddr)
                    .body(new EgmCashoutRequest(
                            "CO-" + capture.txid(),
                            (long) capture.creditos(),
                            DENOMINATION,
                            resolveSource(request.requestOrigen()),
                            uid,
                            "RFID_BALANCE_CASHOUT"
                    ))
                    .retrieve()
                    .body(EgmCashoutResponse.class);
        } catch (RestClientException ex) {
            egmResult = EgmCashoutResponse.failed("CO-" + capture.txid(), egmAddr, ex.getMessage());
        }

        if (egmResult == null) {
            egmResult = EgmCashoutResponse.failed("CO-" + capture.txid(), egmAddr, "Respuesta EGM vacia");
        }
        if (!isLoadApplied(egmResult.status())) {
            return new RfidEgmCashoutResponse(false, uid, capture.txid(), egmResult.operationId(), egmAddr,
                    capture.estado(), egmResult.status(), BigDecimal.ZERO, BigDecimal.ZERO, 0,
                    egmResult.creditsBefore(), egmResult.creditsAfter(), egmResult.txId(), egmResult.message());
        }

        BigDecimal returnedAmount = egmResult.returnedAmount() == null
                ? creditsToAmount(egmResult.returnedCredits())
                : money(egmResult.returnedAmount());
        RfidBalanceResponse balance = returnBalance(new RfidCashoutReturnRequest(
                uid,
                capture.txid(),
                returnedAmount,
                egmResult.returnedCredits() == null ? null : egmResult.returnedCredits().intValue(),
                egmResult.message()
        ));
        return new RfidEgmCashoutResponse(true, uid, capture.txid(), egmResult.operationId(), egmAddr,
                "CERRADA", egmResult.status(), returnedAmount, balance.saldo(),
                egmResult.returnedCredits() == null ? null : egmResult.returnedCredits().intValue(),
                egmResult.creditsBefore(), egmResult.creditsAfter(), egmResult.txId(), balance.mensaje());
    }

    @Transactional
    public RfidBalanceResponse returnBalance(RfidCashoutReturnRequest request) {
        String uid = normalizeUid(request.uid());
        RfidCapture capture = repository.findActiveCapture(uid)
                .filter(item -> item.txid().equals(request.txid()))
                .orElseThrow(() -> new BadRequestException("No hay captura activa para cobrar"));
        BigDecimal returnedAmount = request.montoDevuelto() == null ? BigDecimal.ZERO : money(request.montoDevuelto());
        if (returnedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El monto devuelto no puede ser negativo");
        }
        BigDecimal settlementDelta = money(returnedAmount.subtract(capture.saldoAutorizado()));
        RfidRuntimeCard card = findRuntimeCard(uid);
        if (card.registradoPor() == null) {
            throw new BadRequestException("La captura no tiene cajero registrado");
        }
        Long paymentMovementId = repository.createSettlementMovement(capture, "PAGO", returnedAmount, settlementDelta,
                request.creditosDevueltos(),
                "Cobro RFID desde EGM",
                "capturado=" + capture.saldoAutorizado() + " devuelto=" + returnedAmount + " "
                        + (request.mensaje() == null ? "" : request.mensaje()),
                card.registradoPor());
        if (settlementDelta.signum() != 0) {
            repository.applyCashierDelta(capture.cajaJornadaId(), settlementDelta);
        }
        repository.closeCapture(request.txid(), request.mensaje());
        BigDecimal balance = repository.findRuntimeCard(uid)
                .map(RfidRuntimeCard::saldo)
                .orElse(BigDecimal.ZERO);
        repository.audit("RFID_COBRO", "MOVIMIENTO_CAJA", paymentMovementId,
                "Cobro RFID tarjeta " + uid + " capturado " + capture.saldoAutorizado()
                        + " devuelto " + returnedAmount + " ajuste " + settlementDelta
                        + " pagoMovimiento=" + paymentMovementId,
                card.registradoPor());
        return new RfidBalanceResponse(true, uid, card.clienteNombre(), money(balance), false,
                "Saldo devuelto");
    }

    private RfidRuntimeCard findRuntimeCard(String uid) {
        return repository.findRuntimeCard(uid)
                .orElseThrow(() -> new BadRequestException("Tarjeta no encontrada o sin cliente activo"));
    }

    private static String validateCardForUse(RfidRuntimeCard card) {
        if (!"ASIGNADA".equals(card.tarjetaEstado())) {
            return "La tarjeta no esta asignada";
        }
        if (card.cajaJornadaId() == null || !"ABIERTA".equals(card.cajaEstado())) {
            return "No hay caja abierta para esta tarjeta";
        }
        return null;
    }

    private static int amountToCredits(BigDecimal amount) {
        return amount.divide(DENOMINATION, 0, RoundingMode.HALF_UP).intValueExact();
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal creditsToAmount(Long credits) {
        if (credits == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return money(DENOMINATION.multiply(BigDecimal.valueOf(credits)));
    }

    private static boolean isLoadApplied(String result) {
        if (result == null || result.isBlank()) {
            return false;
        }
        return switch (result.trim().toUpperCase()) {
            case "SUCCESS", "VALIDATED_BY_BALANCE", "LOADED_BY_PROTOCOL", "LOADED_NOT_VALIDATED", "NOT_VALIDATED" -> true;
            default -> false;
        };
    }

    private static String normalizeUid(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new BadRequestException("El UID es obligatorio");
        }
        return uid.trim();
    }

    private static String resolveSource(String requestOrigen) {
        return requestOrigen == null || requestOrigen.isBlank() ? "RFID_READER" : requestOrigen.trim();
    }

    private static String resolveEgmAddr(String requested, String captured) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        if (captured != null && !captured.isBlank()) {
            return captured.trim();
        }
        throw new BadRequestException("La EGM es obligatoria");
    }

    private record EgmLoadCreditRequest(
            String operationId,
            long credits,
            BigDecimal denomination,
            String source,
            String cardUid,
            String reason
    ) {
    }

    private record EgmCashoutRequest(
            String operationId,
            Long requestedCredits,
            BigDecimal denomination,
            String source,
            String cardUid,
            String reason
    ) {
    }

    private record EgmLoadCreditResponse(
            String operationId,
            String egmAddress,
            String status,
            long requestedCredits,
            Long creditsBefore,
            Long creditsAfter,
            Long deltaCredits,
            BigDecimal denomination,
            BigDecimal requestedAmount,
            String txId,
            String message
    ) {
        static EgmLoadCreditResponse failed(String operationId, String egmAddress, String message) {
            return new EgmLoadCreditResponse(operationId, egmAddress, "FAILED", 0, null, null, null,
                    DENOMINATION, BigDecimal.ZERO, null, message);
        }
    }

    private record EgmCashoutResponse(
            String operationId,
            String egmAddress,
            String status,
            Long requestedCredits,
            Long creditsBefore,
            Long creditsAfter,
            Long returnedCredits,
            BigDecimal denomination,
            BigDecimal returnedAmount,
            String txId,
            String message
    ) {
        static EgmCashoutResponse failed(String operationId, String egmAddress, String message) {
            return new EgmCashoutResponse(operationId, egmAddress, "FAILED", null, null, null, null,
                    DENOMINATION, BigDecimal.ZERO, null, message);
        }
    }
}
