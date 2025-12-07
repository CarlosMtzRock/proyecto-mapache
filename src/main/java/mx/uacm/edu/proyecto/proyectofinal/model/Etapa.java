package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etapa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_proyecto", "numero_orden"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etapa")
    private Long idEtapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false)
    @ToString.Exclude
    private Proyecto proyecto;

    @Column(nullable = false, length = 150)
    private String nombre;

    private String descripcion;

    @Column(name = "numero_orden", nullable = false)
    @Min(1)
    private Integer numeroOrden;

    @Column(name = "fecha_inicio_plan", nullable = false)
    private LocalDate fechaInicioPlan;

    @Column(name = "fecha_fin_plan", nullable = false)
    private LocalDate fechaFinPlan;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "porcentaje_avance")
    @Min(0) @Max(100)
    @Builder.Default
    private Integer porcentajeAvance = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEtapa estado = EstadoEtapa.PLANIFICADA;

    @OneToMany(mappedBy = "etapa", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Presupuesto> presupuestos = new ArrayList<>();

    @OneToMany(mappedBy = "etapa", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Actividad> actividades = new ArrayList<>();
}