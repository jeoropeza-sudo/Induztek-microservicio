package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.dto.DatosAltaCuenta;
import com.induztek.usuarios.dto.RespuestaAltaCuenta;
import com.induztek.usuarios.model.AuditoriaUsuario;
import com.induztek.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioRespuesta> listar(
            @RequestParam(required = false) String buscar,
            @RequestAttribute(value = "actorId", required = false) Long actorId,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) {
        return usuarioService.listar(buscar, actorId, actorRoleId);
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta obtener(
            @PathVariable Long id,
            @RequestAttribute(value = "actorId", required = false) Long actorId,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) {
        return usuarioService.obtener(id, actorId, actorRoleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaAltaCuenta registrar(
            @RequestBody DatosRegistro datos,
            @RequestAttribute("actorId") Long actorId
    ) {
        DatosAltaCuenta alta = new DatosAltaCuenta(
                actorId,
                datos.nombre(),
                datos.apellido(),
                datos.rut(),
                datos.correo(),
                datos.telefono(),
                datos.empresaId(),
                datos.rolId()
        );
        return usuarioService.alta(alta, actorId);
    }

    @PostMapping("/alta")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaAltaCuenta alta(
            @RequestBody DatosAltaCuenta datos,
            @RequestAttribute("actorId") Long actorId
    ) {
        return usuarioService.alta(datos, actorId);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioRespuesta cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo,
            @RequestAttribute("actorId") Long actorId
    ) {
        return usuarioService.cambiarEstado(id, activo, actorId);
    }

    @GetMapping("/{id}/auditoria")
    public List<AuditoriaUsuario> auditoria(
            @PathVariable Long id,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) {
        usuarioService.validarAccesoAuditoria(actorRoleId);
        return usuarioService.listarAuditoria(id);
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta actualizar(
            @PathVariable Long id,
            @RequestBody DatosActualizacion datos,
            @RequestAttribute("actorId") Long actorId
    ) {
        return usuarioService.actualizar(id, datos, actorId);
    }

    @PatchMapping("/{id}/primer-ingreso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completarPrimerIngreso(@PathVariable Long id) {
        usuarioService.completarPrimerIngreso(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestAttribute("actorId") Long actorId) {
        usuarioService.eliminar(id, actorId);
    }
}