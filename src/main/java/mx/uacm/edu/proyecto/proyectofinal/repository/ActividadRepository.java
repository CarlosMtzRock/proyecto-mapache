package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Marcamos esta interfaz como un Repositorio de Spring, para que se encargue de la lógica de acceso a datos.
@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // Definimos un método para obtener todas las actividades que pertenecen a una etapa específica.
    // Spring Data JPA construirá la consulta automáticamente basándose en el nombre del método.
    List<Actividad> findByEtapaIdEtapa(Long idEtapa);

    // Creamos este método para implementar la Regla de Negocio RV-05.
    // Necesitamos contar cuántas actividades de una etapa aun no han sido completadas o canceladas.
    // Spring Data JPA generará la consulta: "Select count(*) from actividad where id_etapa = ? and estado NOT IN (?, ?...)"
    long countByEtapaIdEtapaAndEstadoNotIn(Long idEtapa, List<String> estadosFinales);

    // Añadimos un método simple para verificar si una etapa tiene al menos una actividad.
    // Nos será útil para tomar decisiones sin necesidad de cargar la lista completa de actividades.
    boolean existsByEtapaIdEtapa(Long idEtapa);
}
