package com.metrilab.intranet.service;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.metrilab.intranet.modelo.UploadCertificateResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {
    public void init();

    public UploadCertificateResponse save(MultipartFile file, String pw);

    public boolean saveToS3(File file, UploadCertificateResponse certificateResponse);

    public Resource load(String filename);

    public void deleteAll();

    public Stream<Path> loadAll();
}