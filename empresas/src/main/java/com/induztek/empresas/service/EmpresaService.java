package com.induztek.empresas.service;

import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<Empresa> listar() {
        return empresaRepository.findAllByOrderByNombreAsc();
    }

    public Empresa obtener(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
    }

    public Empresa crear(DatosEmpresa datos) {
        validarDatos(datos);

        if (empresaRepository.existsByNombreIgnoreCase(datos.nombre().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese nombre");
        }

        Empresa nueva = Empresa.builder()
                .nombre(datos.nombre().trim())
                .direccion(datos.direccion().trim())
                .build();
        return empresaRepository.save(nueva);
    }

    public Empresa actualizar(Long id, DatosEmpresa datos) {
        Empresa empresa = obtener(id);

        validarDatos(datos);

        if (empresaRepository.existsByNombreIgnoreCaseAndIdNot(datos.nombre().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese nombre");
        }

        empresa.setNombre(datos.nombre().trim());
        empresa.setDireccion(datos.direccion().trim());
        return empresaRepository.save(empresa);
    }

    private void validarDatos(DatosEmpresa datos) {
        if (vacio(datos.nombre()) || vacio(datos.direccion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y la dirección son obligatorios");
        }
        if (datos.nombre().trim().length() < 3 || datos.nombre().trim().length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre debe tener entre 3 y 60 caracteres");
        }
        if (datos.direccion().trim().length() < 5 || datos.direccion().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección debe tener entre 5 y 100 caracteres");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}