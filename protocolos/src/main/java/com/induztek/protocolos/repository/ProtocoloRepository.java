package com.induztek.protocolos.repository;

import com.induztek.protocolos.model.Protocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {
    List<Protocolo> findByIdEquipoOrderByIdProtocoloDesc(Long idEquipo);
    List<Protocolo> findByIdEquipoInOrderByIdProtocoloDesc(List<Long> idsEquipos);
    List<Protocolo> findByIdTecnicoOrderByIdProtocoloDesc(Long idTecnico);
}