package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ActividadRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    private String tipo;
    private Long idRequisito; // Opcional (puede ser null)

    private LocalDate fechaInicioProg;
    private LocalDate fechaFinProg;

    // Opcional: Para crearla ya iniciada (aunque por defecto es 0)
    @Min(0) @Max(100)
    private Integer porcentajeAvance;
}