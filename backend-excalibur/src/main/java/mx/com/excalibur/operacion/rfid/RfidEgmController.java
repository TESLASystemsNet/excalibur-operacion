package mx.com.excalibur.operacion.rfid;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfid")
public class RfidEgmController {

    private final RfidEgmService service;

    public RfidEgmController(RfidEgmService service) {
        this.service = service;
    }

    @PostMapping("/consultar-saldo")
    public RfidBalanceResponse checkBalance(@Valid @RequestBody RfidBalanceRequest request) {
        return service.checkBalance(request);
    }

    @PostMapping("/solicitar-carga")
    public RfidLoadAuthorizationResponse authorizeLoad(@Valid @RequestBody RfidLoadRequest request) {
        return service.authorizeLoad(request);
    }

    @PostMapping("/confirmar-carga")
    public RfidLoadConfirmationResponse confirmLoad(@Valid @RequestBody RfidLoadConfirmationRequest request) {
        return service.confirmLoad(request);
    }

    @PostMapping("/cargar-egm")
    public RfidEgmLoadResponse loadEgm(@Valid @RequestBody RfidEgmLoadRequest request) {
        return service.loadEgm(request);
    }

    @GetMapping("/captura-activa/{uid}")
    public RfidActiveCaptureResponse activeCapture(@PathVariable String uid) {
        return service.findActiveCapture(uid);
    }

    @PostMapping("/devolver-saldo")
    public RfidBalanceResponse returnBalance(@Valid @RequestBody RfidCashoutReturnRequest request) {
        return service.returnBalance(request);
    }

    @PostMapping("/cobrar-egm")
    public RfidEgmCashoutResponse cashoutEgm(@Valid @RequestBody RfidEgmCashoutRequest request) {
        return service.cashoutEgm(request);
    }
}
