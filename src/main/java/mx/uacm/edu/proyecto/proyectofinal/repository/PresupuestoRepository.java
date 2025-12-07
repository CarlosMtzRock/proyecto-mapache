package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // Buscar el presupuesto asociado a una etapa
    Optional<Presupuesto> findByEtapaIdEtapa(Long idEtapa);

    // Regla RN-09: Sumar todos los montos aprobados de un proyecto
    @Query("SELECT SUM(p.montoAprobado) FROM Presupuesto p WHERE p.etapa.proyecto.idProyecto = :idProyecto")
    BigDecimal sumarPresupuestosPorProyecto(@Param("idProyecto") Long idProyecto);
}