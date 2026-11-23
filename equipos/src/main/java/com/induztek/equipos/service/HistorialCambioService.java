package com.induztek.equipos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.induztek.equipos.model.HistorialCambio;
import com.induztek.equipos.repository.HistorialCambioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialCambioService {

    private final HistorialCambioRepository repository;
    private final ObjectMapper objectMapper;
    private final ActorContext actorContext;

    public void registrar(String tipoEntidad, Long entidadId, String accion, Object antes, Object despues) {
        HistorialCambio cambio = new HistorialCambio();
        cambio.setTipoEntidad(tipoEntidad);
        cambio.setEntidadId(entidadId);
        cambio.setAccion(accion);
        cambio.setActorId(actorContext.usuarioId());
        cambio.setDatosAntes(serializar(antes));
        cambio.setDatosDespues(serializar(despues));
        repository.save(cambio);
    }

    public List<HistorialCambio> listar(String tipoEntidad, Long entidadId) {
        return repository.findByTipoEntidadAndEntidadIdOrderByFechaDesc(tipoEntidad, entidadId);
    }

    private String serializar(Object valor) {
        if (valor == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo registrar el historial de cambios", e);
        }
    }
}
