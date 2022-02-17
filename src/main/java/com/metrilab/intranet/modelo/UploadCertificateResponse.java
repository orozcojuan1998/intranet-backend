package com.metrilab.intranet.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadCertificateResponse {

    private String key;
    private String publicUrl;
    private String error;

    public UploadCertificateResponse(){}
}
