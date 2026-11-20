package com.induztek.equipos.service;

import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository repository;

    public List<Equipo> listarTodos() {
        return repository.findAll();
    }

    public List<Equipo> listarPorCliente(Long idCliente) {
        return repository.findByIdCliente(idCliente);
    }

    public Optional<Equipo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Equipo guardar(Equipo equipo) {
        if (repository.existsByCodigo(equipo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un equipo con el código: " + equipo.getCodigo());
        }
        return repository.save(equipo);
    }

    public Optional<Equipo> actualizar(Long id, Equipo datos) {
        return repository.findById(id).map(existente -> {
            existente.setCodigo(datos.getCodigo());
            existente.setTipo(datos.getTipo());
            existente.setInstalacion(datos.getInstalacion());
            existente.setDireccion(datos.getDireccion());
            existente.setLatitud(datos.getLatitud());
            existente.setLongitud(datos.getLongitud());
            existente.setIdCliente(datos.getIdCliente());
            return repository.save(existente);
        });
    }

    public Optional<Equipo> actualizarUbicacion(Long id, UbicacionRequest req) {
        return repository.findById(id).map(existente -> {
            existente.setDireccion(req.getDireccion());
            existente.setLatitud(req.getLatitud());
            existente.setLongitud(req.getLongitud());
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}