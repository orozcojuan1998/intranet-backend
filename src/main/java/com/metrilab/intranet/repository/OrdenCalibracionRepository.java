package com.metrilab.intranet.repository;

import com.metrilab.intranet.modelo.OrdenCalibracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenCalibracionRepository extends JpaRepository<OrdenCalibracion, Long> {

    @Query("Select o from OrdenCalibracion o WHERE o.rma =?1")
    Optional<OrdenCalibracion> obtenerOrderPorRMA(String rma);

    Optional<OrdenCalibracion> findByRma(String rma);
}
