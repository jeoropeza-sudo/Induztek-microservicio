package com.induztek.equipos.service;

import com.induztek.equipos.client.UsuarioClient;
import com.induztek.equipos.repository.AsignacionTecnicoRepository;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTests {

    @Mock
    private EquipoRepository repository;
    @Mock
    private InstalacionRepository instalacionRepository;
    @Mock
    private AsignacionTecnicoRepository asignacionRepository;
    @Mock
    private HistorialCambioService historialService;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private AccesoEquiposService accesoService;
    @Mock
    private ActorContext actorContext;
    @InjectMocks
    private EquipoService equipoService;

    @Test
    void crearEquipoSinTagLoRechazaAntesDePersistir() {
        assertThrows(IllegalArgumentException.class, () -> equipoService.guardar(new com.induztek.equipos.model.Equipo()));

        verifyNoInteractions(repository, instalacionRepository, asignacionRepository, historialService, usuarioClient, accesoService);
    }
}
