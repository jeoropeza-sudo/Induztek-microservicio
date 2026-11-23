package com.induztek.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/olvide",
            "/api/auth/restablecer"
    );
    private final String internalToken;

    public JwtAuthenticationFilter(
            @Value("${jwt.secreto}") String secreto,
            @Value("${security.internal-token}") String internalToken
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.internalToken = internalToken;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Omitir validación para endpoints públicos y documentación Swagger
        if (isPublicEndpoint(path) || path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return chain.filter(exchange);
        }

        // 2. Extraer cabecera Authorization: Bearer <token>
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Cabecera Authorization faltante o en formato incorrecto", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();

        // 3. Validar el token y obtener Claims
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String usuarioId = claims.getSubject();
            Number rolNumber = claims.get("rol", Number.class);
            if (usuarioId == null || rolNumber == null) {
                return onError(exchange, "Token JWT sin identidad o rol", HttpStatus.UNAUTHORIZED);
            }
            Long rolId = rolNumber.longValue();

            Boolean primerIngreso = claims.get("primerIngreso", Boolean.class);
            if (Boolean.TRUE.equals(primerIngreso)
                    && !("/api/auth/configurar".equals(path) && HttpMethod.POST.equals(request.getMethod()))) {
                return onError(exchange, "Debes cambiar la contraseña antes de continuar", HttpStatus.FORBIDDEN);
            }

            // 4. Validar autorización según Rol
            if (!isAuthorized(request.getMethod(), path, rolId)) {
                return onError(exchange, "Acceso denegado: el rol no tiene permisos para realizar esta acción", HttpStatus.FORBIDDEN);
            }

            // 5. Inyectar encabezados de contexto para microservicios aguas abajo
            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Role");
                        headers.remove("X-Internal-Token");
                    })
                    .header("X-User-Id", usuarioId)
                    .header("X-User-Role", String.valueOf(rolId))
                    .header("X-Internal-Token", internalToken)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Token JWT inválido o expirado", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.contains(path);
    }

    private boolean isAuthorized(HttpMethod method, String path, Long rolId) {
        if ("/api/auth/configurar".equals(path) && HttpMethod.POST.equals(method)) {
            return List.of(1L, 2L, 3L, 4L).contains(rolId);
        }

        // Rol 4 (JEFE_TECNICO): Permiso total
        if (rolId == 4L) {
            return true;
        }

        // Rol 2 (CLIENTE): Solo lectura (GET)
        if (rolId == 2L) {
            return HttpMethod.GET.equals(method);
        }

        // Técnico puede consultar sus recursos y registrar trabajo de terreno.
        if (rolId == 1L) {
            if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
                return true;
            }
            if (path.startsWith("/api/visitas")) {
                return !HttpMethod.DELETE.equals(method)
                        || path.matches("^/api/visitas/\\d+/equipos/\\d+$");
            }
            return path.startsWith("/api/protocolos") && !HttpMethod.DELETE.equals(method);
        }

        // Ingeniería y Jefatura Técnica pueden administrar los recursos.
        if (rolId == 3L) {
            return true;
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -1; // Ejecutar temprano en la cadena de filtros
    }
}
