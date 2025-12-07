package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    // Validar si existe
    boolean existsByProyectoIdProyectoAndNumeroOrden(Long idProyecto, Integer numeroOrden);

    // RA-04: Reordenamiento Automático
    // "Mueve todas las etapas hacia abajo  donde el orden sea mayor o igual al que quiero insertar"
    @Modifying // Indica que es un UPDATE, no un SELECT
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto AND e.numeroOrden >= :ordenNuevo")
    void desplazarOrdenes(@Param("idProyecto") Long idProyecto, @Param("ordenNuevo") Integer ordenNuevo);

    // CASO A: Mover de 5 a 2 (Subir prioridad)
    // Empujamos las etapas intermedias (2, 3, 4) hacia abajo (+1) para hacer hueco en el 2.
    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden >= :nuevoOrden AND e.numeroOrden < :ordenActual")
    void empujarEtapasHaciaAbajo(@Param("idProyecto") Long idProyecto,
                                 @Param("nuevoOrden") Integer nuevoOrden,
                                 @Param("ordenActual") Integer ordenActual);

    // CASO B: Mover de 2 a 5 (Bajar prioridad)
    // Jalamos las etapas intermedias (3, 4, 5) hacia arriba (-1) para tapar el hueco del 2.
    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden - 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden > :ordenActual AND e.numeroOrden <= :nuevoOrden")
    void jalarEtapasHaciaArriba(@Param("idProyecto") Long idProyecto,
                                @Param("nuevoOrden") Integer nuevoOrden,
                                @Param("ordenActual") Integer ordenActual);

    // RF-02: Buscar por ID de Proyecto y Ordenar por numeroOrden
    List<Etapa> findByProyectoIdProyectoOrderByNumeroOrdenAsc(Long idProyecto);

    // Para RN-03: Contar cuántas actividades tiene una etapa (para dejarla iniciar)
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa")
    long contarActividadesPorEtapa(@Param("idEtapa") Long idEtapa);

    // Para RV-05: Contar actividades pendientes (no completadas ni canceladas)
    // Se usa para bloquear el cierre de la etapa
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa AND a.estado NOT IN ('COMPLETADA', 'CANCELADA')")
    long contarActividadesPendientes(@Param("idEtapa") Long idEtapa);

    // Para RA-05: Verificar si existen etapas NO completadas en un proyecto
    // Si devuelve true, el proyecto NO puede estar completado.
    boolean existsByProyectoIdProyectoAndEstadoNot(Long idProyecto, EstadoEtapa estado);


    // 1. Calcular el promedio de avance de todo el proyecto
    @Query("SELECT COALESCE(AVG(e.porcentajeAvance), 0) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto")
    Double obtenerPromedioAvance(@Param("idProyecto") Long idProyecto);

    // 2. Contar etapas retrasadas (RA-06: Hoy > FinPlan Y No completada)
    @Query("SELECT COUNT(e) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.fechaFinPlan < CURRENT_DATE AND e.estado <> 'COMPLETADA'")
    Long contarEtapasAtrasadas(@Param("idProyecto") Long idProyecto);

    // 3. Contar total de etapas
    long countByProyectoIdProyecto(Long idProyecto);

    // 4. Contar etapas completadas
    long countByProyectoIdProyectoAndEstado(Long idProyecto, mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa estado);

    // Para RA-09: Obtener todas las etapas para promediar avance
    List<Etapa> findByProyectoIdProyecto(Long idProyecto);
}