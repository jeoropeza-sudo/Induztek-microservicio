package com.induztek.equipos.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class ServiceJwtProvider {

    private static final long TOKEN_LIFETIME_MILLIS = 2 * 60 * 1000L;

    private final SecretKey secretKey;
    private final String serviceName;

    public ServiceJwtProvider(
            @Value("${jwt.secreto}") String secret,
            @Value("${spring.application.name}") String serviceName
    ) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.serviceName = serviceName;
    }

    public ExchangeFilterFunction authorizationFilter() {
        return (request, next) -> {
            ClientRequest authorizedRequest = ClientRequest.from(request)
                    .headers(headers -> headers.setBearerAuth(createToken()))
                    .build();
            return next.exchange(authorizedRequest);
        };
    }

    private String createToken() {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + TOKEN_LIFETIME_MILLIS);
        return Jwts.builder()
                .issuer(serviceName)
                .claim("service", serviceName)
                .claim("rol", 4L)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }
}
