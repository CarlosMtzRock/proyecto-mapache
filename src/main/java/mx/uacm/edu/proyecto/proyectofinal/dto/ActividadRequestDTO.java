package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ActividadRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String tipo;
    private Long idRequisito;
    private LocalDate fechaInicioProg;
    private LocalDate fechaFinProg;
}