package com.induztek.instrumentos.service;

import com.induztek.instrumentos.model.Instrumento;
import com.induztek.instrumentos.repository.CalibracionRepository;
import com.induztek.instrumentos.repository.InstrumentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrumentoServiceTests {

    @Mock
    private InstrumentoRepository instrumentoRepository;
    @Mock
    private CalibracionRepository calibracionRepository;
    @InjectMocks
    private InstrumentoService instrumentoService;

    @Test
    void crearInstrumentoSinCalibracionLoDejaInactivoYBloqueado() {
        Instrumento datos = Instrumento.builder()
                .nombre("Multímetro")
                .numeroSerie("SERIE-001")
                .tipoPrueba("continuidad")
                .activo(true)
                .build();
        when(instrumentoRepository.save(any(Instrumento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Instrumento creado = instrumentoService.crear(datos);

        assertFalse(creado.isActivo());
        assertTrue(creado.isBloqueadoPorVencimiento());
        verify(instrumentoRepository).save(datos);
    }
}
