package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.UsuarioClient;
import com.induztek.protocolos.dto.EquipoExternoDTO;
import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.dto.UsuarioExternoDTO;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProtocoloService {

    private final ProtocoloRepository repository;
    private final EquipoClient equipoClient;
    private final UsuarioClient usuarioClient;

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

    public Protocolo guardar(Protocolo protocolo) {
        if (protocolo.getEstado() == null || protocolo.getEstado().isBlank()) {
            protocolo.setEstado("REGISTRADO_EN_TERRENO");
        }
        return repository.save(protocolo);
    }

    public Optional<Protocolo> formalizar(Long id) {
        return repository.findById(id).map(existente -> {
            existente.setEstado("FORMALIZADO");
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}