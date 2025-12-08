package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para actualizar el avance de una actividad.
 */
@Data
public class ActividadAvanceDTO {
    /**
     * El nuevo porcentaje de avance de la actividad.
     * Debe ser un valor entre 0 y 100.
     */
    @NotNull
    @Min(0) @Max(100)
    private Integer nuevoAvance;
}
