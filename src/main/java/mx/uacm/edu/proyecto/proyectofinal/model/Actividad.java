package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "actividad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Long idActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etapa", nullable = false)
    @ToString.Exclude
    private Etapa etapa;

    @Column(name = "id_requisito")
    private Long idRequisito;

    @Column(nullable = false, length = 200)
    private String nombre;

    private String tipo;

    @Column(name = "fecha_inicio_prog")
    private LocalDate fechaInicioProg;

    @Column(name = "fecha_fin_prog")
    private LocalDate fechaFinProg;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "porcentaje_avance")
    @Min(0) @Max(100)
    @Builder.Default
    private Integer porcentajeAvance = 0;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "PENDIENTE";
}