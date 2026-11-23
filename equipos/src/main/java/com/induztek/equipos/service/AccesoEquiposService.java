package com.induztek.equipos.service;

import com.induztek.equipos.client.UsuarioClient;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.repository.AsignacionTecnicoRepository;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesoEquiposService {

    private final ActorContext actorContext;
    private final UsuarioClient usuarioClient;
    private final InstalacionRepository instalacionRepository;
    private final AsignacionTecnicoRepository asignacionRepository;
    private final EquipoRepository equipoRepository;

    public void validarInstalacion(Long idInstalacion) {
        if (actorContext.esAdministrador()) {
            return;
        }
        Long actorId = actorContext.usuarioId();
        Long rolId = actorContext.rolId();
        Instalacion instalacion = instalacionRepository.findById(idInstalacion)
                .filter(Instalacion::isActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada"));

        if (Long.valueOf(1L).equals(rolId)) {
            boolean asignado = asignacionRepository
                    .findByIdTecnicoAndIdInstalacion(actorId, idInstalacion)
                    .isPresent();
            if (asignado) {
                return;
            }
        } else if (Long.valueOf(2L).equals(rolId)) {
            UsuarioClient.UsuarioReferencia actor = usuarioClient.obtener(actorId);
            if (actor != null && actor.activo() && instalacion.getIdEmpresa().equals(actor.empresaId())) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta instalación");
    }

    public void validarEquipo(Equipo equipo) {
        validarInstalacion(equipo.getIdInstalacion());
    }

    public List<Equipo> equiposVisibles() {
        if (actorContext.esAdministrador()) {
            return equipoRepository.findAll();
        }

        List<Long> instalaciones = instalacionesVisibles();
        List<Equipo> equipos = new ArrayList<>();
        for (Long idInstalacion : instalaciones) {
            equipos.addAll(equipoRepository.findByIdInstalacion(idInstalacion));
        }
        return equipos;
    }

    public List<Long> instalacionesVisibles() {
        Long actorId = actorContext.usuarioId();
        Long rolId = actorContext.rolId();
        if (Long.valueOf(1L).equals(rolId)) {
            return asignacionRepository.findByIdTecnico(actorId).stream()
                    .map(asignacion -> asignacion.getIdInstalacion())
                    .toList();
        }
        if (Long.valueOf(2L).equals(rolId)) {
            UsuarioClient.UsuarioReferencia actor = usuarioClient.obtener(actorId);
            if (actor == null || !actor.activo() || actor.empresaId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo determinar la empresa del usuario");
            }
            return instalacionRepository.findByIdEmpresaAndActivaTrueOrderByNombreAsc(actor.empresaId())
                    .stream()
                    .map(Instalacion::getIdInstalacion)
                    .toList();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a instalaciones");
    }
}
