package com.induztek.equipos.controller;

import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.model.HistorialCambio;
import com.induztek.equipos.service.InstalacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instalaciones")
@RequiredArgsConstructor
public class InstalacionController {

    private final InstalacionService service;

    @GetMapping
    public List<Instalacion> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Instalacion obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialCambio> historial(@PathVariable Long id) {
        return service.historial(id);
    }

    @GetMapping("/empresa/{idEmpresa}")
    public List<Instalacion> listarPorEmpresa(@PathVariable Long idEmpresa) {
        return service.listarPorEmpresa(idEmpresa);
    }

    @GetMapping("/cliente/{idCliente}")
    public List<Instalacion> listarPorCliente(@PathVariable Long idCliente) {
        return service.listarPorCliente(idCliente);
    }

    @GetMapping("/tecnico/{idTecnico}")
    public List<Instalacion> listarPorTecnico(@PathVariable Long idTecnico) {
        return service.instalacionesDeTecnico(idTecnico);
    }

    @PostMapping
    public ResponseEntity<Instalacion> crear(@Valid @RequestBody Instalacion datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public Instalacion actualizar(@PathVariable Long id, @Valid @RequestBody Instalacion datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }
}
