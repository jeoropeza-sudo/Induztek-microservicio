package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosPerfil;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.dto.DatosAltaCuenta;
import com.induztek.usuarios.dto.RespuestaAltaCuenta;
import com.induztek.usuarios.model.AuditoriaUsuario;
import com.induztek.usuarios.model.AvatarUsuario;
import com.induztek.usuarios.repository.AvatarUsuarioRepository;
import com.induztek.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AvatarUsuarioRepository avatarRepository;

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

    @PatchMapping("/{id}/perfil")
    public UsuarioRespuesta actualizarPerfil(
            @PathVariable Long id,
            @RequestBody DatosPerfil datos,
            @RequestAttribute("actorId") Long actorId,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) {
        return usuarioService.actualizarPerfil(id, datos, actorId, actorRoleId);
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioRespuesta subirAvatar(
            @PathVariable Long id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestAttribute("actorId") Long actorId,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) throws IOException {
        usuarioService.validarAccesoAvatar(id, actorId, actorRoleId);
        if (archivo.isEmpty() || archivo.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen debe tener contenido y pesar hasta 5 MB");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen");
        }
        avatarRepository.save(new AvatarUsuario(id, archivo.getBytes(), tipo));
        return usuarioService.obtener(id, actorId, actorRoleId);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(
            @PathVariable Long id,
            @RequestAttribute(value = "actorId", required = false) Long actorId,
            @RequestAttribute("actorRoleId") Long actorRoleId
    ) {
        usuarioService.validarAccesoAvatar(id, actorId, actorRoleId);
        AvatarUsuario avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene avatar"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getTipoContenido()))
                .body(avatar.getContenido());
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