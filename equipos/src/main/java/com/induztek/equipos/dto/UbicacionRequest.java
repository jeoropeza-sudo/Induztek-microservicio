package com.induztek.equipos.dto;

import lombok.Data;

@Data
public class UbicacionRequest {
    private String direccion;
    private Double latitud;
    private Double longitud;
}