package com.induztek.auth.service;

import com.induztek.auth.client.UsuarioClient;
import com.induztek.auth.dto.DatosCambioContrasena;
import com.induztek.auth.dto.DatosCredencial;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.DatosOlvide;
import com.induztek.auth.dto.DatosRestablecer;
import com.induztek.auth.dto.RespuestaLogin;
import com.induztek.auth.dto.UsuarioDto;
import com.induztek.auth.model.Credencial;
import com.induztek.auth.model.IntentoAcceso;
import com.induztek.auth.model.TokenRestablecimiento;
import com.induztek.auth.repository.CredencialRepository;
import com.induztek.auth.repository.IntentoAccesoRepository;
import com.induztek.auth.repository.TokenRestablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MINUTOS_CODIGO = 15;
    private static final int MAX_INTENTOS_LOGIN = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    private final CredencialRepository credencialRepository;
    private final TokenRestablecimientoRepository tokenRepository;
    private final IntentoAccesoRepository intentoAccesoRepository;
    private final PasswordEncoder cifradorContrasenas;
    private final JwtService jwtService;
    private final UsuarioClient usuarioClient;
    private final CorreoService correoService;

    public RespuestaLogin iniciarSesion(DatosLogin datos) {
        String correo = datos.correo() == null ? "" : datos.correo().trim().toLowerCase();
        IntentoAcceso intento = intentoAccesoRepository.findByCorreo(correo).orElse(null);
        if (intento != null && intento.getBloqueadoHasta() != null
                && intento.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos. Intenta más tarde");
        }

        Credencial credencial = credencialRepository.findByCorreo(correo).orElse(null);
        if (credencial == null || datos.contrasena() == null
                || !cifradorContrasenas.matches(datos.contrasena(), credencial.getContrasena())) {
            registrarFalloAcceso(correo, intento);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        UsuarioDto usuario = usuarioClient.obtener(credencial.getUsuarioId());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta no tiene un usuario asociado");
        }
        if (!usuario.activo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta está inactiva");
        }

        if (intento != null) {
            intentoAccesoRepository.delete(intento);
        }
        String token = jwtService.crearToken(usuario.id(), usuario.rolId(), credencial.isPrimerIngreso());
        return new RespuestaLogin(token, usuario.id(), usuario.rolId(), credencial.isPrimerIngreso());
    }

    public void crearCredencial(DatosCredencial datos) {
        if (datos.usuarioId() == null || vacio(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario y el correo son obligatorios");
        }
        String correo = datos.correo().trim().toLowerCase();
        validarContrasena(datos.contrasena());
        if (credencialRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        if (credencialRepository.existsByUsuarioId(datos.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya tiene una cuenta");
        }
        UsuarioDto usuario = usuarioClient.obtener(datos.usuarioId());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario indicado no existe");
        }
        if (usuario.correo() == null || !usuario.correo().equalsIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo no coincide con el usuario registrado");
        }

        Credencial nueva = Credencial.builder()
                .usuarioId(datos.usuarioId())
                .correo(correo)
                .contrasena(cifradorContrasenas.encode(datos.contrasena()))
                .primerIngreso(true)
                .build();
        credencialRepository.save(nueva);
    }

    public void crearCredencialTemporal(DatosCredencial datos) {
        if (datos.usuarioId() == null || vacio(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario y el correo son obligatorios");
        }
        String correo = datos.correo().trim().toLowerCase();
        UsuarioDto usuario = usuarioClient.obtener(datos.usuarioId());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario indicado no existe");
        }
        if (usuario.correo() == null || !usuario.correo().equalsIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo no coincide con el usuario registrado");
        }
        if (credencialRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        if (credencialRepository.existsByUsuarioId(datos.usuarioId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya tiene una cuenta");
        }

        String contrasenaTemporal = generarContrasenaTemporal();
        Credencial nueva = Credencial.builder()
                .usuarioId(datos.usuarioId())
                .correo(correo)
                .contrasena(cifradorContrasenas.encode(contrasenaTemporal))
                .primerIngreso(true)
                .build();
        credencialRepository.save(nueva);
        correoService.enviarContrasenaTemporal(correo, contrasenaTemporal);
    }

    public void eliminarCredencial(Long usuarioId) {
        credencialRepository.findByUsuarioId(usuarioId).ifPresent(credencialRepository::delete);
    }

    public RespuestaLogin configurar(DatosCambioContrasena datos, Long actorId, Long actorRoleId) {
        String correo = datos.correo() == null ? "" : datos.correo().trim().toLowerCase();
        Credencial credencial = credencialRepository.findByCorreo(correo)
                .filter(c -> cifradorContrasenas.matches(datos.contrasenaActual(), c.getContrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta"));
        if (!credencial.getUsuarioId().equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes cambiar tu propia contraseña");
        }

        validarContrasena(datos.contrasenaNueva());

        credencial.setContrasena(cifradorContrasenas.encode(datos.contrasenaNueva()));
        credencial.setPrimerIngreso(false);
        credencialRepository.save(credencial);
        usuarioClient.completarPrimerIngreso(credencial.getUsuarioId());
        String token = jwtService.crearToken(actorId, actorRoleId, false);
        return new RespuestaLogin(token, actorId, actorRoleId, false);
    }

    public void olvide(DatosOlvide datos) {
        String correo = datos.correo() == null ? "" : datos.correo().trim().toLowerCase();
        credencialRepository.findByCorreo(correo).ifPresent(credencial -> {
            for (TokenRestablecimiento anterior : tokenRepository.findByUsuarioIdAndUsadoFalse(credencial.getUsuarioId())) {
                anterior.setUsado(true);
                tokenRepository.save(anterior);
            }
            String codigo = generarCodigo();

            TokenRestablecimiento token = TokenRestablecimiento.builder()
                    .usuarioId(credencial.getUsuarioId())
                    .codigo(cifradorContrasenas.encode(codigo))
                    .venceEn(LocalDateTime.now().plusMinutes(MINUTOS_CODIGO))
                    .usado(false)
                    .build();
            tokenRepository.save(token);

            correoService.enviarCodigoRestablecimiento(credencial.getCorreo(), codigo);
        });
    }

    public void restablecer(DatosRestablecer datos) {
        String correo = datos.correo() == null ? "" : datos.correo().trim().toLowerCase();
        Credencial credencial = credencialRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido o vencido"));

        validarContrasena(datos.contrasenaNueva());

        TokenRestablecimiento token = tokenRepository.findByUsuarioIdAndUsadoFalse(credencial.getUsuarioId()).stream()
                .filter(t -> t.getVenceEn().isAfter(LocalDateTime.now()))
                .filter(t -> cifradorContrasenas.matches(datos.codigo(), t.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido o vencido"));

        token.setUsado(true);
        tokenRepository.save(token);

        credencial.setContrasena(cifradorContrasenas.encode(datos.contrasenaNueva()));
        credencial.setPrimerIngreso(false);
        credencialRepository.save(credencial);
        usuarioClient.completarPrimerIngreso(credencial.getUsuarioId());
    }

    private String generarCodigo() {
        int numero = new SecureRandom().nextInt(1_000_000);
        return String.format("%06d", numero);
    }

    private void registrarFalloAcceso(String correo, IntentoAcceso intento) {
        if (correo.isBlank()) {
            return;
        }
        IntentoAcceso registro = intento;
        if (registro == null) {
            registro = new IntentoAcceso();
            registro.setCorreo(correo);
        }

        if (registro.getBloqueadoHasta() != null
                && !registro.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            registro.setFallos(0);
            registro.setBloqueadoHasta(null);
        }

        int fallos = registro.getFallos() + 1;
        registro.setFallos(fallos);
        if (fallos >= MAX_INTENTOS_LOGIN) {
            registro.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        intentoAccesoRepository.save(registro);
    }

    private String generarContrasenaTemporal() {
        char[] caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder("Indz");
        for (int i = 0; i < 9; i++) {
            password.append(caracteres[random.nextInt(caracteres.length)]);
        }
        return password.toString();
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}