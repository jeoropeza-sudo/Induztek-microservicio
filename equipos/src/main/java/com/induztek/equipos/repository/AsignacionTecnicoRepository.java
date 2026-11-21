package com.induztek.equipos.repository;

import com.induztek.equipos.model.AsignacionTecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionTecnicoRepository extends JpaRepository<AsignacionTecnico, Long> {
    List<AsignacionTecnico> findByIdTecnico(Long idTecnico);
    List<AsignacionTecnico> findByIdInstalacion(Long idInstalacion);
    Optional<AsignacionTecnico> findByIdTecnicoAndIdInstalacion(Long idTecnico, Long idInstalacion);
    void deleteByIdTecnico(Long idTecnico);
}
