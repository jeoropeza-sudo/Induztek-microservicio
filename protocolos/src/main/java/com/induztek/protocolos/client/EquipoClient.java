package com.induztek.protocolos.client;

import com.induztek.protocolos.dto.EquipoExternoDTO;
import com.induztek.protocolos.config.ServiceJwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class EquipoClient {

    private final WebClient webClient;

    public EquipoClient(
            @Value("${equipos.service.url:http://localhost:8083}") String equiposUrl,
            ServiceJwtProvider serviceJwtProvider
    ) {
        webClient = WebClient.builder()
                .baseUrl(equiposUrl)
                .filter(serviceJwtProvider.authorizationFilter())
                .build();
    }

    public EquipoExternoDTO obtenerPorId(Long idEquipo) {
        try {
            return webClient.get()
                    .uri("/api/equipos/{id}", idEquipo)
                    .retrieve()
                    .bodyToMono(EquipoExternoDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de equipos no está disponible");
        }
    }

    public List<EquipoExternoDTO> listarPorCliente(Long idCliente) {
        try {
            return webClient.get()
                    .uri("/api/equipos/cliente/{id}", idCliente)
                    .retrieve()
                    .bodyToFlux(EquipoExternoDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de equipos no está disponible");
        }
    }

    public List<EquipoExternoDTO> listarPorTecnico(Long idTecnico) {
        try {
            return webClient.get()
                    .uri("/api/equipos/tecnico/{id}", idTecnico)
                    .retrieve()
                    .bodyToFlux(EquipoExternoDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de equipos no está disponible");
        }
    }

    public boolean visitaContieneEquipo(Long idVisita, Long idEquipo) {
        try {
            List<VisitaEquipoReferencia> equipos = webClient.get()
                    .uri("/api/visitas/{id}/equipos", idVisita)
                    .retrieve()
                    .bodyToFlux(VisitaEquipoReferencia.class)
                    .collectList()
                    .block();
            return equipos != null && equipos.stream()
                    .anyMatch(equipo -> idEquipo.equals(equipo.idEquipo()));
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de equipos no está disponible");
        }
    }

    private record VisitaEquipoReferencia(Long idEquipo) {
    }
}
