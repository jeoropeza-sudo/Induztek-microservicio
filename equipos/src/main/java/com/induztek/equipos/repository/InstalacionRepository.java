package com.induztek.equipos.repository;

import com.induztek.equipos.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstalacionRepository extends JpaRepository<Instalacion, Long> {
    List<Instalacion> findByIdEmpresaAndActivaTrueOrderByNombreAsc(Long idEmpresa);
    List<Instalacion> findByIdUsuarioClienteAndActivaTrueOrderByNombreAsc(Long idUsuarioCliente);
    Optional<Instalacion> findByIdEmpresaAndNombreIgnoreCase(Long idEmpresa, String nombre);
    List<Instalacion> findAllByActivaTrueOrderByNombreAsc();
}
