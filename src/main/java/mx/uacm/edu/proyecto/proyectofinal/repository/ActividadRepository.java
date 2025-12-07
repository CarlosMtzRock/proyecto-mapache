package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // Listar actividades de una etapa específica
    List<Actividad> findByEtapaIdEtapa(Long idEtapa);

    // Para la Regla RV-05: Contar cuántas actividades NO están completadas ni canceladas
    // "Select count(*) from actividad where id_etapa = ? and estado NOT IN ('COMPLETADA', 'CANCELADA')"
    long countByEtapaIdEtapaAndEstadoNotIn(Long idEtapa, List<String> estadosFinales);

    boolean existsByEtapaIdEtapa(Long idEtapa);
}