package mx.com.excalibur.operacion.operational;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record EgmMetersResponse(
        String operationId,
        String egmAddress,
        Map<String, Long> meters,
        String rawFrameHex,
        List<Map<String, Object>> traces,
        OffsetDateTime capturedAt
) {
}
