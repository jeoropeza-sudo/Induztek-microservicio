package com.induztek.empresas.service;

import com.induztek.empresas.client.UsuarioClient;
import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private UsuarioClient usuarioClient;
    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void crearEmpresaValidaNormalizaCorreoYPersiste() {
        DatosEmpresa datos = new DatosEmpresa(
                "Empresa de prueba",
                "11.111.111-1",
                "Servicios",
                "Av. Central 123",
                "Contacto",
                " CONTACTO@EXAMPLE.COM ",
                "+56912345678"
        );
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Empresa creada = empresaService.crear(datos);

        assertEquals("contacto@example.com", creada.getContactoCorreo());
        assertEquals("11.111.111-1", creada.getRut());
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void crearEmpresaRechazaRutInvalidoSinGuardar() {
        DatosEmpresa datos = new DatosEmpresa(
                "Empresa de prueba",
                "11.111.111-2",
                "Servicios",
                "Av. Central 123",
                "Contacto",
                "contacto@example.com",
                "+56912345678"
        );

        assertThrows(ResponseStatusException.class, () -> empresaService.crear(datos));
    }
}
