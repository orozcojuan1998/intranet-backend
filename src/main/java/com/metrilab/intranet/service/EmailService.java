package com.metrilab.intranet.service;

import com.metrilab.intranet.modelo.Certificado;

public interface EmailService {

    void sendCertificateApproved(Certificado certificado, String reviewerEmail, String path);

}
