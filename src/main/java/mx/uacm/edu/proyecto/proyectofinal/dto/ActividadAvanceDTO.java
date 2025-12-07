package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActividadAvanceDTO {
    @NotNull
    @Min(0) @Max(100)
    private Integer nuevoAvance;
}