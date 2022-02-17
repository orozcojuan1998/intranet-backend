package com.metrilab.intranet.modelo;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@Entity
@Table(name = "certificado")
public class Certificado {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String idCertificado;
    private String url;
    private String estado;
    private String pass;
    private String approvedBy;
    private LocalDate fechaCreacion;
    private String observaciones;

    public Certificado(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Certificado that = (Certificado) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 1384290925;
    }
}
