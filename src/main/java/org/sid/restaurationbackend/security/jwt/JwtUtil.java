package org.sid.restaurationbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.sid.restaurationbackend.enums.InterfaceType;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    private SecretKey getKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(
            Long employeeId,
            String email,
            String role,
            InterfaceType interfaceType,
            List<String> permissions) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getExpiration()
        );

        return Jwts.builder()
                .subject(email)

                .claim("employeeId", employeeId)
                .claim("role", role)
                .claim("interfaceType", interfaceType.name())
                .claim("permissions", permissions)

                .issuedAt(now)
                .expiration(expiration)

                .signWith(getKey(), Jwts.SIG.HS256)

                .compact();
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }


    public Long extractEmployeeId(String token) {

        Number employeeId = extractClaims(token)
                .get("employeeId", Number.class);

        return employeeId != null
                ? employeeId.longValue()
                : null;
    }


    public Long extractRestaurantId(String token) {

        Number restaurantId = extractClaims(token)
                .get("restaurantId", Number.class);

        return restaurantId != null
                ? restaurantId.longValue()
                : null;
    }


    public String extractRole(String token) {

        return extractClaims(token)
                .get("role", String.class);
    }


    public InterfaceType extractInterfaceType(String token) {

        String interfaceType = extractClaims(token)
                .get("interfaceType", String.class);

        if (interfaceType == null) {
            return null;
        }

        return InterfaceType.valueOf(interfaceType);
    }

    public List<String> extractPermissions(String token) {

        Object permissions = extractClaims(token)
                .get("permissions");

        if (permissions instanceof List<?> list) {

            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }


    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractClaims(token);

            Date expiration = claims.getExpiration();

            return expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }

    }


    public String generateSuperAdminToken(
            Long superAdminId,
            String email) {

        return generateSuperAdminToken(
                superAdminId,
                email,
                null,
                InterfaceType.SUPERADMIN,
                List.of()
        );
    }


    public String generateSuperAdminToken(
            Long superAdminId,
            String email,
            Long restaurantId,
            InterfaceType interfaceType,
            List<String> permissions) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getExpiration()
        );

        var builder = Jwts.builder()
                .subject(email)
                .claim("superAdminId", superAdminId)
                .claim("role", "SUPERADMIN")
                .claim("interfaceType", interfaceType.name())
                .claim("permissions", permissions == null ? List.of() : permissions)
                .issuedAt(now)
                .expiration(expiration);

        if (restaurantId != null) {
            builder.claim("restaurantId", restaurantId);
        }

        return builder
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }


    public String generateClientToken(
            Long clientId,
            String email) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime()
                        + jwtProperties.getExpiration()
        );

        return Jwts.builder()
                .subject(email)

                .claim("clientId", clientId)
                .claim("role", "CLIENT")
                .claim(
                        "interfaceType",
                        InterfaceType.CLIENT.name()
                )
                .claim(
                        "permissions",
                        List.of()
                )

                .issuedAt(now)
                .expiration(expiration)

                .signWith(
                        getKey(),
                        Jwts.SIG.HS256
                )

                .compact();
    }


}