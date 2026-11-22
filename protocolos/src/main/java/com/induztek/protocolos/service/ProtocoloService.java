package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.UsuarioClient;
import com.induztek.protocolos.dto.EquipoExternoDTO;
import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.dto.UsuarioExternoDTO;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.dto.CrearProtocoloRequest;
import com.induztek.protocolos.model.Medicion;
import com.induztek.protocolos.repository.MedicionRepository;
import com.induztek.protocolos.repository.ProtocoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProtocoloService {

    private final ProtocoloRepository repository;
    private final EquipoClient equipoClient;
    private final UsuarioClient usuarioClient;
    private final MedicionRepository medicionRepository;

    public ProtocoloDetalleResponse enriquecerDetalle(Protocolo p) {
        EquipoExternoDTO equipo = equipoClient.obtenerPorId(p.getIdEquipo());
        UsuarioExternoDTO tecnico = usuarioClient.obtenerPorId(p.getIdTecnico());

        return new ProtocoloDetalleResponse(
                p,
                equipo != null ? equipo.getCodigo() : "Equipo #" + p.getIdEquipo(),
                equipo != null ? equipo.getTipo() : "Desconocido",
                equipo != null ? equipo.getInstalacion() : "Sin instalación",
                tecnico != null ? tecnico.getNombre() : "Técnico #" + p.getIdTecnico()
        );
    }

    public List<ProtocoloDetalleResponse> listarTodos() {
        return repository.findAll().stream().map(this::enriquecerDetalle).toList();
    }

    public Optional<ProtocoloDetalleResponse> buscarPorId(Long id) {
        return repository.findById(id).map(this::enriquecerDetalle);
    }

    public List<ProtocoloDetalleResponse> listarPorEquipo(Long idEquipo) {
        return repository.findByIdEquipoOrderByIdProtocoloDesc(idEquipo)
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<ProtocoloDetalleResponse> listarPorCliente(Long idCliente) {
        List<EquipoExternoDTO> equiposCliente = equipoClient.listarPorCliente(idCliente);
        if (equiposCliente == null || equiposCliente.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idsEquipos = equiposCliente.stream()
                .map(EquipoExternoDTO::getIdEquipo)
                .toList();

        return repository.findByIdEquipoInOrderByIdProtocoloDesc(idsEquipos)
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<ProtocoloDetalleResponse> listarPorVisita(Long idVisita) {
        return repository.findByIdVisitaOrderByIdProtocoloDesc(idVisita)
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<Medicion> listarMediciones(Long idProtocolo) {
        if (!repository.existsById(idProtocolo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado");
        }
        return medicionRepository.findByIdProtocoloOrderByIdMedicionAsc(idProtocolo);
    }

    public Protocolo guardar(Protocolo protocolo) {
        return guardarConMediciones(new CrearProtocoloRequest(protocolo, List.of()));
    }

    @Transactional
    public Protocolo guardarConMediciones(CrearProtocoloRequest request) {
        Protocolo protocolo = request.protocolo();
        if (protocolo.getIdEquipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del equipo es obligatorio");
        }
        if (protocolo.getIdTecnico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del técnico es obligatorio");
        }
        if (protocolo.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora son obligatorias");
        }
        if (protocolo.getUuidSync() == null || protocolo.getUuidSync().isBlank()) {
            protocolo.setUuidSync(UUID.randomUUID().toString());
        } else {
            Optional<Protocolo> existente = repository.findByUuidSync(protocolo.getUuidSync());
            if (existente.isPresent()) {
                return existente.get();
            }
        }
        if (protocolo.getEstado() == null || protocolo.getEstado().isBlank()) {
            protocolo.setEstado("REGISTRADO_EN_TERRENO");
        }
        if (protocolo.getResultadoGlobal() == null || protocolo.getResultadoGlobal().isBlank()) {
            protocolo.setResultadoGlobal(protocolo.getResultado());
        }
        Protocolo guardado = repository.save(protocolo);
        if (request.mediciones() != null) {
            medicionRepository.deleteByIdProtocolo(guardado.getIdProtocolo());
            for (Medicion medicion : request.mediciones()) {
                if (medicion.getTipoPrueba() == null || medicion.getTipoPrueba().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Toda medición debe indicar su tipo de prueba");
                }
                medicion.setIdMedicion(null);
                medicion.setIdProtocolo(guardado.getIdProtocolo());
                medicionRepository.save(medicion);
            }
        }
        return guardado;
    }

    public Optional<Protocolo> formalizar(Long id) {
        return repository.findById(id).map(existente -> {
            if ("FORMALIZADO".equals(existente.getEstado())) {
                return existente;
            }
            if (!"REGISTRADO_EN_TERRENO".equals(existente.getEstado()) &&
                    !"REGISTRADO".equals(existente.getEstado())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Solo puede formalizarse un protocolo registrado en terreno"
                );
            }
            existente.setEstado("FORMALIZADO");
            return repository.save(existente);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        medicionRepository.deleteByIdProtocolo(id);
        repository.deleteById(id);
        return true;
    }
}