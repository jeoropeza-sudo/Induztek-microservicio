package com.induztek.protocolos.service;

import com.induztek.protocolos.client.EquipoClient;
import com.induztek.protocolos.client.InstrumentoClient;
import com.induztek.protocolos.client.UsuarioClient;
import com.induztek.protocolos.dto.CrearProtocoloRequest;
import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.MedicionRepository;
import com.induztek.protocolos.repository.ProtocoloRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProtocoloServiceTests {

    @Mock
    private ProtocoloRepository repository;
    @Mock
    private EquipoClient equipoClient;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private MedicionRepository medicionRepository;
    @Mock
    private InstrumentoClient instrumentoClient;
    @Mock
    private ActorContext actorContext;
    @InjectMocks
    private ProtocoloService protocoloService;

    @Test
    void guardarRechazaProtocoloSinEquipoAntesDeLlamadasRemotas() {
        Protocolo protocolo = new Protocolo();

        assertThrows(ResponseStatusException.class, () ->
                protocoloService.guardarConMediciones(new CrearProtocoloRequest(protocolo, List.of())));

        verifyNoInteractions(repository, equipoClient, usuarioClient, medicionRepository, instrumentoClient, actorContext);
    }
}
