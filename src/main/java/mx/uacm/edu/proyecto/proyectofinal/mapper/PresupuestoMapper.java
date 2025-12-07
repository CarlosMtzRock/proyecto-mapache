package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoDTO; // (Lo crearemos abajo rápido)
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PresupuestoMapper {

    public PresupuestoDTO toDTO(Presupuesto entity) {
        if (entity == null) {
            // Retornamos un DTO con ceros para evitar NullPointerExceptions en el frontend
            return PresupuestoDTO.builder()
                    .montoAprobado(BigDecimal.ZERO)
                    .montoGastado(BigDecimal.ZERO)
                    .moneda("MXN")
                    .build();
        }

        return PresupuestoDTO.builder()
                .idPresupuesto(entity.getIdPresupuesto())
                .montoAprobado(entity.getMontoAprobado())
                .montoGastado(entity.getMontoGastado())
                .fechaAprobacion(entity.getFechaAprobacion())
                .moneda(entity.getMoneda())
                .build();
    }
}