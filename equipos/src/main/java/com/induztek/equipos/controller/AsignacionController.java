package com.induztek.equipos.controller;

import com.induztek.equipos.dto.AsignacionRequest;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.service.InstalacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final InstalacionService service;

    @GetMapping("/tecnico/{idTecnico}")
    public List<Instalacion> listar(@PathVariable Long idTecnico) {
        return service.instalacionesDeTecnico(idTecnico);
    }

    @GetMapping("/tecnico/{idTecnico}/ids")
    public List<Long> ids(@PathVariable Long idTecnico) {
        return service.idsInstalacionesTecnico(idTecnico);
    }

    @PutMapping("/tecnico/{idTecnico}")
    public List<Instalacion> asignar(
            @PathVariable Long idTecnico,
            @RequestBody AsignacionRequest request
    ) {
        return service.asignarTecnico(idTecnico, request);
    }

    @DeleteMapping("/tecnico/{idTecnico}/instalacion/{idInstalacion}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(
            @PathVariable Long idTecnico,
            @PathVariable Long idInstalacion
    ) {
        service.retirarAsignacion(idTecnico, idInstalacion);
    }
}
