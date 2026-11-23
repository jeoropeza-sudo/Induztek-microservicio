package com.induztek.auth.repository;

import com.induztek.auth.model.IntentoAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntentoAccesoRepository extends JpaRepository<IntentoAcceso, Long> {

    Optional<IntentoAcceso> findByCorreo(String correo);
}
