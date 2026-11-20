package com.induztek.usuarios.client;

import com.induztek.usuarios.dto.EmpresaDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class EmpresaClient {

    private final WebClient webClient;

    public EmpresaClient(@Value("${empresas.service.url}") String empresasUrl) {
        this.webClient = WebClient.builder().baseUrl(empresasUrl).build();
    }

    public List<EmpresaDto> listar() {
        try {
            return webClient.get()
                    .uri("/api/empresas")
                    .retrieve()
                    .bodyToFlux(EmpresaDto.class)
                    .collectList()
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    public EmpresaDto obtener(Long id) {
        try {
            return webClient.get()
                    .uri("/api/empresas/{id}", id)
                    .retrieve()
                    .bodyToMono(EmpresaDto.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de empresas no está disponible");
    }
}