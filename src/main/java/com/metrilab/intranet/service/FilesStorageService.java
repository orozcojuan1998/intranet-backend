package com.metrilab.intranet.service;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.metrilab.intranet.modelo.Certificado;
import com.metrilab.intranet.modelo.UploadCertificateResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {
    void init();

    UploadCertificateResponse save(MultipartFile file, String pw);

    boolean saveToS3(File file, UploadCertificateResponse certificateResponse);

    Resource load(String filename);

    void createQRCertificate(Certificado certificado, String email);

    void deleteAll();

    Stream<Path> loadAll();
}