package com.metrilab.intranet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteDTO {

    private Long id;
    private String razonSocial;
    private String nit;
    private String email;
    private String sedes;

}
