package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import org.springframework.stereotype.Component;

// Declara esta clase como un componente de Spring para que pueda ser inyectada en otras clases.
@Component
public class ActividadMapper {

    // Convierte un DTO y una entidad Etapa a una entidad Actividad.
    public Actividad toEntity(ActividadRequestDTO dto, Etapa etapa) {
        // Utiliza el patrón de diseño Builder para construir el objeto Actividad.
        return Actividad.builder()
                // Asigna la etapa a la que pertenece la actividad.
                .etapa(etapa)
                // Mapea los datos del DTO a la entidad.
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .idRequisito(dto.getIdRequisito())
                .fechaInicioProg(dto.getFechaInicioProg())
                .fechaFinProg(dto.getFechaFinProg())
                // Si el porcentaje de avance es nulo, lo establece en 0.
                .porcentajeAvance(dto.getPorcentajeAvance() != null ? dto.getPorcentajeAvance() : 0)
                // Establece el estado inicial de la actividad como "PENDIENTE".
                .estado("PENDIENTE") 
                // Construye y devuelve el objeto Actividad.
                .build();
    }

}
