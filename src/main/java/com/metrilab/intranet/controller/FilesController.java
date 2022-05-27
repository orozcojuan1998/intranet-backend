package com.metrilab.intranet.controller;

import com.metrilab.intranet.modelo.UploadCertificateResponse;
import com.metrilab.intranet.service.ClientesService;
import com.metrilab.intranet.service.FilesStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
public class FilesController {

    @Autowired
    FilesStorageService storageService;

    @Autowired
    ClientesService clientesService;

    @PostMapping("/upload")
    public ResponseEntity<UploadCertificateResponse> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam String pw) {
        UploadCertificateResponse uploadCertificateResponse = new UploadCertificateResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Entering request");
        try {
            uploadCertificateResponse = storageService.save(file, pw);
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(uploadCertificateResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(uploadCertificateResponse);
        }
    }
}
