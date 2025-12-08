package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Definimos esta interfaz como un Repositorio de Spring.
// Su propósito es centralizar toda la lógica de acceso a datos para nuestra entidad 'Proyecto'.
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    // Al extender JpaRepository, le damos a Spring el control para que nos proporcione
    // automáticamente los métodos CRUD (Crear, Leer, Actualizar, Eliminar) más comunes.

}
