package com.induztek.protocolos.client;

import com.induztek.protocolos.dto.EquipoExternoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EquipoClient {

    private final WebClient.Builder webClientBuilder;

    public EquipoExternoDTO obtenerPorId(Long idEquipo) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://equipos/api/equipos/" + idEquipo)
                    .retrieve()
                    .bodyToMono(EquipoExternoDTO.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }

    public List<EquipoExternoDTO> listarPorCliente(Long idCliente) {
        return webClientBuilder.build()
                .get()
                .uri("http://equipos/api/equipos/cliente/" + idCliente)
                .retrieve()
                .bodyToFlux(EquipoExternoDTO.class)
                .collectList()
                .onErrorReturn(Collections.emptyList())
                .block();
    }
}