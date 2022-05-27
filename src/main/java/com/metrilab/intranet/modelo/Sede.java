package com.metrilab.intranet.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "sede")
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pais;
    private String departamento;
    private String ciudad;
    private String direccion;
    private String complementoDireccion;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    public Sede() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Sede sede = (Sede) o;

        return Objects.equals(id, sede.id);
    }

    @Override
    public int hashCode() {
        return 145371384;
    }
}
