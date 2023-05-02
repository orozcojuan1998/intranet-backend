package com.metrilab.intranet.service;

import com.metrilab.intranet.modelo.*;
import com.metrilab.intranet.repository.CertificadoRepository;
import com.metrilab.intranet.repository.EquipoRepository;
import com.metrilab.intranet.repository.OrdenCalibracionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdenCalibracionService {

    private static final String REVISION_ESTADO = "REVISION";

    private final OrdenCalibracionRepository ordenRepository;
    private final CertificadoRepository certificadoRepository;
    private final EquipoRepository equipoRepository;
    private final FilesStorageService filesService;

    @Autowired
    public OrdenCalibracionService(OrdenCalibracionRepository ordenRepository, CertificadoRepository certificadoRepository, EquipoRepository equipoRepository, FilesStorageService filesService) {
        this.ordenRepository = ordenRepository;
        this.certificadoRepository = certificadoRepository;
        this.equipoRepository = equipoRepository;
        this.filesService = filesService;
    }

    public Optional<OrdenCalibracion> getOrdenById(String id) {
        ordenRepository.findById(Long.valueOf(id));
        return ordenRepository.findById(Long.valueOf(id));
    }

    public OrdenCalibracion getOrdenByRMA(String rma) {
        log.info(ordenRepository.findByRma(rma).get().toString());
        if (ordenRepository.obtenerOrderPorRMA(rma).isPresent()) {
            return ordenRepository.obtenerOrderPorRMA(rma).get();
        }
        return ordenRepository.obtenerOrderPorRMA(rma).orElse(new OrdenCalibracion());
    }

    @Transactional
    public OrdenCalibracion updateRMA(String id, String estado, String observaciones, String approvedBy, String emailApprover) {
        Optional<OrdenCalibracion> orden = ordenRepository.findById(Long.valueOf(id));
        if (orden.isPresent()) {
            String observacionesPrevias = orden.get().getCertificado().getObservaciones();
            if (observacionesPrevias == null || observacionesPrevias.isEmpty()) {
                observaciones = observaciones.replace("null", "").replace("\n", "").replace("\r", "");
                log.info("No se han encontrado observaciones previas...");
            }

            Certificado certificado = orden.get().getCertificado();
            certificado.setEstado(estado);
            certificado.setObservaciones(observaciones);
            if (approvedBy != null) {
                log.info("Creando el QR para el certificado");
                UploadCertificateResponse datosImagen = filesService.createQRCertificate(certificado, emailApprover);
                log.info("Finalización creación QR y setear nombre del approver...");
                certificado.setApprovedBy(approvedBy);
                certificado.setFechaCreacion(LocalDateTime.now().toLocalDate());
                certificado.setUrlImagen(datosImagen.getPublicUrl());
            }
            orden.get().setCertificado(certificado);
            return ordenRepository.save(orden.get());
        }
        return ordenRepository.findById(Long.valueOf(id)).get();
    }

    @Transactional
    public OrdenCalibracion createRMAForCertificate(MultipartFile certificadoFile, String rma, String nit, String nombreEquipo, String marca, String modelo, String serial, String codigo) {
        final String pass =  nit.replaceAll("[-.,\\s]", "");
        log.info("Iniciando registro del equipo en el sistema");
        Equipo equipo = Equipo.builder().nombre(nombreEquipo)
                .marca(marca).modelo(modelo).serial(serial).codigoInterno(codigo)
                .build();
        log.info("Equipo creado exitosamente");
        log.info("Iniciando encriptación y subida del certificado");
        UploadCertificateResponse datosCertificado = filesService.save(certificadoFile, pass);
        Certificado certificado = Certificado.builder().idCertificado(datosCertificado.getKey())
                .url(datosCertificado.getPublicUrl()).pass(pass).estado(REVISION_ESTADO)
                .fechaSubida(LocalDateTime.now().toLocalDate())
                .build();
        log.info("Finalización subida certificado");

        log.info("Equipo: " + equipo.toString() + "\n" + "Certificado: " + certificado + "\n");
        log.info("Se ha terminado el proceso de subir el certificado... Procediendo a crear la orden de calibracion");

        certificadoRepository.save(certificado);
        equipoRepository.save(equipo);

        OrdenCalibracion ordenCalibracion = OrdenCalibracion.builder().rma(rma)
                .certificado(certificado)
                .equipo(equipo)
                .build();
        ordenRepository.save(ordenCalibracion);
        log.info("Orden creada exitosamente");
        return ordenCalibracion;
    }

    public OrdenCalibracion updateCertificate(MultipartFile file, String ordenId, String estado) {
        log.info("Empezando a actualizar el certificado deseado");
        OrdenCalibracion ordenCalibracion = ordenRepository.findById(Long.valueOf(ordenId)).get();
        Certificado certificado = ordenCalibracion.getCertificado();
        UploadCertificateResponse datosCertificado = filesService.save(file, certificado.getPass());
        log.info("Certificado actualizado exitosamente");
        certificado.setEstado(estado);
        certificado.setUrl(datosCertificado.getPublicUrl());
        certificadoRepository.save(certificado);
        ordenCalibracion.setCertificado(certificado);
        ordenRepository.save(ordenCalibracion);
        log.info("Orden actualizada exitosamente");
        return ordenCalibracion;
    }

    public List<OrderCalibracionResponse> getOrdersWithState(List<String> estados) {
        List<OrderCalibracionResponse> ordenesState = new ArrayList<>();
        log.info("Recolectando ordenes en estado de :" + estados);
        List<OrdenCalibracion> ordenesCalibracion = ordenRepository.findAll().stream().filter(orden -> estados.contains(orden.getCertificado().getEstado())).collect(Collectors.toList());
        Collections.reverse(ordenesCalibracion);
        if (ordenesCalibracion.size() > 0) log.info("Se han encontrado ordenes que cumplan la condicion");
        ordenesCalibracion.forEach(orden -> {
            OrderCalibracionResponse ordenResponse = new OrderCalibracionResponse(orden.getId(), orden.getRma(), orden.getCertificado().getIdCertificado(), orden.getEquipo().getNombre(), orden.getCertificado().getEstado());
            if (orden.getCertificado().getFechaCreacion() != null){
                ordenResponse.setFechaAprobacion(orden.getCertificado().getFechaCreacion());
            }
            ordenesState.add(ordenResponse);
        });
        return ordenesState;
    }
}
