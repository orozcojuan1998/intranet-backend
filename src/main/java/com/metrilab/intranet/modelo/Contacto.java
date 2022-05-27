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
@Table(name = "contacto")
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreContacto;
    private String areaTrabajo;
    private String correo;
    private String telefono;
    private String celular;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    @OneToOne
    private Sede sede;

    public Contacto() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Contacto contacto = (Contacto) o;

        return Objects.equals(id, contacto.id);
    }

    @Override
    public int hashCode() {
        return 146569524;
    }
}
