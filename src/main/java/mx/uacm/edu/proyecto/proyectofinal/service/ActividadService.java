package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import java.util.List;

public interface ActividadService {
    // Crear una actividad dentro de una etapa
    Actividad crearActividad(Long idEtapa, ActividadRequestDTO dto);

    // Listar actividades de una etapa
    List<Actividad> listarPorEtapa(Long idEtapa);

    // Eliminar actividad (solo si no está iniciada, por ejemplo)
    void eliminarActividad(Long idActividad);
}