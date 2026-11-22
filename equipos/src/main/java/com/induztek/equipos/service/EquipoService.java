package com.induztek.equipos.service;

import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository repository;

    public List<Equipo> listarTodos() {
        return repository.findAll();
    }

    public List<Equipo> listarPorCliente(Long idCliente) {
        return repository.findByIdCliente(idCliente);
    }

    public List<Equipo> listarPorInstalacion(Long idInstalacion) {
        return repository.findByIdInstalacion(idInstalacion);
    }

    public Optional<Equipo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Equipo guardar(Equipo equipo) {
        validarEquipo(equipo);
        sincronizarCodigoYTag(equipo);
        if (equipo.getIdInstalacion() != null &&
                        repository.existsByIdInstalacionAndTagIgnoreCase(
                                equipo.getIdInstalacion(),
                                equipo.getTag()
                        )
        ) {
            throw new IllegalArgumentException("Ya existe un equipo con ese código o TAG");
        }
        return repository.save(equipo);
    }

    public Optional<Equipo> actualizar(Long id, Equipo datos) {
        return repository.findById(id).map(existente -> {
            validarEquipo(datos);
            sincronizarCodigoYTag(datos);
            if (datos.getIdInstalacion() != null &&
                            repository.existsByIdInstalacionAndTagIgnoreCaseAndIdEquipoNot(
                                    datos.getIdInstalacion(),
                                    datos.getTag(),
                                    id
                            )
            ) {
                throw new IllegalArgumentException("Ya existe un equipo con ese código o TAG");
            }
            existente.setCodigo(datos.getCodigo());
            existente.setTag(datos.getTag());
            existente.setTipo(datos.getTipo());
            existente.setClasificacion(datos.getClasificacion());
            existente.setMarca(datos.getMarca());
            existente.setModelo(datos.getModelo());
            existente.setNumeroSerie(datos.getNumeroSerie());
            existente.setPotenciaKva(datos.getPotenciaKva());
            existente.setTensiones(datos.getTensiones());
            existente.setAnoFabricacion(datos.getAnoFabricacion());
            existente.setPosicionTap(datos.getPosicionTap());
            existente.setGrupoConexion(datos.getGrupoConexion());
            existente.setResistenciaContactoMaxUohm(datos.getResistenciaContactoMaxUohm());
            existente.setInstalacion(datos.getInstalacion());
            existente.setIdInstalacion(datos.getIdInstalacion());
            existente.setDireccion(datos.getDireccion());
            existente.setLatitud(datos.getLatitud());
            existente.setLongitud(datos.getLongitud());
            existente.setIdCliente(datos.getIdCliente());
            return repository.save(existente);
        });
    }

    public Optional<Equipo> actualizarUbicacion(Long id, UbicacionRequest req) {
        return repository.findById(id).map(existente -> {
            existente.setDireccion(req.getDireccion());
            existente.setLatitud(req.getLatitud());
            existente.setLongitud(req.getLongitud());
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private void sincronizarCodigoYTag(Equipo equipo) {
        if (vacio(equipo.getTag()) && !vacio(equipo.getCodigo())) {
            equipo.setTag(equipo.getCodigo().trim());
        }
        if (vacio(equipo.getCodigo()) && !vacio(equipo.getTag())) {
            equipo.setCodigo(equipo.getTag().trim());
        }
        if (!vacio(equipo.getTag())) {
            equipo.setTag(equipo.getTag().trim());
        }
        if (!vacio(equipo.getCodigo())) {
            equipo.setCodigo(equipo.getCodigo().trim());
        }
    }

    private void validarEquipo(Equipo equipo) {
        sincronizarCodigoYTag(equipo);
        if (vacio(equipo.getCodigo()) || vacio(equipo.getTag())) {
            throw new IllegalArgumentException("El código y el TAG del equipo son obligatorios");
        }
        if (vacio(equipo.getTipo())) {
            throw new IllegalArgumentException("El tipo de equipo es obligatorio");
        }
        if (vacio(equipo.getMarca())) {
            throw new IllegalArgumentException("La marca del equipo es obligatoria");
        }
        if (vacio(equipo.getModelo())) {
            throw new IllegalArgumentException("El modelo del equipo es obligatorio");
        }
        if (vacio(equipo.getNumeroSerie())) {
            throw new IllegalArgumentException("El número de serie del equipo es obligatorio");
        }
        if (equipo.getIdInstalacion() == null) {
            throw new IllegalArgumentException("El ID de la instalación es obligatorio");
        }
        if (equipo.getIdCliente() == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        if (!vacio(equipo.getAnoFabricacion())) {
            try {
                int ano = Integer.parseInt(equipo.getAnoFabricacion().trim());
                int anoActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (ano < 1900 || ano > anoActual) {
                    throw new IllegalArgumentException("El año de fabricación no es válido");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El año de fabricación debe tener cuatro dígitos");
            }
        }
        if ("Transformador".equalsIgnoreCase(equipo.getTipo())) {
            if (equipo.getPotenciaKva() == null || equipo.getPotenciaKva() <= 0) {
                throw new IllegalArgumentException("La potencia del transformador debe ser mayor que cero");
            }
            if (vacio(equipo.getTensiones()) ||
                    !equipo.getTensiones().matches(".*\\d+(?:[.,]\\d+)?\\s*/\\s*\\d+(?:[.,]\\d+)?.*")) {
                throw new IllegalArgumentException("Ingresa las tensiones AT/BT separadas por /");
            }
            if (vacio(equipo.getPosicionTap())) {
                throw new IllegalArgumentException("La posición TAP es obligatoria");
            }
            if (vacio(equipo.getGrupoConexion())) {
                throw new IllegalArgumentException("El grupo de conexión es obligatorio");
            }
        }
        if ("Celda MT".equalsIgnoreCase(equipo.getTipo()) ||
                "Interruptor".equalsIgnoreCase(equipo.getTipo())) {
            if (equipo.getResistenciaContactoMaxUohm() == null ||
                    equipo.getResistenciaContactoMaxUohm() <= 0) {
                throw new IllegalArgumentException("La resistencia máxima de contacto debe ser mayor que cero");
            }
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}