package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para la creación y actualización de una Actividad.
 * Contiene los datos necesarios para registrar una nueva actividad o modificar una existente.
 */
@Data
public class ActividadRequestDTO {
    /**
     * El nombre de la actividad.
     * Es obligatorio y no puede exceder los 200 caracteres.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    /**
     * El tipo de la actividad.
     * Puede ser usado para categorizar la actividad.
     */
    private String tipo;

    /**
     * El ID del requisito al que está asociada la actividad
     */
    private Long idRequisito;

    /**
     * La fecha de inicio programada para la actividad.
     */
    private LocalDate fechaInicioProg;

    /**
     * La fecha de fin programada para la actividad.
     */
    private LocalDate fechaFinProg;

    /**
     * El porcentaje de avance de la actividad.
     * Debe ser un valor entre 0 y 100.
     */
    @Min(0) @Max(100)
    private Integer porcentajeAvance;
}
