package com.induztek.usuarios.config;

import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.saveAll(List.of(
                    usuario("Matías", "Rojas", "16482913-8", "tecnico@gmail.com", "+56911112222", 1L, 1L),
                    usuario("Pedro", "Salas", "14756201-2", "cliente@gmail.com", "+56933334444", 2L, 2L),
                    usuario("Daniela", "Vera", "17093845-3", "cliente2@gmail.com", "+56955556666", 3L, 2L),
                    usuario("Admin", "Induztek", "13570246-3", "admin@gmail.com", "+56977778888", 1L, 3L)
            ));
        }
    }

    private Usuario usuario(String nombre, String apellido, String rut, String correo,
                            String telefono, Long empresaId, Long rolId) {
        return Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .rut(rut)
                .correo(correo)
                .telefono(telefono)
                .empresaId(empresaId)
                .rolId(rolId)
                .build();
    }
}