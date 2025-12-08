package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DashboardDTO {
    // Informacion General
    private Long idProyecto;
    private String nombreProyecto;
    private String estadoProyecto;
    private LocalDate fechaFinEstimada;

    // Métricas de Desempeño
    private Integer avanceGlobal; // 0-100%
    private Long etapasTotales;
    private Long etapasCompletadas;
    private Long etapasConRetraso;

    // Métricas Financieras
    private BigDecimal presupuestoTotal; // Lo que tiene el proyecto
    private BigDecimal gastoEjecutado;   // Lo que se ha gastado realmente
    private BigDecimal presupuestoRestante; // Cunto queda
}