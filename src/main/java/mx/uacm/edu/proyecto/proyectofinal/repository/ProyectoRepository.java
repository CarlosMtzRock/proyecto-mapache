package mx.uacm.edu.proyecto.proyectofinal.repository;

import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}