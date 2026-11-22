package com.induztek.usuarios.service;

import com.induztek.usuarios.client.EmpresaClient;
import com.induztek.usuarios.client.AuthClient;
import com.induztek.usuarios.dto.DatosAltaCuenta;
import com.induztek.usuarios.dto.RespuestaAltaCuenta;
import com.induztek.usuarios.model.AuditoriaUsuario;
import com.induztek.usuarios.repository.AuditoriaUsuarioRepository;
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
    private final AuthClient authClient;
    private final AuditoriaUsuarioRepository auditoriaRepository;

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

    public RespuestaAltaCuenta alta(DatosAltaCuenta datos) {
        validarActorAdministrativo(datos.actorId());
        DatosRegistro registro = new DatosRegistro(
                datos.nombre(),
                datos.apellido(),
                datos.rut(),
                datos.correo(),
                datos.telefono(),
                datos.empresaId(),
                datos.rolId()
        );
        validarDatos(registro.nombre(), registro.apellido(), registro.rut(), registro.correo(), registro.telefono(), registro.rolId());
        validarEmpresa(registro.empresaId());

        if (!validarRut(registro.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un RUT válido");
        }
        if (usuarioRepository.existsByRut(registro.rut().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(registro.correo().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario nuevo = Usuario.builder()
                .nombre(registro.nombre().trim())
                .apellido(registro.apellido().trim())
                .rut(registro.rut().trim())
                .correo(registro.correo().trim().toLowerCase())
                .telefono(registro.telefono().trim())
                .empresaId(registro.empresaId())
                .rolId(registro.rolId())
                .activo(true)
                .debeCambiarClave(true)
                .build();

        Usuario guardado;
        try {
            guardado = usuarioRepository.save(nuevo);
            AuthClient.RespuestaAltaCredencial credencial =
                    authClient.crearCredencialTemporal(guardado.getId(), guardado.getCorreo());
            registrarAuditoria(datos.actorId(), guardado.getId(), "ALTA_CUENTA",
                    "Cuenta creada con rol " + nombreRol(guardado.getRolId()));
            return new RespuestaAltaCuenta(aRespuesta(guardado), credencial.contrasenaTemporal());
        } catch (RuntimeException error) {
            if (nuevo.getId() != null) {
                authClient.eliminarCredencial(nuevo.getId());
                usuarioRepository.delete(nuevo);
            }
            throw error;
        }
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
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        registrarAuditoria(null, usuario.getId(), "BAJA_CUENTA", "Cuenta dada de baja");
    }

    public UsuarioRespuesta cambiarEstado(Long id, boolean activo, Long actorId) {
        validarActorAdministrativo(actorId);
        Usuario usuario = buscar(id);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        registrarAuditoria(actorId, usuario.getId(),
                activo ? "ACTIVAR_CUENTA" : "DESACTIVAR_CUENTA",
                activo ? "Cuenta activada" : "Cuenta desactivada");
        return aRespuesta(usuario);
    }

    public void completarPrimerIngreso(Long id) {
        Usuario usuario = buscar(id);
        usuario.setDebeCambiarClave(false);
        usuarioRepository.save(usuario);
        registrarAuditoria(null, usuario.getId(), "CAMBIO_CLAVE", "Primer cambio de clave completado");
    }

    public List<AuditoriaUsuario> listarAuditoria(Long usuarioId) {
        return auditoriaRepository.findByUsuarioAfectadoIdOrderByFechaDesc(usuarioId);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarDatos(String nombre, String apellido, String rut, String correo, String telefono, Long rolId) {
        if (vacio(nombre) || vacio(apellido) || vacio(rut) || vacio(correo) || vacio(telefono)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, apellido, RUT, correo y teléfono son obligatorios");
        }
        if (!validarRut(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un RUT válido");
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

    private void validarActorAdministrativo(Long actorId) {
        if (actorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El actor que crea la cuenta es obligatorio");
        }
        Usuario actor = usuarioRepository.findById(actorId)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario administrador no existe"));
        if (!ROL_INGENIERO.equals(actor.getRolId()) && !ROL_JEFE_TECNICO.equals(actor.getRolId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo Ingeniería o Jefatura Técnica pueden crear cuentas");
        }
    }

    private void registrarAuditoria(Long actorId, Long usuarioId, String accion, String detalle) {
        auditoriaRepository.save(AuditoriaUsuario.builder()
                .actorId(actorId)
                .usuarioAfectadoId(usuarioId)
                .accion(accion)
                .detalle(detalle)
                .build());
    }

    private boolean validarRut(String rut) {
        if (rut == null) return false;
        String limpio = rut.replace(".", "").replace("-", "").toUpperCase();
        if (!limpio.matches("\\d{7,8}[0-9K]")) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dvCalculado == dvIngresado;
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