package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    boolean existsByProyectoIdProyectoAndNumeroOrden(Long idProyecto, Integer numeroOrden);

    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto AND e.numeroOrden >= :ordenNuevo")
    void desplazarOrdenes(@Param("idProyecto") Long idProyecto, @Param("ordenNuevo") Integer ordenNuevo);

    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden >= :nuevoOrden AND e.numeroOrden < :ordenActual")
    void empujarEtapasHaciaAbajo(@Param("idProyecto") Long idProyecto,
                                 @Param("nuevoOrden") Integer nuevoOrden,
                                 @Param("ordenActual") Integer ordenActual);

    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden - 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden > :ordenActual AND e.numeroOrden <= :nuevoOrden")
    void jalarEtapasHaciaArriba(@Param("idProyecto") Long idProyecto,
                                @Param("nuevoOrden") Integer nuevoOrden,
                                @Param("ordenActual") Integer ordenActual);

    List<Etapa> findByProyectoIdProyectoOrderByNumeroOrdenAsc(Long idProyecto);

    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa")
    long contarActividadesPorEtapa(@Param("idEtapa") Long idEtapa);

    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa AND a.estado NOT IN ('COMPLETADA', 'CANCELADA')")
    long contarActividadesPendientes(@Param("idEtapa") Long idEtapa);

    boolean existsByProyectoIdProyectoAndEstadoNot(Long idProyecto, EstadoEtapa estado);

    @Query("SELECT COALESCE(AVG(e.porcentajeAvance), 0) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto")
    Double obtenerPromedioAvance(@Param("idProyecto") Long idProyecto);

    @Query("SELECT COUNT(e) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.fechaFinPlan < CURRENT_DATE AND e.estado <> 'COMPLETADA'")
    Long contarEtapasAtrasadas(@Param("idProyecto") Long idProyecto);

    long countByProyectoIdProyecto(Long idProyecto);

    long countByProyectoIdProyectoAndEstado(Long idProyecto, mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa estado);

    List<Etapa> findByProyectoIdProyecto(Long idProyecto);
}
