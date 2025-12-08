package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Builder;
import lombok.Data;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EtapaResponseDTO {
    private Long idEtapa;
    private Long idProyecto;
    private String nombre;
    private String descripcion;
    private Integer numeroOrden;

    // Agrupamos fechas para orden visual
    private LocalDate fechaInicioPlan;
    private LocalDate fechaFinPlan;
    private LocalDate fechaInicioReal;
    private LocalDate fechaFinReal;

    private Integer porcentajeAvance;
    private EstadoEtapa estado;

    // Datos financieros calculados
    private BigDecimal presupuestoAsignado;
    private BigDecimal presupuestoGastado;
}