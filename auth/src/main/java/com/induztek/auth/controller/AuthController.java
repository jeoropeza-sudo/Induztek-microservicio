package com.induztek.auth.controller;

import com.induztek.auth.dto.DatosCambioContrasena;
import com.induztek.auth.dto.DatosCredencial;
import com.induztek.auth.dto.DatosLogin;
import com.induztek.auth.dto.DatosOlvide;
import com.induztek.auth.dto.DatosRestablecer;
import com.induztek.auth.dto.RespuestaLogin;
import com.induztek.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public RespuestaLogin iniciarSesion(@RequestBody DatosLogin datos) {
        return authService.iniciarSesion(datos);
    }

    @PostMapping("/credenciales")
    @ResponseStatus(HttpStatus.CREATED)
    public void crearCredencial(@RequestBody DatosCredencial datos) {
        authService.crearCredencial(datos);
    }

    @PostMapping("/configurar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void configurar(@RequestBody DatosCambioContrasena datos) {
        authService.configurar(datos);
    }

    @PostMapping("/olvide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void olvide(@RequestBody DatosOlvide datos) {
        authService.olvide(datos);
    }

    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@RequestBody DatosRestablecer datos) {
        authService.restablecer(datos);
    }
}