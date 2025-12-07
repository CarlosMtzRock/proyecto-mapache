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
    // COALESCE es para que si no hay registros devuelva 0 en vez de null
    @Query("SELECT COALESCE(SUM(p.montoAprobado), 0) FROM Presupuesto p WHERE p.etapa.proyecto.idProyecto = :idProyecto")
    BigDecimal sumarPresupuestosPorProyecto(@Param("idProyecto") Long idProyecto);

    // Devuelve true si existe algún presupuesto en esa etapa con montoGastado > 0
    boolean existsByEtapaIdEtapaAndMontoGastadoGreaterThan(Long idEtapa, java.math.BigDecimal monto);

    // Calcular cuánto se ha gastado en total en el proyecto
    // Navegación: Presupuesto -> Etapa -> Proyecto
    @Query("SELECT COALESCE(SUM(p.montoGastado), 0) FROM Presupuesto p WHERE p.etapa.proyecto.idProyecto = :idProyecto")
    BigDecimal sumarGastoRealPorProyecto(@Param("idProyecto") Long idProyecto);
}