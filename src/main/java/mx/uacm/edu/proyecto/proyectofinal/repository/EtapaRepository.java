package mx.uacm.edu.proyecto.proyectofinal.repository;

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

    // RF-02: Buscar por ID de Proyecto y Ordenar por numeroOrden
    List<Etapa> findByProyectoIdProyectoOrderByNumeroOrdenAsc(Long idProyecto);
}