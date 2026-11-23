package com.induztek.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitenteConfigurado;

    @Value("${app.mail.from}")
    private String remitentePredeterminado;

    public void enviarContrasenaTemporal(String destinatario, String contrasena) {
        enviar(
                destinatario,
                "Acceso a Induztek",
                "Tu cuenta fue creada. Tu contraseña temporal es: " + contrasena
                        + "\nDebes cambiarla al iniciar sesión."
        );
    }

    public void enviarCodigoRestablecimiento(String destinatario, String codigo) {
        enviar(
                destinatario,
                "Recuperación de contraseña Induztek",
                "Tu código para restablecer la contraseña es: " + codigo
                        + "\nEl código vence en 15 minutos."
        );
    }

    private void enviar(String destinatario, String asunto, String contenido) {
        String remitente = remitenteConfigurado == null || remitenteConfigurado.isBlank()
                ? remitentePredeterminado
                : remitenteConfigurado;
        if (remitente == null || remitente.isBlank()) {
            throw new IllegalStateException("Configura MAIL_FROM o spring.mail.username para enviar correo");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }
}
