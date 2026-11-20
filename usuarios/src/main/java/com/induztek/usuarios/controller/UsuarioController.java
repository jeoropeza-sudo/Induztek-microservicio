package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioRespuesta> listar(@RequestParam(required = false) String buscar) {
        return usuarioService.listar(buscar);
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta registrar(@RequestBody DatosRegistro datos) {
        return usuarioService.registrar(datos);
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(@PathVariable Long id, @RequestBody DatosActualizacion datos) {
        return usuarioService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}