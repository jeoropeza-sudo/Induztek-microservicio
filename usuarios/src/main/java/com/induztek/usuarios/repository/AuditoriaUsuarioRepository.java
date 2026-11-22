package com.induztek.usuarios.repository;

import com.induztek.usuarios.model.AuditoriaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaUsuarioRepository extends JpaRepository<AuditoriaUsuario, Long> {
    List<AuditoriaUsuario> findByUsuarioAfectadoIdOrderByFechaDesc(Long usuarioAfectadoId);
}
