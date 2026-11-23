package com.induztek.usuarios.service;

import com.induztek.usuarios.client.AuthClient;
import com.induztek.usuarios.client.EmpresaClient;
import com.induztek.usuarios.dto.DatosRegistro;
import com.induztek.usuarios.repository.AuditoriaUsuarioRepository;
import com.induztek.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaClient empresaClient;
    @Mock
    private AuthClient authClient;
    @Mock
    private AuditoriaUsuarioRepository auditoriaRepository;
    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarRechazaRutInvalidoAntesDeConsultarServiciosRemotos() {
        DatosRegistro datos = new DatosRegistro(
                "Ana",
                "Prueba",
                "11.111.111-2",
                "ana@example.com",
                "+56912345678",
                4L,
                2L
        );

        assertThrows(ResponseStatusException.class, () -> usuarioService.registrar(datos));

        verifyNoInteractions(usuarioRepository, empresaClient, authClient, auditoriaRepository);
    }
}
