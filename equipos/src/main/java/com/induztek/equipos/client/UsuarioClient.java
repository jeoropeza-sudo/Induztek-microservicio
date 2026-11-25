package com.induztek.equipos.client;

import com.induztek.equipos.config.ServiceJwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UsuarioClient {

    private final WebClient webClient;

    public UsuarioClient(
            @Value("${usuarios.service.url:http://localhost:8082}") String usuariosUrl,
            ServiceJwtProvider serviceJwtProvider
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(usuariosUrl)
                .filter(serviceJwtProvider.authorizationFilter())
                .build();
    }

    public UsuarioReferencia obtener(Long usuarioId) {
        try {
            return webClient.get()
                    .uri("/api/usuarios/{id}", usuarioId)
                    .retrieve()
                    .bodyToMono(UsuarioReferencia.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible");
        }
    }

    public record UsuarioReferencia(Long id, Long rolId, Long empresaId, boolean activo) {
    }
}
