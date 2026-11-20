package com.induztek.auth.repository;

import com.induztek.auth.model.TokenRestablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRestablecimientoRepository extends JpaRepository<TokenRestablecimiento, Long> {

    List<TokenRestablecimiento> findByUsuarioIdAndUsadoFalse(Long usuarioId);
}