package com.induztek.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutos;

    public JwtService(@Value("${jwt.secreto}") String secreto,
                      @Value("${jwt.minutos}") long minutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutos = minutos;
    }

    public String crearToken(Long usuarioId, Long rolId) {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + minutos * 60 * 1000);

        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("rol", rolId)
                .issuedAt(ahora)
                .expiration(vence)
                .signWith(clave)
                .compact();
    }
}