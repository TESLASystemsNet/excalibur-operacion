package mx.com.excalibur.operacion.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import mx.com.excalibur.operacion.config.SecurityProperties;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecurityProperties properties;
    private final SecretKey secretKey;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.jwtExpirationMinutes() * 60);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.id())
                .claim("roles", user.roles())
                .claim("permisos", user.permisos())
                .claim("requiereCambioPassword", user.requiereCambioPassword())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, String username) {
        Claims claims = claims(token);
        return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
