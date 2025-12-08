package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Declara esta clase como un componente de Spring.
@Component
public class PresupuestoMapper {

    // Convierte una entidad Presupuesto a un PresupuestoDTO.
    public PresupuestoDTO toDTO(Presupuesto entity) {
        // Verifica si la entidad es nula.
        if (entity == null) {
            // Si es nula, retorna un DTO con valores predeterminados para evitar errores.
            return PresupuestoDTO.builder()
                    .montoAprobado(BigDecimal.ZERO)
                    .montoGastado(BigDecimal.ZERO)
                    .moneda("MXN")
                    .build();
        }

        // Si la entidad no es nula, mapea sus propiedades al DTO.
        return PresupuestoDTO.builder()
                .idPresupuesto(entity.getIdPresupuesto())
                .montoAprobado(entity.getMontoAprobado())
                .montoGastado(entity.getMontoGastado())
                .fechaAprobacion(entity.getFechaAprobacion())
                .moneda(entity.getMoneda())
                .build();
    }
}
