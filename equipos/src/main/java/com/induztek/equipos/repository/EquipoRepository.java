package com.induztek.equipos.repository;

import com.induztek.equipos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByIdCliente(Long idCliente);
    boolean existsByCodigo(String codigo);
}