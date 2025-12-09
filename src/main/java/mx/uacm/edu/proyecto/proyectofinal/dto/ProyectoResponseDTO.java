package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// Este DTO representa la informacion de un Proyecto que devolvemos al cliente
@Data
public class ProyectoResponseDTO {

    private Long idProyecto;
    private Long idCliente;
    private String nombre;
    private String descripcion;
    private String metodologia;
    private String tipo;
    private String prioridad;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaFinReal;
    private BigDecimal presupuestoTotalObjetivo;
    private String estado;
    // private List<EtapaResponseDTO> etapas;
}
