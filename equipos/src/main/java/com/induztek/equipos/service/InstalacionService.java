package com.induztek.equipos.service;

import com.induztek.equipos.dto.AsignacionRequest;
import com.induztek.equipos.client.EmpresaClient;
import com.induztek.equipos.client.UsuarioClient;
import com.induztek.equipos.model.AsignacionTecnico;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.model.HistorialCambio;
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
    private final HistorialCambioService historialService;
    private final EmpresaClient empresaClient;
    private final UsuarioClient usuarioClient;
    private final ActorContext actorContext;
    private final AccesoEquiposService accesoService;

    public List<Instalacion> listarTodas() {
        if (actorContext.esAdministrador()) {
            return instalacionRepository.findAllByActivaTrueOrderByNombreAsc();
        }
        if (Long.valueOf(1L).equals(actorContext.rolId())) {
            return instalacionesDeTecnico(actorContext.usuarioId());
        }
        List<Instalacion> resultado = new java.util.ArrayList<>();
        for (Long idInstalacion : accesoService.instalacionesVisibles()) {
            instalacionRepository.findById(idInstalacion).filter(Instalacion::isActiva).ifPresent(resultado::add);
        }
        return resultado;
    }

    public Instalacion obtener(Long id) {
        accesoService.validarInstalacion(id);
        return instalacionRepository.findById(id)
                .filter(Instalacion::isActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada"));
    }

    public List<Instalacion> listarPorEmpresa(Long idEmpresa) {
        List<Instalacion> instalaciones = instalacionRepository.findByIdEmpresaAndActivaTrueOrderByNombreAsc(idEmpresa);
        return filtrarVisibles(instalaciones);
    }

    public List<Instalacion> listarPorCliente(Long idCliente) {
        if (!actorContext.esAdministrador() && !idCliente.equals(actorContext.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar instalaciones de otro cliente");
        }
        return filtrarVisibles(instalacionRepository.findByIdUsuarioClienteAndActivaTrueOrderByNombreAsc(idCliente));
    }

    @Transactional
    public Instalacion crear(Instalacion datos) {
        validarRelaciones(datos);
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la instalación es obligatorio");
        }
        datos.setNombre(datos.getNombre().trim());
        if (instalacionRepository.findByIdEmpresaAndNombreIgnoreCase(datos.getIdEmpresa(), datos.getNombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una instalación con ese nombre en la empresa");
        }
        datos.setActiva(true);
        Instalacion guardada = instalacionRepository.save(datos);
        historialService.registrar("INSTALACION", guardada.getIdInstalacion(), "ALTA", null, guardada);
        return guardada;
    }

    @Transactional
    public Instalacion actualizar(Long id, Instalacion datos) {
        Instalacion existente = obtener(id);
        Instalacion antes = copiar(existente);
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la instalación es obligatorio");
        }
        existente.setNombre(datos.getNombre().trim());
        existente.setDireccion(datos.getDireccion());
        Instalacion guardada = instalacionRepository.save(existente);
        historialService.registrar("INSTALACION", id, "CAMBIO", antes, guardada);
        return guardada;
    }

    @Transactional
    public void desactivar(Long id) {
        Instalacion instalacion = obtener(id);
        Instalacion antes = copiar(instalacion);
        instalacion.setActiva(false);
        Instalacion guardada = instalacionRepository.save(instalacion);
        historialService.registrar("INSTALACION", id, "BAJA", antes, guardada);
    }

    public List<HistorialCambio> historial(Long id) {
        obtener(id);
        return historialService.listar("INSTALACION", id);
    }

    public List<Instalacion> instalacionesDeTecnico(Long idTecnico) {
        if (!actorContext.esAdministrador() && !idTecnico.equals(actorContext.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar asignaciones de otro técnico");
        }
        return asignacionRepository.findByIdTecnico(idTecnico).stream()
                .map(asignacion -> instalacionRepository.findById(asignacion.getIdInstalacion())
                        .filter(Instalacion::isActiva)
                        .orElse(null))
                .filter(instalacion -> instalacion != null)
                .toList();
    }

    public List<Long> idsInstalacionesTecnico(Long idTecnico) {
        if (!actorContext.esAdministrador() && !idTecnico.equals(actorContext.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar asignaciones de otro técnico");
        }
        return asignacionRepository.findByIdTecnico(idTecnico).stream()
                .map(AsignacionTecnico::getIdInstalacion)
                .toList();
    }

    @Transactional
    public List<Instalacion> asignarTecnico(Long idTecnico, AsignacionRequest request) {
        if (request == null || request.idsInstalacion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista de instalaciones es obligatoria");
        }
        UsuarioClient.UsuarioReferencia tecnico = usuarioClient.obtener(idTecnico);
        if (tecnico == null || !tecnico.activo() || !Long.valueOf(1L).equals(tecnico.rolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El técnico indicado no existe o no tiene rol Técnico");
        }
        List<Long> ids = request.idsInstalacion();
        for (Long id : ids) {
            if (id == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista contiene una instalación inválida");
            }
        }

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
                    .asignadoPor(actorContext.usuarioId())
                    .build());
        }

        return instalacionesDeTecnico(idTecnico);
    }

    public void retirarAsignacion(Long idTecnico, Long idInstalacion) {
        asignacionRepository.findByIdTecnicoAndIdInstalacion(idTecnico, idInstalacion)
                .ifPresent(asignacionRepository::delete);
    }

    private void validarRelaciones(Instalacion datos) {
        if (datos.getIdEmpresa() == null || datos.getIdUsuarioCliente() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La empresa y el cliente son obligatorios");
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la instalación es obligatorio");
        }
        if (!empresaClient.existe(datos.getIdEmpresa())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La empresa indicada no existe");
        }
        UsuarioClient.UsuarioReferencia cliente = usuarioClient.obtener(datos.getIdUsuarioCliente());
        if (cliente == null || !cliente.activo() || !Long.valueOf(2L).equals(cliente.rolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente indicado no existe o no tiene rol Cliente");
        }
        if (!datos.getIdEmpresa().equals(cliente.empresaId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente no pertenece a la empresa indicada");
        }
    }

    private Instalacion copiar(Instalacion instalacion) {
        return Instalacion.builder()
                .idInstalacion(instalacion.getIdInstalacion())
                .idEmpresa(instalacion.getIdEmpresa())
                .idUsuarioCliente(instalacion.getIdUsuarioCliente())
                .nombre(instalacion.getNombre())
                .direccion(instalacion.getDireccion())
                .activa(instalacion.isActiva())
                .build();
    }

    private List<Instalacion> filtrarVisibles(List<Instalacion> instalaciones) {
        if (actorContext.esAdministrador()) {
            return instalaciones;
        }
        List<Long> idsVisibles = accesoService.instalacionesVisibles();
        return instalaciones.stream()
                .filter(instalacion -> idsVisibles.contains(instalacion.getIdInstalacion()))
                .toList();
    }
}
