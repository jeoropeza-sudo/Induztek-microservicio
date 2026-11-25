package com.induztek.protocolos.client;

import com.induztek.protocolos.config.ServiceJwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Component
public class InstrumentoClient {

    private final WebClient webClient;

    public InstrumentoClient(
            @Value("${instrumentos.service.url:http://localhost:8086}") String instrumentosUrl,
            ServiceJwtProvider serviceJwtProvider
    ) {
        webClient = WebClient.builder()
                .baseUrl(instrumentosUrl)
                .filter(serviceJwtProvider.authorizationFilter())
                .build();
    }

    public void validarVigencia(Long idInstrumento, LocalDate fecha) {
        try {
            InstrumentoReferencia instrumento = webClient.get()
                    .uri("/api/instrumentos/{id}", idInstrumento)
                    .retrieve()
                    .bodyToMono(InstrumentoReferencia.class)
                    .block();
            if (instrumento == null || !instrumento.activo()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El instrumento está inactivo");
            }

            VigenciaReferencia vigencia = webClient.get()
                    .uri(uri -> uri.path("/api/instrumentos/{id}/vigencia")
                            .queryParam("fecha", fecha)
                            .build(idInstrumento))
                    .retrieve()
                    .bodyToMono(VigenciaReferencia.class)
                    .block();
            if (vigencia == null || !vigencia.vigente()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El instrumento no estaba vigente en la fecha de medición");
            }
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de instrumentos no está disponible");
        }
    }

    private record InstrumentoReferencia(boolean activo) {
    }

    private record VigenciaReferencia(boolean vigente) {
    }
}
