package com.induztek.protocolos.client;

import com.induztek.protocolos.dto.UsuarioExternoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class UsuarioClient {

    private final WebClient.Builder webClientBuilder;

    public UsuarioExternoDTO obtenerPorId(Long idUsuario) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://usuarios/api/usuarios/" + idUsuario)
                    .retrieve()
                    .bodyToMono(UsuarioExternoDTO.class)
                    .block();
        } catch (Exception e) {
            return null;
        }
    }
}