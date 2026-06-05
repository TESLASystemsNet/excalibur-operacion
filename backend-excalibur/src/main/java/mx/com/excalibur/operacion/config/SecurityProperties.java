package mx.com.excalibur.operacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "excalibur.security")
public record SecurityProperties(
        String jwtSecret,
        long jwtExpirationMinutes
) {
}
