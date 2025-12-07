package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proyecto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proyecto")
    private Long idProyecto;

    //rEFERENCIA pOR iD
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;
    private String metodologia;
    private String tipo;
    private String prioridad;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "presupuesto_total_objetivo", precision = 19, scale = 2)
    @PositiveOrZero
    private BigDecimal presupuestoTotalObjetivo;

    @Column(nullable = false)
    private String estado;

    // Relación Bidireccional: Mantenemos la lista de etapas porque es parte del módulo
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Etapa> etapas = new ArrayList<>();
}