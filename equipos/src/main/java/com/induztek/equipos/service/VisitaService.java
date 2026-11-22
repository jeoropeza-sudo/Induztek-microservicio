package com.induztek.equipos.service;

import com.induztek.equipos.dto.AgregarEquipoVisitaRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.model.Visita;
import com.induztek.equipos.model.VisitaEquipo;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import com.induztek.equipos.repository.VisitaEquipoRepository;
import com.induztek.equipos.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final VisitaEquipoRepository visitaEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final InstalacionRepository instalacionRepository;

    public List<Visita> listar(Long idInstalacion, Long idTecnico, String estado) {
        if (idInstalacion != null) {
            return visitaRepository.findByIdInstalacionOrderByFechaVisitaDesc(idInstalacion);
        }
        if (idTecnico != null) {
            return visitaRepository.findByIdTecnicoOrderByFechaVisitaDesc(idTecnico);
        }
        if (estado != null && !estado.isBlank()) {
            return visitaRepository.findByEstadoOrderByFechaVisitaDesc(estado.trim().toUpperCase());
        }
        return visitaRepository.findAll();
    }

    public List<Visita> listarPorFecha(LocalDate desde, LocalDate hasta) {
        LocalDate fechaDesde = desde == null ? LocalDate.now().minusYears(1) : desde;
        LocalDate fechaHasta = hasta == null ? LocalDate.now() : hasta;
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la inicial");
        }
        return visitaRepository.findByFechaVisitaBetweenOrderByFechaVisitaDesc(
                fechaDesde.atStartOfDay(),
                fechaHasta.atTime(LocalTime.MAX)
        );
    }

    public Visita obtener(Long id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visita no encontrada"));
    }

    public Visita crear(Visita datos) {
        validarCabecera(datos);
        if (datos.getUuidSync() == null || datos.getUuidSync().isBlank()) {
            datos.setUuidSync(UUID.randomUUID().toString());
        } else if (visitaRepository.findByUuidSync(datos.getUuidSync()).isPresent()) {
            return visitaRepository.findByUuidSync(datos.getUuidSync()).orElseThrow();
        }
        if (visitaRepository.findByNumeroOt(datos.getNumeroOt().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una visita con ese número de OT");
        }
        datos.setNumeroOt(datos.getNumeroOt().trim());
        datos.setTipoVisita(datos.getTipoVisita().trim().toUpperCase());
        datos.setEstado("EN_PREPARACION");
        datos.setCreatedAt(LocalDateTime.now());
        datos.setUpdatedAt(LocalDateTime.now());
        return visitaRepository.save(datos);
    }

    public Visita actualizar(Long id, Visita datos) {
        Visita existente = obtener(id);
        if (!"EN_PREPARACION".equals(existente.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La visita ya está cerrada y no puede editarse");
        }
        validarCabecera(datos);
        existente.setNumeroOt(datos.getNumeroOt().trim());
        existente.setFechaVisita(datos.getFechaVisita());
        existente.setIdInstalacion(datos.getIdInstalacion());
        existente.setIdTecnico(datos.getIdTecnico());
        existente.setTipoVisita(datos.getTipoVisita().trim().toUpperCase());
        existente.setDescripcionMantenimiento(datos.getDescripcionMantenimiento());
        existente.setTemperaturaC(datos.getTemperaturaC());
        existente.setHumedadPct(datos.getHumedadPct());
        existente.setReceptorNombre(datos.getReceptorNombre());
        existente.setReceptorRut(datos.getReceptorRut());
        existente.setReceptorCargo(datos.getReceptorCargo());
        existente.setMotivoNoRealizada(datos.getMotivoNoRealizada());
        existente.setUpdatedAt(LocalDateTime.now());
        return visitaRepository.save(existente);
    }

    @Transactional
    public VisitaEquipo agregarEquipo(Long idVisita, AgregarEquipoVisitaRequest request) {
        Visita visita = obtener(idVisita);
        if (!"EN_PREPARACION".equals(visita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La visita ya está cerrada");
        }
        Equipo equipo = equipoRepository.findById(request.idEquipo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        if (!visita.getIdInstalacion().equals(equipo.getIdInstalacion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo no pertenece a la instalación de la visita");
        }
        if (visitaEquipoRepository.findByIdVisitaAndIdEquipo(idVisita, request.idEquipo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El equipo ya fue agregado a la visita");
        }
        int orden = request.orden() == null || request.orden() < 1
                ? visitaEquipoRepository.findByIdVisitaOrderByOrdenAsc(idVisita).size() + 1
                : request.orden();
        return visitaEquipoRepository.save(VisitaEquipo.builder()
                .idVisita(idVisita)
                .idEquipo(request.idEquipo())
                .orden(orden)
                .estado("PENDIENTE")
                .build());
    }

    @Transactional
    public void retirarEquipo(Long idVisita, Long idEquipo) {
        Visita visita = obtener(idVisita);
        if (!"EN_PREPARACION".equals(visita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La visita ya está cerrada");
        }
        visitaEquipoRepository.deleteByIdVisitaAndIdEquipo(idVisita, idEquipo);
    }

    public List<VisitaEquipo> equipos(Long idVisita) {
        obtener(idVisita);
        return visitaEquipoRepository.findByIdVisitaOrderByOrdenAsc(idVisita);
    }

    public VisitaEquipo cambiarEstadoEquipo(Long idVisita, Long idEquipo, String estado) {
        obtener(idVisita);
        VisitaEquipo relacion = visitaEquipoRepository.findByIdVisitaAndIdEquipo(idVisita, idEquipo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El equipo no pertenece a la visita"));
        String nuevoEstado = estado == null ? "" : estado.trim().toUpperCase();
        if (!List.of("PENDIENTE", "REGISTRADO", "NO_REALIZADO").contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de equipo inválido");
        }
        relacion.setEstado(nuevoEstado);
        return visitaEquipoRepository.save(relacion);
    }

    public Visita cerrar(Long idVisita) {
        Visita visita = obtener(idVisita);
        if (!"EN_PREPARACION".equals(visita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La visita ya fue cerrada");
        }
        if (visitaEquipoRepository.findByIdVisitaOrderByOrdenAsc(idVisita).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La visita debe incluir al menos un equipo");
        }
        visita.setEstado("CERRADA");
        visita.setUpdatedAt(LocalDateTime.now());
        return visitaRepository.save(visita);
    }

    public Visita registrar(Long idVisita) {
        Visita visita = obtener(idVisita);
        if (!"CERRADA".equals(visita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La visita debe estar cerrada antes de registrarla");
        }
        visita.setEstado("REGISTRADA");
        visita.setUpdatedAt(LocalDateTime.now());
        return visitaRepository.save(visita);
    }

    private void validarCabecera(Visita datos) {
        if (datos.getNumeroOt() == null || datos.getNumeroOt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de OT es obligatorio");
        }
        if (datos.getFechaVisita() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de visita es obligatoria");
        }
        if (datos.getIdInstalacion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La instalación es obligatoria");
        }
        if (datos.getIdTecnico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El técnico es obligatorio");
        }
        if (datos.getTipoVisita() == null || datos.getTipoVisita().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de visita es obligatorio");
        }
        Instalacion instalacion = instalacionRepository.findById(datos.getIdInstalacion())
                .filter(Instalacion::isActiva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La instalación no existe"));
        if (!datos.getIdInstalacion().equals(instalacion.getIdInstalacion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instalación inválida");
        }
        if (datos.getTemperaturaC() != null && datos.getTemperaturaC() < -50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Temperatura fuera de rango");
        }
        if (datos.getHumedadPct() != null &&
                (datos.getHumedadPct() < 0 || datos.getHumedadPct() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La humedad debe estar entre 0 y 100");
        }
    }
}
