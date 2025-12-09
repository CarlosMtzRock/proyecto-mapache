package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Usamos este DTO para recibir los datos al crear o actualizar un Proyecto
@Data
public class ProyectoRequestDTO {

    @NotNull(message = "El ID del cliente no puede ser nulo")
    private Long idCliente;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String nombre;

    private String descripcion;
    private String metodologia;
    private String tipo;
    private String prioridad;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @FutureOrPresent(message = "La fecha de fin estimada debe ser en el presente o futuro")
    private LocalDate fechaFinEstimada;

    @PositiveOrZero(message = "El presupuesto debe ser un valor positivo o cero")
    private BigDecimal presupuestoTotalObjetivo;

    @NotBlank(message = "El estado no puede estar vacio")
    private String estado;
}
