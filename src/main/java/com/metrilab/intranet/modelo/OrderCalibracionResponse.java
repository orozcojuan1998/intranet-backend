package com.metrilab.intranet.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCalibracionResponse {

    private Long id;
    private String rma;
    private String idCertificado;
    private String nombreEquipo;
    private String estado;

    public OrderCalibracionResponse(){}
}
