package com.metrilab.intranet.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrdenCalibrationCertificateResponse {

    private OrdenCalibracion ordenCalibracion;
    private String message;
    private String error;

    public OrdenCalibrationCertificateResponse(){}
}
