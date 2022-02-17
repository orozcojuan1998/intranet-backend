package com.metrilab.intranet.modelo;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@Entity
@Table(name = "orden_calibracion")
public class OrdenCalibracion {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String rma;
    private String observaciones;

    @OneToOne
    private Certificado certificado;
    @OneToOne
    private Equipo equipo;

    public OrdenCalibracion(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrdenCalibracion that = (OrdenCalibracion) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 1469248678;
    }
}
