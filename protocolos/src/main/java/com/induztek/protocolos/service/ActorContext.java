package com.induztek.protocolos.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ActorContext {

    public Long usuarioId() {
        Object valor = atributo("actorId");
        return valor instanceof Long ? (Long) valor : null;
    }

    public Long rolId() {
        Object valor = atributo("actorRoleId");
        return valor instanceof Long ? (Long) valor : null;
    }

    public boolean esAdministrador() {
        Long rol = rolId();
        return Long.valueOf(3L).equals(rol) || Long.valueOf(4L).equals(rol);
    }

    private Object atributo(String nombre) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos)) {
            return null;
        }
        HttpServletRequest request = atributos.getRequest();
        return request.getAttribute(nombre);
    }
}
