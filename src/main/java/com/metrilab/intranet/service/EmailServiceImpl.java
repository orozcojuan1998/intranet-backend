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
public class EmailServiceImpl implements EmailService{

    private final String tecnicaEmail = "dirtecnica@metrilab.co";
    private final String ensayosEmail = "dirensayos@metrilab.co";

    @Override
    public void sendCertificateApproved(Certificado certificado, String reviewerEmail, String path) {
        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        String emailToSend = certificado.getIdCertificado().contains("C") ? tecnicaEmail : ensayosEmail;
        String subject = "Nuevo Certificado Digital Generado: " + certificado.getIdCertificado().split("\\.")[0];
        Email to = new Email(emailToSend);
        Email ccEmail = new Email(reviewerEmail);
        Personalization personalization = new Personalization();
        personalization.addCc(ccEmail);
        personalization.addTo(to);
        personalization.setSubject(subject);
        Email from = new Email("it@metrilab.co");

        Content content = new Content("text/html", "<strong>Se ha generado el siguiente certificado difital por favor verifique que los datos sean los correctos antes de enviarlo al cliente.</strong> " +
            "<p>La contraseña asignada al certificado es la siguiente: <strong> " + certificado.getPass() + " </strong><p/> " +
            "<p>El cliente puede revisar el certificado en la siguiente URL:  <a href=" + certificado.getUrl()  + ">" + certificado.getUrl() + "</a>");


        Request request = new Request();
        try {
            Attachments attachment = new Attachments();
            Base64 encode = new Base64();
            FileInputStream fis = new FileInputStream(path);
            byte[] fileData = IOUtils.toByteArray(fis);
            String imageDataString = encode.encodeAsString(fileData);
            attachment.setContent(imageDataString);
            attachment.setType("image/png");
            attachment.setDisposition("attachment");
            attachment.setFilename(certificado.getIdCertificado().split("\\.")[0]+".png");
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
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }
}
