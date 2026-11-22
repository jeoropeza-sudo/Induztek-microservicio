package com.induztek.equipos.repository;

import com.induztek.equipos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByIdCliente(Long idCliente);
    List<Equipo> findByIdInstalacion(Long idInstalacion);
    boolean existsByIdInstalacionAndTagIgnoreCase(Long idInstalacion, String tag);
    boolean existsByIdInstalacionAndTagIgnoreCaseAndIdEquipoNot(
            Long idInstalacion,
            String tag,
            Long idEquipo
    );
}