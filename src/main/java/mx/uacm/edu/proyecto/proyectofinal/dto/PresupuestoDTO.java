package mx.uacm.edu.proyecto.proyectofinal.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PresupuestoDTO {
    private Long idPresupuesto;
    private BigDecimal montoAprobado;
    private BigDecimal montoGastado;
    private LocalDate fechaAprobacion;
    private String moneda;
}