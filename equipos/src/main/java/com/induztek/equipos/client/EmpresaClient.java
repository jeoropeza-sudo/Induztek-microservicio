package com.induztek.equipos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EmpresaClient {

    private final WebClient webClient;

    public EmpresaClient(
            @Value("${empresas.service.url:http://localhost:8081}") String empresasUrl,
            @Value("${security.internal-token}") String internalToken
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(empresasUrl)
                .defaultHeader("X-Internal-Token", internalToken)
                .build();
    }

    public boolean existe(Long empresaId) {
        try {
            return webClient.get()
                    .uri("/api/empresas/{id}", empresaId)
                    .retrieve()
                    .toBodilessEntity()
                    .block() != null;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de empresas no está disponible");
        }
    }
}
