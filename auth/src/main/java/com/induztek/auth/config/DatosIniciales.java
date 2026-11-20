package com.induztek.auth.config;

import com.induztek.auth.model.Credencial;
import com.induztek.auth.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final CredencialRepository credencialRepository;
    private final PasswordEncoder cifradorContrasenas;

    @Override
    public void run(String... args) {
        if (credencialRepository.count() == 0) {
            credencialRepository.saveAll(List.of(
                    credencial(1L, "tecnico@gmail.com"),
                    credencial(2L, "cliente@gmail.com"),
                    credencial(3L, "cliente2@gmail.com"),
                    credencial(4L, "admin@gmail.com")
            ));
        }
    }

    private Credencial credencial(Long usuarioId, String correo) {
        return Credencial.builder()
                .usuarioId(usuarioId)
                .correo(correo)
                .contrasena(cifradorContrasenas.encode("123456"))
                .primerIngreso(false)
                .build();
    }
}