package com.induztek.usuarios.controller;

import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository repository;

    @GetMapping
    public List<Rol> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Rol obtener(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @GetMapping("/codigo/{codigo}")
    public Rol porCodigo(@PathVariable String codigo) {
        return repository.findByCodigo(codigo.trim().toUpperCase()).orElseThrow();
    }
}
