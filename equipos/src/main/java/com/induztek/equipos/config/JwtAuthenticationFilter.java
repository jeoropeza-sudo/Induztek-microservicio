package com.induztek.equipos.config;

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;
    private final String internalToken;

    public JwtAuthenticationFilter(
            @Value("${jwt.secreto}") String secret,
            @Value("${security.internal-token:}") String internalToken
    ) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.internalToken = internalToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String internalHeader = request.getHeader("X-Internal-Token");
        if (!internalToken.isBlank() && internalToken.equals(internalHeader)) {
            if (!establecerActorInterno(request, response)) {
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(header.substring(7).trim()).getPayload();
            String subject = claims.getSubject();
            Number roleClaim = claims.get("rol", Number.class);
            if (subject == null || roleClaim == null) {
                responder(response, HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            Long roleId = roleClaim.longValue();
            if (Boolean.TRUE.equals(claims.get("primerIngreso", Boolean.class))
                    || !autorizado(request.getMethod(), request.getServletPath(), roleId)) {
                responder(response, HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("actorId", Long.valueOf(subject));
            request.setAttribute("actorRoleId", roleId);
            chain.doFilter(request, response);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean autorizado(String method, String path, Long roleId) {
        if (Long.valueOf(3L).equals(roleId) || Long.valueOf(4L).equals(roleId)) {
            return true;
        }
        if (Long.valueOf(2L).equals(roleId)) {
            return HttpMethod.GET.matches(method);
        }
        if (Long.valueOf(1L).equals(roleId)) {
            if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method)) {
                return true;
            }
            if (path.startsWith("/api/visitas")) {
                return !HttpMethod.DELETE.matches(method)
                        || path.matches("^/api/visitas/\\d+/equipos/\\d+$");
            }
            return false;
        }
        return false;
    }

    private void responder(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Acceso no autorizado\",\"status\":" + status + "}");
    }

    private boolean establecerActorInterno(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String actorId = request.getHeader("X-User-Id");
        String roleId = request.getHeader("X-User-Role");
        if (actorId == null && roleId == null) {
            request.setAttribute("actorRoleId", 4L);
            return true;
        }
        if (actorId == null || roleId == null) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        try {
            Long id = Long.valueOf(actorId);
            Long rol = Long.valueOf(roleId);
            if (id <= 0 || rol < 1 || rol > 4) {
                responder(response, HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            request.setAttribute("actorId", id);
            request.setAttribute("actorRoleId", rol);
            return true;
        } catch (NumberFormatException e) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
