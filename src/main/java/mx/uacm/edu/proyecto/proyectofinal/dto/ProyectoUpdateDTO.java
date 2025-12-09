package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO para actualizaciones parciales de un Proyecto.
// Nota: No usamos @NotNull o @NotBlank para poder enviar solo los campos que queremos cambiar.
@Data
public class ProyectoUpdateDTO {

    private Long idCliente;

    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String nombre;

    private String descripcion;
    private String metodologia;
    private String tipo;
    private String prioridad;

    private LocalDate fechaInicio;

    @FutureOrPresent(message = "La fecha de fin estimada debe ser en el presente o futuro")
    private LocalDate fechaFinEstimada;

    @PositiveOrZero(message = "El presupuesto debe ser un valor positivo o cero")
    private BigDecimal presupuestoTotalObjetivo;

    private String estado;
}
