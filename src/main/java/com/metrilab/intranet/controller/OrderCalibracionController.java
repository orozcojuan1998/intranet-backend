package com.metrilab.intranet.controller;

import com.metrilab.intranet.modelo.OrdenCalibracion;
import com.metrilab.intranet.modelo.OrdenCalibrationCertificateResponse;
import com.metrilab.intranet.modelo.OrderCalibracionResponse;
import com.metrilab.intranet.service.OrdenCalibracionService;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/orden-calibracion")
public class OrderCalibracionController {

    private final List<String> CERT_REVIEW = List.of("REVISION", "RECHAZADO_TECNICA", "NUEVA_REVISION");
    private final List<String> CERT_REVIEW_CORRECTION = List.of("NECESITA_CORRECCION");
    private final List<String> CERT_APPROVED = List.of("REVISADO");

    @Autowired
    OrdenCalibracionService ordenCalibracionService;

    @GetMapping(value = "/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderCalibracionResponse>> ordenesRevision() {
        try {
            log.info("Trying to get the desired certificate");
            return ResponseEntity.status(HttpStatus.OK).body(ordenCalibracionService.getOrdersWithState(CERT_REVIEW));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @GetMapping(value = "/approved", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderCalibracionResponse>> ordenesAprobadas() {
        try {
            log.info("Trying to get the desired certificate");
            return ResponseEntity.status(HttpStatus.OK).body(ordenCalibracionService.getOrdersWithState(CERT_APPROVED));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @GetMapping(value = "/rejected", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderCalibracionResponse>> ordenesNecesitanCorrection() {
        try {
            log.info("Trying to get the desired certificate");
            return ResponseEntity.status(HttpStatus.OK).body(ordenCalibracionService.getOrdersWithState(CERT_REVIEW_CORRECTION));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @GetMapping(value = "/review/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrdenCalibracion> ordenRevision(@PathVariable @NotNull String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Cache-Control","no-store");
        try {
            Optional<OrdenCalibracion> ordenCalibracion = ordenCalibracionService.getOrdenById(id);
            log.info("Obteniendo la orden con id indicado");
            if (ordenCalibracion.isPresent()) {
                log.info("Se ha encontrado la orden específicada... retornando respuesta");
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(ordenCalibracion.get());
            } else {
                log.info("No se encontró la orden especificada");
                return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error("Se encontro un error al tratar de traer la informacion de la orden con id: " + id);
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping()
    public ResponseEntity<OrdenCalibracion> getOrdenRMA(@RequestParam(value = "rma") @NotNull String rma) {
        OrdenCalibracion ordenCalibracion = new OrdenCalibracion();
        try {
            log.info("Trying to get the desired certificate");
            ordenCalibracion = ordenCalibracionService.getOrdenByRMA(rma);
            return ResponseEntity.status(HttpStatus.OK).body(ordenCalibracion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ordenCalibracion);
        }
    }

    @PostMapping(value = "/new")
    public ResponseEntity<OrdenCalibrationCertificateResponse> createNewRMA(@RequestParam("file") MultipartFile file, @RequestParam String rma, @RequestParam(required = false) String nit, @RequestParam String nombreEquipo, @RequestParam String marca, @RequestParam String modelo, @RequestParam String serial, @RequestParam String codigo) {
        try {
            log.info("Starting to read the data and upload the certificate");
            OrdenCalibracion ordenCalibracion = ordenCalibracionService.createRMAForCertificate(file, rma, nit, nombreEquipo, marca, modelo, serial, codigo);
            OrdenCalibrationCertificateResponse ordenResponse = OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(ordenCalibracion)
                    .message("Created")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(ordenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(null)
                    .message("Error trying to upload the certificate or setting the data")
                    .build());
        }
    }

    @PostMapping(value = "/edit/updateCertificate")
    public ResponseEntity<OrdenCalibrationCertificateResponse> createNewRMA(@RequestParam("file") MultipartFile file, @RequestParam String ordenId, @RequestParam String estado) {
        try {
            log.info("Starting to read the data and upload the certificate");
            OrdenCalibracion ordenCalibracion = ordenCalibracionService.updateCertificate(file, ordenId, estado);
            OrdenCalibrationCertificateResponse ordenResponse = OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(ordenCalibracion)
                    .message("Updated")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(ordenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(null)
                    .message("Error trying to upload the certificate or setting the data")
                    .build());
        }
    }

    @PostMapping(value = "/review/update")
    public ResponseEntity<OrdenCalibrationCertificateResponse> updateRMA(@RequestParam String idOrdenCalibracion, @RequestParam String estado, @RequestParam String observaciones, @RequestParam(required = false) String approvedBy, @RequestParam(required = false) String emailApprover) {
        OrdenCalibracion ordenCalibracion;
        try {
            log.info("Starting to read the data and update the order");

            ordenCalibracion = ordenCalibracionService.updateRMA(idOrdenCalibracion, estado, observaciones, approvedBy, emailApprover);
            OrdenCalibrationCertificateResponse ordenResponse = OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(ordenCalibracion)
                    .message("Updated")
                    .build();
            log.info("Se ha actualizado correctamente la orden registrada");
            return ResponseEntity.status(HttpStatus.OK).body(ordenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(OrdenCalibrationCertificateResponse.builder()
                    .ordenCalibracion(null)
                    .message(e.getMessage())
                    .build());
        }
    }
}
