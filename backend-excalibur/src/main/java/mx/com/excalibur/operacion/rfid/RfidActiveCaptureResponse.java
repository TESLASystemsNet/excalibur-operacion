package mx.com.excalibur.operacion.rfid;

public record RfidActiveCaptureResponse(
        boolean success,
        String uid,
        String txid,
        int creditos,
        String estado,
        String mensaje
) {
}
