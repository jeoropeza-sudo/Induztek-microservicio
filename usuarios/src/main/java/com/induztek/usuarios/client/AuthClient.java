package com.induztek.usuarios.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(@Value("${auth.service.url:http://localhost:8085}") String authUrl) {
        this.webClient = WebClient.builder().baseUrl(authUrl).build();
    }

    public RespuestaAltaCredencial crearCredencialTemporal(Long usuarioId, String correo) {
        try {
            return webClient.post()
                    .uri("/api/auth/credenciales/alta")
                    .bodyValue(new DatosCredencial(usuarioId, correo, null))
                    .retrieve()
                    .bodyToMono(RespuestaAltaCredencial.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de autenticación no está disponible"
            );
        }
    }

    public void eliminarCredencial(Long usuarioId) {
        try {
            webClient.delete()
                    .uri("/api/auth/credenciales/usuario/{usuarioId}", usuarioId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientRequestException e) {
            // Compensación best-effort; la cuenta se elimina localmente.
        }
    }

    public record DatosCredencial(Long usuarioId, String correo, String contrasena) {
    }

    public record RespuestaAltaCredencial(String contrasenaTemporal) {
    }
}
