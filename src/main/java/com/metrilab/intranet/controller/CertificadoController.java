package com.metrilab.intranet.controller;

import com.metrilab.intranet.modelo.Certificado;
import com.metrilab.intranet.service.CertificadoService;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/certificado")
public class CertificadoController {

    @Autowired
    CertificadoService certificadoService;

    @GetMapping(path ="/{id}")
    public ResponseEntity<Certificado> getCertificado(@PathVariable @NotNull String id) {
        Certificado certificado = new Certificado();
        try {
            log.info("Trying to get the desired certificate");
            certificado = certificadoService.getCertificado(id);
            return ResponseEntity.status(HttpStatus.OK).body(certificado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(certificado);
        }
    }
}
