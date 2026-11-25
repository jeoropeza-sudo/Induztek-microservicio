package com.induztek.usuarios.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/olvide",
            "/api/auth/restablecer"
    );

    private final SecretKey secretKey;
    private static final List<String> SERVICE_NAMES = List.of(
            "auth", "usuarios", "empresas", "equipos", "instrumentos", "protocolos"
    );

    public JwtAuthenticationFilter(
            @Value("${jwt.secreto}") String secret
    ) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        if (PUBLIC_ENDPOINTS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        boolean endpointPrimerIngreso = path.matches("^/api/usuarios/\\d+/primer-ingreso$");
        if (endpointPrimerIngreso
                && (request.getHeader("X-User-Id") != null || request.getHeader("X-User-Role") != null)) {
            responder(response, HttpServletResponse.SC_FORBIDDEN, "Solo el servicio de autenticación puede completar el primer ingreso");
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED, "Falta un JWT válido");
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(header.substring(7).trim())
                    .getPayload();
            String serviceName = claims.get("service", String.class);
            Number roleClaim = claims.get("rol", Number.class);
            if (serviceName != null) {
                if (!SERVICE_NAMES.contains(serviceName)
                        || !serviceName.equals(claims.getIssuer())
                        || roleClaim == null
                        || roleClaim.longValue() != 4L) {
                    responder(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT de servicio inválido");
                    return;
                }
                if (endpointPrimerIngreso && !"auth".equals(serviceName)) {
                    responder(response, HttpServletResponse.SC_FORBIDDEN, "Solo el servicio de autenticación puede completar el primer ingreso");
                    return;
                }
                request.setAttribute("actorRoleId", 4L);
                chain.doFilter(request, response);
                return;
            }
            if (endpointPrimerIngreso) {
                responder(response, HttpServletResponse.SC_FORBIDDEN, "Solo el servicio de autenticación puede completar el primer ingreso");
                return;
            }

            String subject = claims.getSubject();
            if (subject == null || roleClaim == null) {
                responder(response, HttpServletResponse.SC_UNAUTHORIZED, "El JWT no contiene usuario y rol");
                return;
            }

            Long roleId = roleClaim.longValue();
            if (Boolean.TRUE.equals(claims.get("primerIngreso", Boolean.class))
                    && !("/api/auth/configurar".equals(path) && HttpMethod.POST.matches(request.getMethod()))) {
                responder(response, HttpServletResponse.SC_FORBIDDEN, "Debes cambiar la contraseña antes de continuar");
                return;
            }
            if (!autorizado(request.getMethod(), path, roleId)) {
                responder(response, HttpServletResponse.SC_FORBIDDEN, "El rol no puede realizar esta operación");
                return;
            }

            request.setAttribute("actorId", Long.valueOf(subject));
            request.setAttribute("actorRoleId", roleId);
            chain.doFilter(request, response);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT inválido o expirado");
        }
    }

    private boolean autorizado(String method, String path, Long roleId) {
        if (Long.valueOf(4L).equals(roleId) || Long.valueOf(3L).equals(roleId)) {
            return true;
        }
        if (Long.valueOf(2L).equals(roleId)) {
            return HttpMethod.GET.matches(method);
        }
        if (Long.valueOf(1L).equals(roleId)) {
            boolean escritura = !HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method);
            if (escritura && (path.startsWith("/api/usuarios")
                    || path.startsWith("/api/empresas")
                    || path.startsWith("/api/roles")
                    || path.startsWith("/api/instrumentos")
                    || path.startsWith("/api/auth"))) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void responder(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\",\"status\":" + status + "}");
    }
}
