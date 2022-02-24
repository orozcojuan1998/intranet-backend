package com.metrilab.intranet.service;

import com.metrilab.intranet.modelo.Certificado;
import com.metrilab.intranet.modelo.OrdenCalibracion;

public interface EmailService {

    void sendCertificateApproved(Certificado certificado, String reviewerEmail, String path);

}
