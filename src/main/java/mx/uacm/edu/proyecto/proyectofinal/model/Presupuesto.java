package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "presupuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presupuesto")
    private Long idPresupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etapa", nullable = false)
    @ToString.Exclude
    private Etapa etapa;

    @Column(name = "monto_aprobado", nullable = false, precision = 19, scale = 2)
    @PositiveOrZero
    @Builder.Default
    private BigDecimal montoAprobado = BigDecimal.ZERO;

    @Column(name = "monto_gastado", precision = 19, scale = 2)
    @PositiveOrZero
    @Builder.Default
    private BigDecimal montoGastado = BigDecimal.ZERO;

    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    private String estado;

    @Builder.Default
    private String moneda = "MXN";
}