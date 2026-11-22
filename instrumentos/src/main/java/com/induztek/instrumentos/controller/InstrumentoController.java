package com.induztek.instrumentos.controller;

import com.induztek.instrumentos.dto.VigenciaResponse;
import com.induztek.instrumentos.model.Calibracion;
import com.induztek.instrumentos.model.Instrumento;
import com.induztek.instrumentos.service.InstrumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
@RequiredArgsConstructor
public class InstrumentoController {

    private final InstrumentoService service;

    @GetMapping
    public List<Instrumento> listar(
            @RequestParam(required = false) String tipoPrueba,
            @RequestParam(required = false) Boolean activo
    ) {
        return service.listar(tipoPrueba, activo);
    }

    @GetMapping("/{id}")
    public Instrumento obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Instrumento crear(@Valid @RequestBody Instrumento datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id}")
    public Instrumento actualizar(@PathVariable Long id, @Valid @RequestBody Instrumento datos) {
        return service.actualizar(id, datos);
    }

    @PatchMapping("/{id}/estado")
    public Instrumento cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return service.cambiarEstado(id, activo);
    }

    @PostMapping("/{id}/calibraciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Calibracion registrarCalibracion(
            @PathVariable Long id,
            @Valid @RequestBody Calibracion datos
    ) {
        return service.registrarCalibracion(id, datos);
    }

    @GetMapping("/{id}/calibraciones")
    public List<Calibracion> listarCalibraciones(@PathVariable Long id) {
        return service.listarCalibraciones(id);
    }

    @GetMapping("/{id}/vigencia")
    public VigenciaResponse vigencia(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return service.vigencia(id, fecha);
    }
}
