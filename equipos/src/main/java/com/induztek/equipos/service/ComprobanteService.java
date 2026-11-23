package com.induztek.equipos.service;

import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.model.Visita;
import com.induztek.equipos.model.VisitaEquipo;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.repository.InstalacionRepository;
import com.induztek.equipos.repository.VisitaEquipoRepository;
import com.induztek.equipos.repository.VisitaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final VisitaRepository visitaRepository;
    private final VisitaEquipoRepository visitaEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final InstalacionRepository instalacionRepository;
    private final AccesoEquiposService accesoService;
    private final ActorContext actorContext;

    public byte[] generar(Long idVisita) {
        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visita no encontrada"));
        accesoService.validarInstalacion(visita.getIdInstalacion());
        if (Long.valueOf(1L).equals(actorContext.rolId())
                && !actorContext.usuarioId().equals(visita.getIdTecnico())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La visita pertenece a otro técnico");
        }
        if (!"CERRADA".equals(visita.getEstado()) && !"REGISTRADA".equals(visita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El comprobante solo está disponible para visitas cerradas");
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document();
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph("Comprobante de visita / Orden de trabajo"));
            documento.add(new Paragraph("OT: " + visita.getNumeroOt()));
            documento.add(new Paragraph("Fecha: " + visita.getFechaVisita().format(FECHA)));
            documento.add(new Paragraph("Estado: " + visita.getEstado()));
            documento.add(new Paragraph("Técnico ID: " + visita.getIdTecnico()));
            documento.add(new Paragraph("Tipo de visita: " + visita.getTipoVisita()));
            documento.add(new Paragraph("Temperatura: " + valor(visita.getTemperaturaC()) + " °C"));
            documento.add(new Paragraph("Humedad: " + valor(visita.getHumedadPct()) + " %"));
            documento.add(new Paragraph(" "));

            Instalacion instalacion = instalacionRepository.findById(visita.getIdInstalacion()).orElse(null);
            documento.add(new Paragraph("Instalación: " + (instalacion == null ? visita.getIdInstalacion() : instalacion.getNombre())));
            documento.add(new Paragraph("Dirección: " + (instalacion == null ? "" : valor(instalacion.getDireccion()))));
            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("Equipos atendidos"));

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.addCell(new Phrase("TAG"));
            tabla.addCell(new Phrase("Tipo"));
            tabla.addCell(new Phrase("Marca / modelo"));
            tabla.addCell(new Phrase("Estado"));

            List<VisitaEquipo> relaciones = visitaEquipoRepository.findByIdVisitaOrderByOrdenAsc(idVisita);
            for (VisitaEquipo relacion : relaciones) {
                Equipo equipo = equipoRepository.findById(relacion.getIdEquipo()).orElse(null);
                tabla.addCell(equipo == null ? String.valueOf(relacion.getIdEquipo()) : valor(equipo.getTag()));
                tabla.addCell(equipo == null ? "" : valor(equipo.getTipo()));
                tabla.addCell(equipo == null ? "" : valor(equipo.getMarca()) + " " + valor(equipo.getModelo()));
                tabla.addCell(valor(relacion.getEstado()));
            }
            documento.add(tabla);
            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("Receptor: " + valor(visita.getReceptorNombre())));
            documento.add(new Paragraph("RUT: " + valor(visita.getReceptorRut())));
            documento.add(new Paragraph("Cargo: " + valor(visita.getReceptorCargo())));
            documento.add(new Paragraph("Descripción: " + valor(visita.getDescripcionMantenimiento())));
            documento.add(new Paragraph("Motivo no realizado: " + valor(visita.getMotivoNoRealizada())));

            if (visita.getFirmaReceptor() != null) {
                Image firma = Image.getInstance(visita.getFirmaReceptor());
                firma.scaleToFit(220, 90);
                documento.add(new Paragraph("Firma de recepción:"));
                documento.add(firma);
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el comprobante PDF", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    private String valor(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }
}
