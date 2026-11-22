package com.induztek.instrumentos.repository;

import com.induztek.instrumentos.model.Calibracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface CalibracionRepository extends JpaRepository<Calibracion, Long> {
    List<Calibracion> findByIdInstrumentoOrderByFechaVencimientoDesc(Long idInstrumento);
    Optional<Calibracion> findFirstByIdInstrumentoOrderByFechaVencimientoDesc(Long idInstrumento);
    Optional<Calibracion> findFirstByIdInstrumentoAndFechaCalibracionLessThanEqualAndFechaVencimientoGreaterThanEqualOrderByFechaCalibracionDesc(
            Long idInstrumento,
            LocalDate fecha,
            LocalDate mismaFecha
    );
}
