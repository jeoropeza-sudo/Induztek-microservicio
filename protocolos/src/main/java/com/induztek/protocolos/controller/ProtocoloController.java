package com.induztek.protocolos.controller;

import com.induztek.protocolos.dto.ProtocoloDetalleResponse;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.service.ProtocoloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocolos")
@RequiredArgsConstructor
public class ProtocoloController {

    private final ProtocoloService service;

    @GetMapping
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProtocoloDetalleResponse> obtenerPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipo/{idEquipo}")
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarPorEquipo(@PathVariable Long idEquipo) {
        return ResponseEntity.ok(service.listarPorEquipo(idEquipo));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ProtocoloDetalleResponse>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @PostMapping
    public ResponseEntity<Protocolo> crear(@Valid @RequestBody Protocolo protocolo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(protocolo));
    }

    @PatchMapping("/{id}/formalizar")
    public ResponseEntity<Protocolo> formalizar(@PathVariable Long id) {
        return service.formalizar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!service.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}