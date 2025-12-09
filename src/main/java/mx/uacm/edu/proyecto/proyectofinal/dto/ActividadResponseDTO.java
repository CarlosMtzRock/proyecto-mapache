package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Data;
import java.time.LocalDate;

// DTO para devolver la informacion completa de una Actividad
@Data
public class ActividadResponseDTO {

    private Long idActividad;
    private Long idEtapa; // Aplanamos la informacion, solo devolvemos el ID de la etapa
    private Long idRequisito;
    private String nombre;
    private String tipo;
    private LocalDate fechaInicioProg;
    private LocalDate fechaFinProg;
    private LocalDate fechaInicioReal;
    private LocalDate fechaFinReal;
    private Integer porcentajeAvance;
    private String estado;
}
