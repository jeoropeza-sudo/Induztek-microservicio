package com.induztek.usuarios.service;

import com.induztek.usuarios.client.EmpresaClient;
import com.induztek.usuarios.dto.DatosActualizacion;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.dto.EmpresaDto;
import com.induztek.usuarios.dto.UsuarioRespuesta;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Long ROL_TECNICO = 1L;
    private static final Long ROL_CLIENTE = 2L;
    private static final Long ROL_INGENIERO = 3L;
    private static final Long ROL_JEFE_TECNICO = 4L;

    private final UsuarioRepository usuarioRepository;
    private final EmpresaClient empresaClient;

    public List<UsuarioRespuesta> listar(String buscar) {
        List<Usuario> usuarios = (buscar == null || buscar.isBlank())
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrderByNombreAsc(buscar, buscar);

        Map<Long, String> empresas = empresaClient.listar().stream()
                .collect(Collectors.toMap(EmpresaDto::id, EmpresaDto::nombre));

        return usuarios.stream()
                .map(usuario -> aRespuesta(usuario, empresas.getOrDefault(usuario.getEmpresaId(), "")))
                .toList();
    }

    public UsuarioRespuesta obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public UsuarioRespuesta registrar(DatosRegistro datos) {
        validarDatos(datos.nombre(), datos.apellido(), datos.rut(), datos.correo(), datos.telefono(), datos.rolId());
        validarEmpresa(datos.empresaId());

        if (usuarioRepository.existsByRut(datos.rut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(datos.correo().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario nuevo = Usuario.builder()
                .nombre(datos.nombre().trim())
                .apellido(datos.apellido().trim())
                .rut(datos.rut().trim())
                .correo(datos.correo().trim().toLowerCase())
                .telefono(datos.telefono().trim())
                .empresaId(datos.empresaId())
                .rolId(datos.rolId())
                .activo(true)
                .debeCambiarClave(true)
                .build();
        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public UsuarioRespuesta actualizar(Long id, DatosActualizacion datos) {
        Usuario usuario = buscar(id);

        validarDatos(datos.nombre(), datos.apellido(), datos.rut(), datos.correo(), datos.telefono(), datos.rolId());
        validarEmpresa(datos.empresaId());

        if (usuarioRepository.existsByRutAndIdNot(datos.rut(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(datos.correo().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());
        usuario.setRut(datos.rut().trim());
        usuario.setCorreo(datos.correo().trim().toLowerCase());
        usuario.setTelefono(datos.telefono().trim());
        usuario.setEmpresaId(datos.empresaId());
        usuario.setRolId(datos.rolId());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        usuarioRepository.delete(buscar(id));
    }

    public void completarPrimerIngreso(Long id) {
        Usuario usuario = buscar(id);
        usuario.setDebeCambiarClave(false);
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarDatos(String nombre, String apellido, String rut, String correo, String telefono, Long rolId) {
        if (vacio(nombre) || vacio(apellido) || vacio(rut) || vacio(correo) || vacio(telefono)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, apellido, RUT, correo y teléfono son obligatorios");
        }
        if (!correo.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo electrónico válido");
        }
        if (!ROL_TECNICO.equals(rolId) && !ROL_CLIENTE.equals(rolId)
                && !ROL_INGENIERO.equals(rolId) && !ROL_JEFE_TECNICO.equals(rolId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol indicado no existe");
        }
    }

    private void validarEmpresa(Long empresaId) {
        if (empresaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La empresa es obligatoria");
        }
        if (empresaClient.obtener(empresaId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La empresa indicada no existe");
        }
    }

    private String nombreRol(Long rolId) {
        if (ROL_TECNICO.equals(rolId)) return "Técnico";
        if (ROL_CLIENTE.equals(rolId)) return "Cliente";
        if (ROL_INGENIERO.equals(rolId)) return "Ingeniería";
        if (ROL_JEFE_TECNICO.equals(rolId)) return "Jefatura Técnica";
        return "";
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        EmpresaDto empresa = empresaClient.obtener(usuario.getEmpresaId());
        return aRespuesta(usuario, empresa != null ? empresa.nombre() : "");
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario, String empresaNombre) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRut(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getEmpresaId(),
                empresaNombre,
                usuario.getRolId(),
                nombreRol(usuario.getRolId()),
                usuario.isActivo(),
                usuario.isDebeCambiarClave(),
                null
        );
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}