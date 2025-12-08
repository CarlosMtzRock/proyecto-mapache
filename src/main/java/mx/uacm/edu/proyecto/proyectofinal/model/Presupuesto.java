package mx.uacm.edu.proyecto.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// Definimos esta clase como una entidad que se mapeará a una tabla de base de datos.
@Entity
// Especificamos el nombre de la tabla a la que se asociará esta entidad.
@Table(name = "presupuesto")
// Usamos Lombok para generar automáticamente constructores, getters, setters, etc.
@Data
@NoArgsConstructor
@AllArgsConstructor
// Habilitamos el patrón de diseño Builder para construir objetos de esta clase.
@Builder
public class Presupuesto {

    // Definimos la clave primaria de nuestra entidad.
    @Id
    // Configuramos la estrategia de generación de ID como autoincremental.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Mapeamos este campo a la columna 'id_presupuesto'.
    @Column(name = "id_presupuesto")
    private Long idPresupuesto;

    // Establecemos una relación de muchos a uno con la entidad Etapa.
    @ManyToOne(fetch = FetchType.LAZY)
    // Definimos la columna de clave foránea 'id_etapa'.
    @JoinColumn(name = "id_etapa", nullable = false)
    // Excluimos este campo del método toString() para evitar bucles de serialización.
    @ToString.Exclude
    private Etapa etapa;

    // Mapeamos el campo al a columna 'monto_aprobado', especificando precisión y escala.
    @Column(name = "monto_aprobado", nullable = false, precision = 19, scale = 2)
    // Nos aseguramos de que el valor sea siempre positivo o cero.
    @PositiveOrZero
    // Asignamos un valor por defecto de cero al construir el objeto.
    @Builder.Default
    private BigDecimal montoAprobado = BigDecimal.ZERO;

    // Mapeamos el campo a la columna 'monto_gastado'.
    @Column(name = "monto_gastado", precision = 19, scale = 2)
    // Validamos que el monto gastado no sea negativo.
    @PositiveOrZero
    // Asignamos un valor por defecto de cero al construir el objeto.
    @Builder.Default
    private BigDecimal montoGastado = BigDecimal.ZERO;

    // Mapeamos este campo a la columna 'fecha_aprobacion'.
    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    // Definimos un campo para almacenar el estado del presupuesto.
    private String estado;

    // Establecemos 'MXN' como la moneda por defecto al construir el objeto.
    @Builder.Default
    private String moneda = "MXN";
}
