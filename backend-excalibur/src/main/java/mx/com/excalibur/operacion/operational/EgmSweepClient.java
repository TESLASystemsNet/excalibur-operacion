package mx.com.excalibur.operacion.operational;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class EgmSweepClient {

    private final RestClient.Builder restClientBuilder;
    private final String defaultBaseUrl;

    public EgmSweepClient(
            RestClient.Builder restClientBuilder,
            @Value("${excalibur.egm-backend.base-url}") String defaultBaseUrl
    ) {
        this.restClientBuilder = restClientBuilder;
        this.defaultBaseUrl = trimTrailingSlash(defaultBaseUrl);
    }

    public EgmMetersResponse readMeters(EgmMachine egm, String snapshotType) {
        String baseUrl = trimTrailingSlash(
                egm.raspberryBaseUrl() == null || egm.raspberryBaseUrl().isBlank()
                        ? defaultBaseUrl
                        : egm.raspberryBaseUrl()
        );
        String operationId = "EXC-" + snapshotType + "-" + egm.egmAddr() + "-" + UUID.randomUUID();
        RestClient client = restClientBuilder.baseUrl(baseUrl).build();
        try {
            return readMeters(client, "/api/egms/{egmAddress}/meters", egm.egmAddr(), operationId);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 404) {
                throw ex;
            }
            return readMeters(client, "/api/egm/{egmAddress}/meters", egm.egmAddr(), operationId);
        }
    }

    private EgmMetersResponse readMeters(RestClient client, String path, String egmAddress, String operationId) {
        EgmMetersResponse response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("operationId", operationId)
                        .build(egmAddress))
                .retrieve()
                .body(EgmMetersResponse.class);
        if (response == null) {
            throw new IllegalStateException("Respuesta vacia al leer meters EGM " + egmAddress);
        }
        return response;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
