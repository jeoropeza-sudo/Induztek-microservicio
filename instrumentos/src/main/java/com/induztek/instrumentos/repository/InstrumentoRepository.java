package com.induztek.instrumentos.repository;

import com.induztek.instrumentos.model.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {
    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);
    boolean existsByNumeroSerieIgnoreCaseAndIdInstrumentoNot(String numeroSerie, Long idInstrumento);
    List<Instrumento> findByActivoTrueOrderByNombreAsc();
    List<Instrumento> findByTipoPruebaAndActivoTrueOrderByNombreAsc(String tipoPrueba);
}
