package com.induztek.equipos.service;

import com.induztek.equipos.dto.UbicacionRequest;
import com.induztek.equipos.client.UsuarioClient;
import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.model.HistorialCambio;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import com.induztek.equipos.repository.AsignacionTecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository repository;
    private final InstalacionRepository instalacionRepository;
    private final AsignacionTecnicoRepository asignacionRepository;
    private final HistorialCambioService historialService;
    private final UsuarioClient usuarioClient;
    private final AccesoEquiposService accesoService;
    private final ActorContext actorContext;

    public List<Equipo> listarTodos() {
        return accesoService.equiposVisibles();
    }

    public List<Equipo> listarPorCliente(Long idCliente) {
        if (Long.valueOf(2L).equals(actorContext.rolId())
                && !idCliente.equals(actorContext.usuarioId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No puedes consultar equipos de otro cliente"
            );
        }
        List<Equipo> resultado = repository.findByIdCliente(idCliente);
        return filtrarVisibles(resultado);
    }

    public List<Equipo> listarPorTecnico(Long idTecnico) {
        if (!actorContext.esAdministrador()
                && (!Long.valueOf(1L).equals(actorContext.rolId())
                || !idTecnico.equals(actorContext.usuarioId()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "No puedes consultar equipos de otro técnico"
            );
        }
        return asignacionRepository.findByIdTecnico(idTecnico).stream()
                .flatMap(asignacion -> repository.findByIdInstalacion(asignacion.getIdInstalacion()).stream())
                .toList();
    }

    public List<Equipo> listarPorInstalacion(Long idInstalacion) {
        accesoService.validarInstalacion(idInstalacion);
        return repository.findByIdInstalacion(idInstalacion);
    }

    public Optional<Equipo> buscarPorId(Long id) {
        Optional<Equipo> equipo = repository.findById(id);
        equipo.ifPresent(accesoService::validarEquipo);
        return equipo;
    }

    @Transactional
    public Equipo guardar(Equipo equipo) {
        validarEquipo(equipo);
        validarInstalacion(equipo);
        sincronizarCodigoYTag(equipo);
        if (equipo.getIdInstalacion() != null &&
                        repository.existsByIdInstalacionAndTagIgnoreCase(
                                equipo.getIdInstalacion(),
                                equipo.getTag()
                        )
        ) {
            throw new IllegalArgumentException("Ya existe un equipo con ese código o TAG");
        }
        Equipo guardado = repository.save(equipo);
        historialService.registrar("EQUIPO", guardado.getIdEquipo(), "ALTA", null, guardado);
        return guardado;
    }

    @Transactional
    public Optional<Equipo> actualizar(Long id, Equipo datos) {
        buscarPorId(id);
        return repository.findById(id).map(existente -> {
            Equipo antes = copiar(existente);
            validarEquipo(datos);
            validarInstalacion(datos);
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
            Equipo guardado = repository.save(existente);
            historialService.registrar("EQUIPO", id, "CAMBIO", antes, guardado);
            return guardado;
        });
    }

    @Transactional
    public Optional<Equipo> actualizarUbicacion(Long id, UbicacionRequest req) {
        Optional<Equipo> equipoExistente = buscarPorId(id);
        Equipo antes = equipoExistente.map(this::copiar).orElse(null);
        return repository.findById(id).map(existente -> {
            existente.setDireccion(req.getDireccion());
            existente.setLatitud(req.getLatitud());
            existente.setLongitud(req.getLongitud());
            Equipo guardado = repository.save(existente);
            historialService.registrar("EQUIPO", id, "CAMBIO_UBICACION", antes, guardado);
            return guardado;
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        Optional<Equipo> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return false;
        }
        accesoService.validarEquipo(encontrado.get());
        historialService.registrar("EQUIPO", id, "BAJA", encontrado.get(), null);
        repository.deleteById(id);
        return true;
    }

    public List<HistorialCambio> historial(Long id) {
        Optional<Equipo> equipo = repository.findById(id);
        if (equipo.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Equipo no encontrado"
            );
        }
        accesoService.validarEquipo(equipo.get());
        return historialService.listar("EQUIPO", id);
    }

    private void validarInstalacion(Equipo equipo) {
        if (equipo.getIdInstalacion() == null) {
            throw new IllegalArgumentException("El ID de la instalación es obligatorio");
        }
        com.induztek.equipos.model.Instalacion instalacion = instalacionRepository.findById(equipo.getIdInstalacion())
                .filter(com.induztek.equipos.model.Instalacion::isActiva)
                .orElseThrow(() -> new IllegalArgumentException("La instalación no existe o está inactiva"));
        accesoService.validarInstalacion(equipo.getIdInstalacion());
        if (!instalacion.getIdUsuarioCliente().equals(equipo.getIdCliente())) {
            throw new IllegalArgumentException("El cliente del equipo no coincide con la instalación");
        }
        UsuarioClient.UsuarioReferencia cliente = usuarioClient.obtener(equipo.getIdCliente());
        if (cliente == null || !cliente.activo() || !Long.valueOf(2L).equals(cliente.rolId())) {
            throw new IllegalArgumentException("El cliente asociado al equipo no existe o no tiene rol Cliente");
        }
    }

    private List<Equipo> filtrarVisibles(List<Equipo> equipos) {
        if (actorContext.esAdministrador()) {
            return equipos;
        }
        List<Long> instalacionesVisibles = accesoService.instalacionesVisibles();
        return equipos.stream()
                .filter(equipo -> instalacionesVisibles.contains(equipo.getIdInstalacion()))
                .toList();
    }

    private Equipo copiar(Equipo equipo) {
        Equipo copia = new Equipo();
        copia.setIdEquipo(equipo.getIdEquipo());
        copia.setCodigo(equipo.getCodigo());
        copia.setTag(equipo.getTag());
        copia.setTipo(equipo.getTipo());
        copia.setClasificacion(equipo.getClasificacion());
        copia.setMarca(equipo.getMarca());
        copia.setModelo(equipo.getModelo());
        copia.setNumeroSerie(equipo.getNumeroSerie());
        copia.setPotenciaKva(equipo.getPotenciaKva());
        copia.setTensiones(equipo.getTensiones());
        copia.setAnoFabricacion(equipo.getAnoFabricacion());
        copia.setPosicionTap(equipo.getPosicionTap());
        copia.setGrupoConexion(equipo.getGrupoConexion());
        copia.setResistenciaContactoMaxUohm(equipo.getResistenciaContactoMaxUohm());
        copia.setInstalacion(equipo.getInstalacion());
        copia.setIdInstalacion(equipo.getIdInstalacion());
        copia.setDireccion(equipo.getDireccion());
        copia.setLatitud(equipo.getLatitud());
        copia.setLongitud(equipo.getLongitud());
        copia.setIdCliente(equipo.getIdCliente());
        return copia;
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