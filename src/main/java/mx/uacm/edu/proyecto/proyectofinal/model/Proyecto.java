package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Construimos esta clase como una entidad JPA, que representará la tabla 'proyecto' en nuestra base de datos.
@Entity
@Table(name = "proyecto")
// Confiamos en Lombok para que nos genere el código repetitivo como constructores, getters y setters.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    // Establecemos el identificador único para cada proyecto.
    @Id
    // Dejamos que la base de datos se encargue de generar este ID de forma automática.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Mapeamos este atributo a la columna 'id_proyecto'.
    @Column(name = "id_proyecto")
    private Long idProyecto;

    // Aquí guardamos una referencia al cliente. Decidimos usar solo el ID.
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    // Todo proyecto necesita un nombre, así que lo definimos como un campo obligatorio.
    @Column(nullable = false)
    private String nombre;

    // Agregamos varios campos descriptivos que nos darán más contexto sobre el proyecto.
    private String descripcion;
    private String metodologia;
    private String tipo;
    private String prioridad;

    // Definimos las fechas clave para la gestión del ciclo de vida del proyecto. La fecha de inicio es indispensable.
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Esta es la fecha en que planeamos terminar el proyecto.
    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    // Y esta será la fecha en que realmente concluyó.
    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    // Manejamos el presupuesto como BigDecimal para asegurar la precisión financiera.
    @Column(name = "presupuesto_total_objetivo", precision = 19, scale = 2)
    // Nos aseguramos de que el presupuesto no pueda ser un valor negativo.
    @PositiveOrZero
    private BigDecimal presupuestoTotalObjetivo;

    // Definimos un campo de estado obligatorio para rastrear si el proyecto está 'Activo', 'Completado', etc.
    @Column(nullable = false)
    private String estado;

    // Creamos una relación de uno a muchos para gestionar las etapas que componen el proyecto.
    // Configuramos la cascada para que, al eliminar un proyecto, todas sus etapas se eliminen también.
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    // Excluimos esta lista de los métodos generados por Lombok para prevenir problemas de recursividad.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    // Inicializamos la lista de etapas para que nunca sea nula.
    @Builder.Default
    private List<Etapa> etapas = new ArrayList<>();
}
