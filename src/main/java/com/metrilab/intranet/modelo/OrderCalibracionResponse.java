package com.metrilab.intranet.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderCalibracionResponse {

    private Long id;
    private String rma;
    private String idCertificado;
    private String nombreEquipo;
    private String estado;
    private LocalDate fechaAprobacion;

    public OrderCalibracionResponse(){}

    public OrderCalibracionResponse(Long id, String rma, String idCertificado, String nombreEquipo, String estado) {
        this.id = id;
        this.rma = rma;
        this.idCertificado = idCertificado;
        this.nombreEquipo = nombreEquipo;
        this.estado = estado;
    }
}
