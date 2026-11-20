package com.induztek.auth.repository;

import com.induztek.auth.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByCorreo(String correo);

    Optional<Credencial> findByUsuarioId(Long usuarioId);

    boolean existsByCorreo(String correo);

    boolean existsByUsuarioId(Long usuarioId);
}