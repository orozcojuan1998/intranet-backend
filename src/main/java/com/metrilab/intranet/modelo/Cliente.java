package com.metrilab.intranet.modelo;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String razonSocial;
    @Column(unique=true)
    private String nit;
    private String correo;
    private String correoFE;
    private LocalDate fechaCreacion;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Contacto> contactos = new ArrayList<>();


    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Sede> sedes;

    public Cliente(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return 196541384;
    }

    public boolean addContact (Contacto contacto){
        return this.getContactos().add(contacto);
    }

    public boolean addSede (Sede sede){
        if (sedes == null){
            sedes = new ArrayList<>();
        }
        return this.getSedes().add(sede);
    }
}
