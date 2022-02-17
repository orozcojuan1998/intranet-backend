package com.metrilab.intranet.service;

import com.metrilab.intranet.modelo.Certificado;
import com.metrilab.intranet.repository.CertificadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;

    @Autowired
    public CertificadoService(CertificadoRepository certificadoRepository){
        this.certificadoRepository = certificadoRepository;
    }

    public Certificado getCertificado(String id){
        if(certificadoRepository.findById(Long.valueOf(id)).isPresent()){
            return certificadoRepository.findById(Long.valueOf(id)).get();
        }
        return certificadoRepository.findById(Long.valueOf(id)).orElse(new Certificado());
    }

}
