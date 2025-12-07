package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EtapaRequestDTO {

    // RV-01: Campos obligatorios
    @NotNull(message = "El nombre es obligatorio")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El número de orden es obligatorio")
    @Min(value = 1, message = "El orden debe ser al menos 1")
    private Integer numeroOrden;

    @NotNull(message = "La fecha de inicio planificada es obligatoria")
    private LocalDate fechaInicioPlan;

    @NotNull(message = "La fecha de fin planificada es obligatoria")
    private LocalDate fechaFinPlan;

    @PositiveOrZero(message = "El presupuesto inicial no puede ser negativo")
    private BigDecimal presupuestoInicial;
}