package com.metrilab.intranet.service;

import com.metrilab.intranet.modelo.Certificado;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {


    @Override
    public void sendCertificateApproved(Certificado certificado, String reviewerEmail, String path) {
        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

        log.info("Seteando datos básicos del email");
        String certificateType = certificado.getIdCertificado();
        String tecnicaEmail = "dirtecnica@metrilab.co";
        String ensayosEmail = "dirensayos@metrilab.co";
        String firstEmailPart = "<strong>Se ha generado el siguiente ";
        String finalEmailPart = "por favor verifique que los datos sean los correctos antes de enviarlo al cliente.</strong>";
        boolean isEnsayo = certificateType.matches("^\\d{2}E-\\d{1,3}.pdf") || certificateType.matches("^\\d{2}E-P\\d{1,2}.pdf");
        String emailToSend = isEnsayo  ? ensayosEmail : tecnicaEmail;
        String message;
        String subject;
        if (isEnsayo){
            subject = "Nuevo Informe de Ensayo Digital Generado: " + certificado.getIdCertificado().split("\\.")[0];
            message = firstEmailPart + "Informe de Ensayo, " + finalEmailPart;
        }
        else if (certificateType.matches("^\\d{2}IC-\\d{1,3}.pdf")){
            message = firstEmailPart + "Informe de Calificación, " + finalEmailPart;
            subject = "Nuevo Informe de Calificación Digital Generado: " + certificado.getIdCertificado().split("\\.")[0];
        }
        else if (certificateType.matches("^\\d{2}C-\\d{1,3}.pdf")){
            subject = "Nuevo Certificado de Calibración Digital Generado: " + certificado.getIdCertificado().split("\\.")[0];
            message = firstEmailPart + "Certificado de Calibración, " + finalEmailPart;
        }
        else if (certificateType.matches("^\\d{2}EC-\\d{1,3}.pdf")){
            subject = "Nueva Evaluación de Conformidad Digital Generada: " + certificado.getIdCertificado().split("\\.")[0];
            message = "<strong>Se ha generado la siguiente " + "Evaluación de Conformidad, " + finalEmailPart;
        }
        else{
            subject = "Nuevo Documento digital Generado: " + certificado.getIdCertificado().split("\\.")[0];
            message = firstEmailPart + "Documento digital, " + finalEmailPart;
        }
        log.info("Finalización seteo del cuerpo del email");

        Email to = new Email(emailToSend);
        Email ccEmail = new Email(reviewerEmail);
        Personalization personalization = new Personalization();
        personalization.addCc(ccEmail);
        personalization.addTo(to);
        personalization.setSubject(subject);
        Email from = new Email("it@metrilab.co");

        Content content = new Content("text/html", message +
                "<p>La contraseña asignada al certificado es la siguiente: <strong> " + certificado.getPass() + " </strong><p/> " +
                "<p>El cliente puede revisar el certificado en la siguiente URL:  <a href=" + certificado.getUrl() + ">" + certificado.getUrl() + "</a>");

        Request request = new Request();
        try {
            log.info("Iniciando envio del mensaje a remitentes en try catch block");
            Attachments attachment = new Attachments();
            Base64 encode = new Base64();
            FileInputStream fis = new FileInputStream(path);
            byte[] fileData = IOUtils.toByteArray(fis);
            String imageDataString = encode.encodeAsString(fileData);
            attachment.setContent(imageDataString);
            attachment.setType("image/png");
            attachment.setDisposition("attachment");
            attachment.setFilename(certificado.getIdCertificado().split("\\.")[0] + ".png");
            Mail mail = new Mail();
            mail.setFrom(from);
            mail.addPersonalization(personalization);
            mail.addContent(content);
            mail.addAttachments(attachment);
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("Response from email provider: " + response.getStatusCode());
            fis.close();
            log.info("Envio completado sin errores");
        } catch (IOException ex) {
            ex.printStackTrace();
            log.info(ex.getMessage());
            log.error(ex.getMessage());
        }
    }
}
