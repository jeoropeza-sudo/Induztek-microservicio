package com.induztek.equipos.repository;

import com.induztek.equipos.model.VisitaEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitaEquipoRepository extends JpaRepository<VisitaEquipo, Long> {
    List<VisitaEquipo> findByIdVisitaOrderByOrdenAsc(Long idVisita);
    Optional<VisitaEquipo> findByIdVisitaAndIdEquipo(Long idVisita, Long idEquipo);
    void deleteByIdVisitaAndIdEquipo(Long idVisita, Long idEquipo);
}
