package com.induztek.usuarios.repository;

import com.induztek.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrderByNombreAsc(String nombre, String rut);

    boolean existsByRut(String rut);

    boolean existsByRutAndIdNot(String rut, Long id);
}