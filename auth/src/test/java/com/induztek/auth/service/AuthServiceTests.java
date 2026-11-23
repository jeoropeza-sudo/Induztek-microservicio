package com.induztek.auth.service;

import com.induztek.auth.client.UsuarioClient;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.UsuarioDto;
import com.induztek.auth.model.Credencial;
import com.induztek.auth.model.IntentoAcceso;
import com.induztek.auth.repository.CredencialRepository;
import com.induztek.auth.repository.IntentoAccesoRepository;
import com.induztek.auth.repository.TokenRestablecimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private CredencialRepository credencialRepository;
    @Mock
    private TokenRestablecimientoRepository tokenRepository;
    @Mock
    private IntentoAccesoRepository intentoAccesoRepository;
    @Mock
    private PasswordEncoder cifradorContrasenas;
    @Mock
    private JwtService jwtService;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private CorreoService correoService;
    @InjectMocks
    private AuthService authService;

    @Test
    void iniciarSesionEntregaTokenYReiniciaIntentosTrasAutenticar() {
        Credencial credencial = Credencial.builder()
                .usuarioId(15L)
                .correo("cliente@example.com")
                .contrasena("hash")
                .primerIngreso(false)
                .build();
        IntentoAcceso intento = new IntentoAcceso();
        intento.setCorreo("cliente@example.com");
        intento.setFallos(2);
        when(intentoAccesoRepository.findByCorreo("cliente@example.com")).thenReturn(Optional.of(intento));
        when(credencialRepository.findByCorreo("cliente@example.com")).thenReturn(Optional.of(credencial));
        when(cifradorContrasenas.matches("clave-correcta", "hash")).thenReturn(true);
        when(usuarioClient.obtener(15L)).thenReturn(new UsuarioDto(15L, 2L, "cliente@example.com", true, false));
        when(jwtService.crearToken(15L, 2L, false)).thenReturn("jwt-valido");

        var respuesta = authService.iniciarSesion(new DatosLogin(" CLIENTE@example.com ", "clave-correcta"));

        assertEquals("jwt-valido", respuesta.token());
        assertEquals(15L, respuesta.usuarioId());
        verify(intentoAccesoRepository).delete(intento);
    }

    @Test
    void iniciarSesionConClaveIncorrectaRegistraFallo() {
        when(intentoAccesoRepository.findByCorreo("cliente@example.com")).thenReturn(Optional.empty());
        when(credencialRepository.findByCorreo("cliente@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                authService.iniciarSesion(new DatosLogin("cliente@example.com", "incorrecta")));

        verify(intentoAccesoRepository).save(any(IntentoAcceso.class));
    }
}
