package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Indica que esta clase es una entidad JPA.
@Entity
// Especifica la tabla y define una restricción única compuesta.
@Table(name = "etapa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_proyecto", "numero_orden"})
})
// Anotaciones de Lombok para generar código boilerplate (getters, setters, etc.).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etapa {

    // Define la clave primaria de la entidad.
    @Id
    // Configura la generación automática del valor de la clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Mapea el campo a la columna 'id_etapa'.
    @Column(name = "id_etapa")
    private Long idEtapa;

    // Define una relación de muchos a uno con la entidad Proyecto.
    @ManyToOne(fetch = FetchType.LAZY)
    // Especifica la columna de unión (clave foránea).
    @JoinColumn(name = "id_proyecto", nullable = false)
    // Excluye este campo del método toString() para evitar bucles infinitos.
    @ToString.Exclude
    private Proyecto proyecto;

    // Mapea el campo a la columna 'nombre', no puede ser nulo y tiene longitud máxima de 150.
    @Column(nullable = false, length = 150)
    private String nombre;

    // Descripción de la etapa.
    private String descripcion;

    // Mapea el campo a la columna 'numero_orden', no puede ser nulo.
    @Column(name = "numero_orden", nullable = false)
    // Valida que el valor sea como mínimo 1.
    @Min(1)
    private Integer numeroOrden;

    // Mapea el campo a la columna 'fecha_inicio_plan', no puede ser nulo.
    @Column(name = "fecha_inicio_plan", nullable = false)
    private LocalDate fechaInicioPlan;

    // Mapea el campo a la columna 'fecha_fin_plan', no puede ser nulo.
    @Column(name = "fecha_fin_plan", nullable = false)
    private LocalDate fechaFinPlan;

    // Mapea el campo a la columna 'fecha_inicio_real'.
    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    // Mapea el campo a la columna 'fecha_fin_real'.
    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    // Mapea el campo a la columna 'porcentaje_avance'.
    @Column(name = "porcentaje_avance")
    // Valida que el valor esté entre 0 y 100.
    @Min(0) @Max(100)
    // Establece un valor por defecto de 0 usando el Builder.
    @Builder.Default
    private Integer porcentajeAvance = 0;

    // Especifica que el enum se debe guardar como una cadena de texto.
    @Enumerated(EnumType.STRING)
    // Mapea el campo a una columna que no puede ser nula.
    @Column(nullable = false)
    // Establece un estado por defecto usando el Builder.
    @Builder.Default
    private EstadoEtapa estado = EstadoEtapa.PLANIFICADA;

    // Define una relación de uno a muchos con la entidad Presupuesto.
    @OneToMany(mappedBy = "etapa", cascade = CascadeType.ALL, orphanRemoval = true)
    // Excluye este campo de los métodos toString(), equals() y hashCode() para evitar problemas de rendimiento y recursividad.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    // Inicializa la lista por defecto usando el Builder.
    @Builder.Default
    private List<Presupuesto> presupuestos = new ArrayList<>();

    // Define una relación de uno a muchos con la entidad Actividad.
    @OneToMany(mappedBy = "etapa", cascade = CascadeType.ALL, orphanRemoval = true)
    // Excluye este campo de los métodos toString(), equals() y hashCode().
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    // Inicializa la lista por defecto usando el Builder.
    @Builder.Default
    private List<Actividad> actividades = new ArrayList<>();
}
