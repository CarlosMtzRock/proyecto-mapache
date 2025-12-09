package mx.uacm.edu.proyecto.proyectofinal.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

// DTO para recibir actualizaciones de un presupuesto
@Data
public class PresupuestoUpdateDTO {

    @PositiveOrZero(message = "El monto aprobado no puede ser negativo")
    private BigDecimal montoAprobado;

    @PositiveOrZero(message = "El monto gastado no puede ser negativo")
    private BigDecimal montoGastado;
}
