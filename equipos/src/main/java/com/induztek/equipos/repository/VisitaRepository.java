package com.induztek.equipos.repository;

import com.induztek.equipos.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
    Optional<Visita> findByNumeroOt(String numeroOt);
    Optional<Visita> findByUuidSync(String uuidSync);
    List<Visita> findByIdInstalacionOrderByFechaVisitaDesc(Long idInstalacion);
    List<Visita> findByIdTecnicoOrderByFechaVisitaDesc(Long idTecnico);
    List<Visita> findByEstadoOrderByFechaVisitaDesc(String estado);
    List<Visita> findByFechaVisitaBetweenOrderByFechaVisitaDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
