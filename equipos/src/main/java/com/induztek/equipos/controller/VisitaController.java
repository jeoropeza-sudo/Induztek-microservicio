package com.induztek.equipos.controller;

import com.induztek.equipos.dto.AgregarEquipoVisitaRequest;
import com.induztek.equipos.model.Visita;
import com.induztek.equipos.model.VisitaEquipo;
import com.induztek.equipos.service.VisitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visitas")
@RequiredArgsConstructor
public class VisitaController {

    private final VisitaService service;

    @GetMapping
    public List<Visita> listar(
            @RequestParam(required = false) Long instalacion,
            @RequestParam(required = false) Long tecnico,
            @RequestParam(required = false) String estado
    ) {
        return service.listar(instalacion, tecnico, estado);
    }

    @GetMapping("/fecha")
    public List<Visita> listarPorFecha(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return service.listarPorFecha(desde, hasta);
    }

    @GetMapping("/{id}")
    public Visita obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Visita crear(@Valid @RequestBody Visita datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id}")
    public Visita actualizar(@PathVariable Long id, @Valid @RequestBody Visita datos) {
        return service.actualizar(id, datos);
    }

    @PostMapping("/{id}/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitaEquipo agregarEquipo(
            @PathVariable Long id,
            @Valid @RequestBody AgregarEquipoVisitaRequest datos
    ) {
        return service.agregarEquipo(id, datos);
    }

    @GetMapping("/{id}/equipos")
    public List<VisitaEquipo> equipos(@PathVariable Long id) {
        return service.equipos(id);
    }

    @DeleteMapping("/{id}/equipos/{idEquipo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirarEquipo(
            @PathVariable Long id,
            @PathVariable Long idEquipo
    ) {
        service.retirarEquipo(id, idEquipo);
    }

    @PatchMapping("/{id}/equipos/{idEquipo}/estado")
    public VisitaEquipo cambiarEstadoEquipo(
            @PathVariable Long id,
            @PathVariable Long idEquipo,
            @RequestParam String estado
    ) {
        return service.cambiarEstadoEquipo(id, idEquipo, estado);
    }

    @PatchMapping("/{id}/cerrar")
    public Visita cerrar(@PathVariable Long id) {
        return service.cerrar(id);
    }

    @PatchMapping("/{id}/registrar")
    public Visita registrar(@PathVariable Long id) {
        return service.registrar(id);
    }
}
