package com.metrilab.intranet.repository;

import com.metrilab.intranet.modelo.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
}