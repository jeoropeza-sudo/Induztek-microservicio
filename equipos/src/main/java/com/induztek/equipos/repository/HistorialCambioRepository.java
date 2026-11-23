package com.induztek.equipos.repository;

import com.induztek.equipos.model.HistorialCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Long> {

    List<HistorialCambio> findByTipoEntidadAndEntidadIdOrderByFechaDesc(String tipoEntidad, Long entidadId);
}
