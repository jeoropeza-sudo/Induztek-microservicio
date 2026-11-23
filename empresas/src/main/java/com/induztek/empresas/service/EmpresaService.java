package com.induztek.empresas.service;

import com.induztek.empresas.dto.DatosEmpresa;
import com.induztek.empresas.client.UsuarioClient;
import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioClient usuarioClient;

    public List<Empresa> listar(Long actorId, Long actorRoleId) {
        if (Long.valueOf(3L).equals(actorRoleId) || Long.valueOf(4L).equals(actorRoleId)) {
            return empresaRepository.findAllByOrderByNombreAsc();
        }
        UsuarioClient.UsuarioReferencia actor = usuarioClient.obtener(actorId);
        if (actor == null || !actor.activo() || actor.empresaId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se pudo determinar la empresa del usuario");
        }
        return List.of(obtener(actor.empresaId()));
    }

    public Empresa obtener(Long id, Long actorId, Long actorRoleId) {
        Empresa empresa = obtener(id);
        if (Long.valueOf(3L).equals(actorRoleId) || Long.valueOf(4L).equals(actorRoleId)) {
            return empresa;
        }
        UsuarioClient.UsuarioReferencia actor = usuarioClient.obtener(actorId);
        if (actor == null || !actor.activo() || !id.equals(actor.empresaId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar otra empresa");
        }
        return empresa;
    }

    private Empresa obtener(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
    }

    public Empresa crear(DatosEmpresa datos) {
        validarDatos(datos);

        if (empresaRepository.existsByNombreIgnoreCase(datos.nombre().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese nombre");
        }
        if (empresaRepository.existsByRutIgnoreCase(datos.rut().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese RUT");
        }

        Empresa nueva = Empresa.builder()
                .nombre(datos.nombre().trim())
                .rut(datos.rut().trim())
                .giro(datos.giro().trim())
                .direccion(datos.direccion().trim())
                .contactoNombre(datos.contactoNombre().trim())
                .contactoCorreo(datos.contactoCorreo().trim().toLowerCase())
                .contactoTelefono(datos.contactoTelefono().trim())
                .build();
        return empresaRepository.save(nueva);
    }

    public Empresa actualizar(Long id, DatosEmpresa datos) {
        Empresa empresa = obtener(id);

        validarDatos(datos);

        if (empresaRepository.existsByNombreIgnoreCaseAndIdNot(datos.nombre().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese nombre");
        }
        if (empresaRepository.existsByRutIgnoreCaseAndIdNot(datos.rut().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una empresa con ese RUT");
        }

        empresa.setNombre(datos.nombre().trim());
        empresa.setRut(datos.rut().trim());
        empresa.setGiro(datos.giro().trim());
        empresa.setDireccion(datos.direccion().trim());
        empresa.setContactoNombre(datos.contactoNombre().trim());
        empresa.setContactoCorreo(datos.contactoCorreo().trim().toLowerCase());
        empresa.setContactoTelefono(datos.contactoTelefono().trim());
        return empresaRepository.save(empresa);
    }

    private void validarDatos(DatosEmpresa datos) {
        if (vacio(datos.nombre()) || vacio(datos.rut()) || vacio(datos.giro()) ||
                vacio(datos.direccion()) || vacio(datos.contactoNombre()) ||
                vacio(datos.contactoCorreo()) || vacio(datos.contactoTelefono())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completa todos los datos de la empresa");
        }
        if (datos.nombre().trim().length() < 3 || datos.nombre().trim().length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre debe tener entre 3 y 60 caracteres");
        }
        if (datos.direccion().trim().length() < 5 || datos.direccion().trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La dirección debe tener entre 5 y 100 caracteres");
        }
        if (!datos.contactoCorreo().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo de contacto válido");
        }
        if (!validarRut(datos.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un RUT de empresa válido");
        }
    }

    private boolean validarRut(String rut) {
        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();
        if (!limpio.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digitoIngresado = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int indice = cuerpo.length() - 1; indice >= 0; indice--) {
            suma += (cuerpo.charAt(indice) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }

        int resto = 11 - (suma % 11);
        char digitoCalculado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return digitoCalculado == digitoIngresado;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}