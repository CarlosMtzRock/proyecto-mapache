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

// Definimos esta interfaz como un Repositorio de Spring para que gestione el acceso a los datos de la entidad Etapa.
@Repository
public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    // Construimos un método para validar si ya existe una etapa con un número de orden específico en un proyecto.
    // Esto para mantener la integridad y evitar ordenes duplicadas.
    boolean existsByProyectoIdProyectoAndNumeroOrden(Long idProyecto, Integer numeroOrden);

    // Implementamos la Regla de Automatización RA-04 para el reordenamiento.
    // Cuando insertamos una nueva etapa, necesitamos hacer espacio, esta consulta desplaza todas las etapas posteriores.
    @Modifying // Marcamos la consulta porque modifica datos es un UPDATE
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto AND e.numeroOrden >= :ordenNuevo")
    void desplazarOrdenes(@Param("idProyecto") Long idProyecto, @Param("ordenNuevo") Integer ordenNuevo);

    // Para el reordenamiento, cubrimos el CASO A: mover una etapa hacia arriba en la lista
    // Esta consulta empuja las etapas intermedias hacia abajo para abrir el espacio en la nueva posición.
    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden + 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden >= :nuevoOrden AND e.numeroOrden < :ordenActual")
    void empujarEtapasHaciaAbajo(@Param("idProyecto") Long idProyecto,
                                 @Param("nuevoOrden") Integer nuevoOrden,
                                 @Param("ordenActual") Integer ordenActual);

    // cubrimos el CASO B: mover una etapa hacia abajo
    // Esta consulta jala las etapas intermedias hacia arriba para cerrar el hueco que dejó la etapa movida.
    @Modifying
    @Query("UPDATE Etapa e SET e.numeroOrden = e.numeroOrden - 1 " +
            "WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.numeroOrden > :ordenActual AND e.numeroOrden <= :nuevoOrden")
    void jalarEtapasHaciaArriba(@Param("idProyecto") Long idProyecto,
                                @Param("nuevoOrden") Integer nuevoOrden,
                                @Param("ordenActual") Integer ordenActual);

    // Para el Requerimiento Funcional RF-02, necesitamos una forma de listar las etapas de un proyecto en su orden correcto.
    List<Etapa> findByProyectoIdProyectoOrderByNumeroOrdenAsc(Long idProyecto);

    // Implementamos la Regla de Negocio RN-03. Antes de permitir que una etapa inicie, verificamos si tiene actividades.
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa")
    long contarActividadesPorEtapa(@Param("idEtapa") Long idEtapa);

    // Para la Regla de Validación RV-05, no podemos cerrar una etapa si aún tiene trabajo pendiente.
    // Esta consulta cuenta las actividades que no están 'COMPLETADA' o 'CANCELADA'.
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.etapa.idEtapa = :idEtapa AND a.estado NOT IN ('COMPLETADA', 'CANCELADA')")
    long contarActividadesPendientes(@Param("idEtapa") Long idEtapa);

    // Para la Regla de Automatización RA-05, el estado de un proyecto depende de sus etapas.
    // Verificamos si existen etapas que NO estén completadas para decidir si el proyecto puede marcarse como completo.
    boolean existsByProyectoIdProyectoAndEstadoNot(Long idProyecto, EstadoEtapa estado);


    // Para el Dashboard, necesitamos calcular el avance promedio de un proyecto.
    // Usamos COALESCE para devolver 0 en lugar de null si un proyecto no tiene etapas.
    @Query("SELECT COALESCE(AVG(e.porcentajeAvance), 0) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto")
    Double obtenerPromedioAvance(@Param("idProyecto") Long idProyecto);

    // También para el Dashboard (RA-06), identificamos etapas con retraso.
    // Contamos las etapas cuya fecha de fin planificada ya pasó y aún no están completadas.
    @Query("SELECT COUNT(e) FROM Etapa e WHERE e.proyecto.idProyecto = :idProyecto " +
            "AND e.fechaFinPlan < CURRENT_DATE AND e.estado <> 'COMPLETADA'")
    Long contarEtapasAtrasadas(@Param("idProyecto") Long idProyecto);

    // Una consulta simple para obtener el número total de etapas de un proyecto.
    long countByProyectoIdProyecto(Long idProyecto);

    // Y otra para contar cuántas de esas etapas están en un estado específico (ej. 'COMPLETADA').
    long countByProyectoIdProyectoAndEstado(Long idProyecto, mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa estado);

    // Para la Regla de Automatización RA-09, a veces necesitamos todos los objetos Etapa para cálculos más complejos.
    // Este método nos los proporciona.
    List<Etapa> findByProyectoIdProyecto(Long idProyecto);
}
