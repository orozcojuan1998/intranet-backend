package com.metrilab.intranet.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.metrilab.intranet.modelo.Certificado;
import com.metrilab.intranet.modelo.UploadCertificateResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class FilesStorageServiceImpl implements FilesStorageService {
    private final String BUCKET_NAME = "certificados-metrilab-" + Year.now().toString();
    private final Path root = Paths.get("uploads");
    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();
    private final EmailService emailService;

    public FilesStorageServiceImpl(EmailService emailService) {
        this.emailService = emailService;
    }


    @Override
    public void init() {
        try {
            Files.createDirectory(root);

        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public UploadCertificateResponse save(MultipartFile file, String pw) {
        UploadCertificateResponse uploadCertificateResponse = new UploadCertificateResponse();
        AccessPermission ap = new AccessPermission();
        var stpp = new StandardProtectionPolicy(pw, pw, ap);
        stpp.setEncryptionKeyLength(128);
        stpp.setPermissions(ap);

        log.info("Starting to save file");
        try {
            String fileName = Objects.requireNonNull(file.getOriginalFilename());
            File myFile = new File(root + File.separator + fileName);
            OutputStream os = new FileOutputStream(myFile);
            os.write(file.getBytes());
            PDDocument pdd = PDDocument.load(myFile);

            pdd.protect(stpp);
            // pdd.saveIncremental(os);
            pdd.save(root + File.separator + Objects.requireNonNull(file.getOriginalFilename()));
            log.info("Finishing securing the document");
            boolean wasFileUploaded = saveToS3(new File(root + File.separator + fileName), uploadCertificateResponse);
            if (!wasFileUploaded){
                log.error("El archivo no se pudo subir a S3");
            }
            log.info("File uploaded to S3");

            pdd.close();
            os.close();
            if (myFile.delete()) {
                log.info("Eliminación del archivo temporal completada exitosamente");
            }
        } catch (Exception e) {
            log.error("Could not store the file. Error: " + e.getMessage());
            uploadCertificateResponse.setError("Could not upload the file");
        }
        return uploadCertificateResponse;
    }

    @Override
    public void createQRCertificate(Certificado certificado, String email) {
        String charset = "UTF-8";
        String path = root + File.separator + "QR_Certificado.png";
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(
                    new String(certificado.getUrl().getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, 150, 150);
            MatrixToImageWriter.writeToPath(
                    matrix,
                    path.substring(path.lastIndexOf('.') + 1),
                    Paths.get(path));
            emailService.sendCertificateApproved(certificado, email, path);
            File myFile = new File(path);
            if (myFile.delete()) {
                log.info("Eliminación del archivo temporal completada exitosamente");
            }
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean saveToS3(File file, UploadCertificateResponse certificateResponse) {
        String key = file.getName();
        log.info("Uploading the following file to S3: " + key);
        try {
            S3Waiter s3Waiter = s3Client.waiter();
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromFile(file));
            HeadObjectRequest bucketRequestWait = HeadObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            WaiterResponse<HeadObjectResponse> waiterResponse = s3Waiter.waitUntilObjectExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            log.info(key + " is ready, uploading process was successful");
            certificateResponse.setKey(file.getName());
            certificateResponse.setPublicUrl(s3Client.utilities().getUrl(x -> x.bucket(BUCKET_NAME).key(key)).toExternalForm());
            return true;
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }
}