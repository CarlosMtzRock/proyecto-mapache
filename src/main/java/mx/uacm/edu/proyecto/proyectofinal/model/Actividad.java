package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDate;

// Indica que esta clase es una entidad JPA, que se mapeará a una tabla en la base de datos.
@Entity
// Especifica el nombre de la tabla en la base de datos.
@Table(name = "actividad")
// Anotación de Lombok que genera getters, setters, toString, equals y hashCode.
@Data
// Anotación de Lombok que genera un constructor sin argumentos.
@NoArgsConstructor
// Anotación de Lombok que genera un constructor con todos los argumentos.
@AllArgsConstructor
// Anotación de Lombok que implementa el patrón de diseño Builder.
@Builder
public class Actividad {

    // Indica que este campo es la clave primaria de la tabla.
    @Id
    // Configura la estrategia de generación de la clave primaria (autoincremental).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Mapea este campo a la columna 'id_actividad' en la tabla.
    @Column(name = "id_actividad")
    private Long idActividad;

    // Define una relación de muchos a uno con la entidad Etapa.
    @ManyToOne(fetch = FetchType.LAZY)
    // Especifica la columna de unión (clave foránea) en la tabla 'actividad'.
    @JoinColumn(name = "id_etapa", nullable = false)
    // Excluye este campo del método toString() para evitar recursividad infinita.
    @ToString.Exclude
    private Etapa etapa;

    // Mapea este campo a la columna 'id_requisito'.
    @Column(name = "id_requisito")
    private Long idRequisito;

    // Mapea este campo a una columna, no puede ser nulo y tiene una longitud máxima de 200.
    @Column(nullable = false, length = 200)
    private String nombre;

    // Tipo o categoría de la actividad.
    private String tipo;

    // Mapea este campo a la columna 'fecha_inicio_prog'.
    @Column(name = "fecha_inicio_prog")
    private LocalDate fechaInicioProg;

    // Mapea este campo a la columna 'fecha_fin_prog'.
    @Column(name = "fecha_fin_prog")
    private LocalDate fechaFinProg;

    // Mapea este campo a la columna 'fecha_inicio_real'.
    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    // Mapea este campo a la columna 'fecha_fin_real'.
    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    // Mapea este campo a la columna 'porcentaje_avance'.
    @Column(name = "porcentaje_avance")
    // Validaciones para que el valor esté entre 0 y 100.
    @Min(0) @Max(100)
    // Establece un valor por defecto (0) para este campo cuando se usa el Builder.
    @Builder.Default
    private Integer porcentajeAvance = 0;

    // Mapea este campo a una columna que no puede ser nula.
    @Column(nullable = false)
    // Establece un valor por defecto ("PENDIENTE") para este campo cuando se usa el Builder.
    @Builder.Default
    private String estado = "PENDIENTE";
}
