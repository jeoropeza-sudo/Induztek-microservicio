package com.induztek.instrumentos.service;

import com.induztek.instrumentos.dto.VigenciaResponse;
import com.induztek.instrumentos.model.Calibracion;
import com.induztek.instrumentos.model.Instrumento;
import com.induztek.instrumentos.repository.CalibracionRepository;
import com.induztek.instrumentos.repository.InstrumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;
    private final CalibracionRepository calibracionRepository;

    public List<Instrumento> listar(String tipoPrueba, Boolean activo) {
        if (tipoPrueba != null && !tipoPrueba.isBlank()) {
            return instrumentoRepository.findByTipoPruebaAndActivoTrueOrderByNombreAsc(tipoPrueba.trim().toUpperCase());
        }
        if (Boolean.FALSE.equals(activo)) {
            return instrumentoRepository.findAll();
        }
        return instrumentoRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Instrumento obtener(Long id) {
        return instrumentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrumento no encontrado"));
    }

    public Instrumento crear(Instrumento datos) {
        validar(datos);
        String serie = datos.getNumeroSerie().trim();
        if (instrumentoRepository.existsByNumeroSerieIgnoreCase(serie)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un instrumento con ese número de serie");
        }
        datos.setNumeroSerie(serie);
        datos.setTipoPrueba(datos.getTipoPrueba().trim().toUpperCase());
        datos.setActivo(true);
        return instrumentoRepository.save(datos);
    }

    public Instrumento actualizar(Long id, Instrumento datos) {
        Instrumento existente = obtener(id);
        validar(datos);
        String serie = datos.getNumeroSerie().trim();
        if (instrumentoRepository.existsByNumeroSerieIgnoreCaseAndIdInstrumentoNot(serie, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un instrumento con ese número de serie");
        }
        existente.setNombre(datos.getNombre().trim());
        existente.setMarca(datos.getMarca());
        existente.setModelo(datos.getModelo());
        existente.setNumeroSerie(serie);
        existente.setTipoPrueba(datos.getTipoPrueba().trim().toUpperCase());
        return instrumentoRepository.save(existente);
    }

    public Instrumento cambiarEstado(Long id, boolean activo) {
        Instrumento instrumento = obtener(id);
        instrumento.setActivo(activo);
        return instrumentoRepository.save(instrumento);
    }

    public Calibracion registrarCalibracion(Long idInstrumento, Calibracion datos) {
        obtener(idInstrumento);
        if (datos.getFechaCalibracion() == null || datos.getFechaVencimiento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de calibración y vencimiento son obligatorias");
        }
        if (datos.getFechaVencimiento().isBefore(datos.getFechaCalibracion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento no puede ser anterior a la calibración");
        }
        datos.setIdInstrumento(idInstrumento);
        return calibracionRepository.save(datos);
    }

    public List<Calibracion> listarCalibraciones(Long idInstrumento) {
        obtener(idInstrumento);
        return calibracionRepository.findByIdInstrumentoOrderByFechaVencimientoDesc(idInstrumento);
    }

    public VigenciaResponse vigencia(Long idInstrumento, LocalDate fecha) {
        obtener(idInstrumento);
        LocalDate fechaEvaluada = fecha == null ? LocalDate.now() : fecha;
        Calibracion ultima = calibracionRepository
                .findFirstByIdInstrumentoAndFechaCalibracionLessThanEqualAndFechaVencimientoGreaterThanEqualOrderByFechaCalibracionDesc(
                        idInstrumento,
                        fechaEvaluada,
                        fechaEvaluada
                )
                .orElse(null);
        if (ultima == null) {
            return new VigenciaResponse(idInstrumento, false, fechaEvaluada, null, null, null);
        }
        boolean vigente = !ultima.getFechaVencimiento().isBefore(fechaEvaluada);
        return new VigenciaResponse(
                idInstrumento,
                vigente,
                fechaEvaluada,
                ultima.getFechaCalibracion(),
                ultima.getFechaVencimiento(),
                ultima.getCertificado()
        );
    }

    private void validar(Instrumento datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del instrumento es obligatorio");
        }
        if (datos.getNumeroSerie() == null || datos.getNumeroSerie().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de serie es obligatorio");
        }
        if (datos.getTipoPrueba() == null || datos.getTipoPrueba().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de prueba es obligatorio");
        }
    }
}
