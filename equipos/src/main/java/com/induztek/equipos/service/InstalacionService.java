package com.induztek.equipos.service;

import com.induztek.equipos.dto.AsignacionRequest;
import com.induztek.equipos.model.AsignacionTecnico;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.repository.AsignacionTecnicoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InstalacionService {

    private final InstalacionRepository instalacionRepository;
    private final AsignacionTecnicoRepository asignacionRepository;

    public List<Instalacion> listarTodas() {
        return instalacionRepository.findAllByActivaTrueOrderByNombreAsc();
    }

    public Instalacion obtener(Long id) {
        return instalacionRepository.findById(id)
                .filter(Instalacion::isActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada"));
    }

    public List<Instalacion> listarPorEmpresa(Long idEmpresa) {
        return instalacionRepository.findByIdEmpresaAndActivaTrueOrderByNombreAsc(idEmpresa);
    }

    public List<Instalacion> listarPorCliente(Long idCliente) {
        return instalacionRepository.findByIdUsuarioClienteAndActivaTrueOrderByNombreAsc(idCliente);
    }

    public Instalacion crear(Instalacion datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la instalación es obligatorio");
        }
        datos.setNombre(datos.getNombre().trim());
        if (instalacionRepository.findByIdEmpresaAndNombreIgnoreCase(datos.getIdEmpresa(), datos.getNombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una instalación con ese nombre en la empresa");
        }
        datos.setActiva(true);
        return instalacionRepository.save(datos);
    }

    public Instalacion actualizar(Long id, Instalacion datos) {
        Instalacion existente = obtener(id);
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la instalación es obligatorio");
        }
        existente.setNombre(datos.getNombre().trim());
        existente.setDireccion(datos.getDireccion());
        return instalacionRepository.save(existente);
    }

    public void desactivar(Long id) {
        Instalacion instalacion = obtener(id);
        instalacion.setActiva(false);
        instalacionRepository.save(instalacion);
    }

    public List<Instalacion> instalacionesDeTecnico(Long idTecnico) {
        return asignacionRepository.findByIdTecnico(idTecnico).stream()
                .map(asignacion -> instalacionRepository.findById(asignacion.getIdInstalacion())
                        .filter(Instalacion::isActiva)
                        .orElse(null))
                .filter(instalacion -> instalacion != null)
                .toList();
    }

    public List<Long> idsInstalacionesTecnico(Long idTecnico) {
        return asignacionRepository.findByIdTecnico(idTecnico).stream()
                .map(AsignacionTecnico::getIdInstalacion)
                .toList();
    }

    @Transactional
    public List<Instalacion> asignarTecnico(Long idTecnico, AsignacionRequest request) {
        List<Long> ids = request == null || request.idsInstalacion() == null
                ? List.of()
                : request.idsInstalacion();

        Set<Long> idUnicos = new HashSet<>(ids);
        for (Long idInstalacion : idUnicos) {
            obtener(idInstalacion);
            for (AsignacionTecnico ocupada : asignacionRepository.findByIdInstalacion(idInstalacion)) {
                if (!ocupada.getIdTecnico().equals(idTecnico)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "La instalación " + idInstalacion + " ya está asignada a otro técnico"
                    );
                }
            }
        }

        asignacionRepository.deleteByIdTecnico(idTecnico);
        asignacionRepository.flush();

        for (Long idInstalacion : idUnicos) {
            asignacionRepository.save(AsignacionTecnico.builder()
                    .idTecnico(idTecnico)
                    .idInstalacion(idInstalacion)
                    .asignadoPor(request != null ? request.asignadoPor() : null)
                    .build());
        }

        return instalacionesDeTecnico(idTecnico);
    }

    public void retirarAsignacion(Long idTecnico, Long idInstalacion) {
        asignacionRepository.findByIdTecnicoAndIdInstalacion(idTecnico, idInstalacion)
                .ifPresent(asignacionRepository::delete);
    }
}
