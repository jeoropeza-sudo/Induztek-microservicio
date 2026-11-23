package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.UsuarioClient;
import com.induztek.protocolos.client.InstrumentoClient;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final InstrumentoClient instrumentoClient;
    private final ActorContext actorContext;

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
        return filtrarVisibles(repository.findAll()).stream().map(this::enriquecerDetalle).toList();
    }

    public Optional<ProtocoloDetalleResponse> buscarPorId(Long id) {
        Optional<Protocolo> protocolo = repository.findById(id);
        protocolo.ifPresent(registro -> validarAccesoEquipo(registro.getIdEquipo()));
        return protocolo.map(this::enriquecerDetalle);
    }

    public List<ProtocoloDetalleResponse> listarPorEquipo(Long idEquipo) {
        validarAccesoEquipo(idEquipo);
        return repository.findByIdEquipoOrderByIdProtocoloDesc(idEquipo)
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<ProtocoloDetalleResponse> listarPorCliente(Long idCliente) {
        if (Long.valueOf(2L).equals(actorContext.rolId())
                && !idCliente.equals(actorContext.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar protocolos de otro cliente");
        }
        List<EquipoExternoDTO> equiposCliente = equipoClient.listarPorCliente(idCliente);
        if (equiposCliente == null || equiposCliente.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idsEquipos = equiposCliente.stream()
                .map(EquipoExternoDTO::getIdEquipo)
                .toList();

        return filtrarVisibles(repository.findByIdEquipoInOrderByIdProtocoloDesc(idsEquipos))
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<ProtocoloDetalleResponse> listarPorVisita(Long idVisita) {
        return filtrarVisibles(repository.findByIdVisitaOrderByIdProtocoloDesc(idVisita))
                .stream()
                .map(this::enriquecerDetalle)
                .toList();
    }

    public List<Medicion> listarMediciones(Long idProtocolo) {
        Protocolo protocolo = repository.findById(idProtocolo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado"));
        validarAccesoEquipo(protocolo.getIdEquipo());
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
        validarAccesoEquipo(protocolo.getIdEquipo());
        if (protocolo.getIdVisita() != null
                && !equipoClient.visitaContieneEquipo(protocolo.getIdVisita(), protocolo.getIdEquipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo no pertenece a la visita indicada");
        }
        if (Long.valueOf(1L).equals(actorContext.rolId())
                && !actorContext.usuarioId().equals(protocolo.getIdTecnico())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes registrar protocolos como tu usuario");
        }
        if (protocolo.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora son obligatorias");
        }
        if (protocolo.getUuidSync() == null || protocolo.getUuidSync().isBlank()) {
            protocolo.setUuidSync(UUID.randomUUID().toString());
        } else {
            Optional<Protocolo> existente = repository.findByUuidSync(protocolo.getUuidSync());
            if (existente.isPresent()) {
                validarAccesoEquipo(existente.get().getIdEquipo());
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
            validarAccesoEquipo(existente.getIdEquipo());
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
            List<Medicion> mediciones = medicionRepository.findByIdProtocoloOrderByIdMedicionAsc(id);
            if (mediciones.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede formalizar sin mediciones");
            }
            LocalDate fechaMedicion = Instant.ofEpochMilli(existente.getFechaHora())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            for (Medicion medicion : mediciones) {
                if (medicion.getIdInstrumento() == null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cada medición debe indicar el instrumento utilizado");
                }
                instrumentoClient.validarVigencia(medicion.getIdInstrumento(), fechaMedicion);
            }
            existente.setEstado("FORMALIZADO");
            return repository.save(existente);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        Protocolo protocolo = repository.findById(id).orElse(null);
        if (protocolo == null) return false;
        validarAccesoEquipo(protocolo.getIdEquipo());
        medicionRepository.deleteByIdProtocolo(id);
        repository.deleteById(id);
        return true;
    }

    private List<Protocolo> filtrarVisibles(List<Protocolo> protocolos) {
        List<Protocolo> resultado = new java.util.ArrayList<>();
        for (Protocolo protocolo : protocolos) {
            try {
                validarAccesoEquipo(protocolo.getIdEquipo());
                resultado.add(protocolo);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() != HttpStatus.FORBIDDEN) {
                    throw e;
                }
            }
        }
        return resultado;
    }

    private void validarAccesoEquipo(Long idEquipo) {
        if (actorContext.esAdministrador()) {
            return;
        }

        Long rolId = actorContext.rolId();
        Long actorId = actorContext.usuarioId();
        List<EquipoExternoDTO> equiposVisibles;
        if (Long.valueOf(1L).equals(rolId)) {
            equiposVisibles = equipoClient.listarPorTecnico(actorId);
        } else if (Long.valueOf(2L).equals(rolId)) {
            equiposVisibles = equipoClient.listarPorCliente(actorId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este protocolo");
        }

        boolean autorizado = equiposVisibles != null && equiposVisibles.stream()
                .anyMatch(equipo -> idEquipo.equals(equipo.getIdEquipo()));
        if (!autorizado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso al equipo asociado al protocolo");
        }
    }
}