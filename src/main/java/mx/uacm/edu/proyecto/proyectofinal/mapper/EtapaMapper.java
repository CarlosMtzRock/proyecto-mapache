package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EtapaMapper {

    // Convierte el Request DTO a la Entidad Etapa (para guardar)
    public Etapa toEntity(EtapaRequestDTO dto, Proyecto proyecto) {
        return Etapa.builder()
                .proyecto(proyecto)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .numeroOrden(dto.getNumeroOrden())
                .fechaInicioPlan(dto.getFechaInicioPlan())
                .fechaFinPlan(dto.getFechaFinPlan())
                .estado(EstadoEtapa.PLANIFICADA) // RA-01
                .porcentajeAvance(0)
                .build();
    }

    // Convierte la Entidad Etapa mas Presupuesto a Response DTO (para responder)
    public EtapaResponseDTO toResponse(Etapa etapa, Presupuesto presupuesto) {
        BigDecimal asignado = (presupuesto != null) ? presupuesto.getMontoAprobado() : BigDecimal.ZERO;
        BigDecimal gastado = (presupuesto != null) ? presupuesto.getMontoGastado() : BigDecimal.ZERO;

        return EtapaResponseDTO.builder()
                .idEtapa(etapa.getIdEtapa())
                .idProyecto(etapa.getProyecto().getIdProyecto())
                .nombre(etapa.getNombre())
                .descripcion(etapa.getDescripcion())
                .numeroOrden(etapa.getNumeroOrden())
                .fechaInicioPlan(etapa.getFechaInicioPlan())
                .fechaFinPlan(etapa.getFechaFinPlan())
                .estado(etapa.getEstado())
                .porcentajeAvance(etapa.getPorcentajeAvance())
                .presupuestoAsignado(asignado)
                .presupuestoGastado(gastado)
                .build();
    }

    // Convierte Entidad (con su lista de presupuestos) a DTO
    public EtapaResponseDTO toResponse(Etapa etapa) {
        // Lógica de Agregación: Sumar todos los presupuestos asignados a esta etapa
        // Si la lista es null o vacía, reduce a Cero.
        BigDecimal totalAsignado = BigDecimal.ZERO;
        BigDecimal totalGastado = BigDecimal.ZERO;

        if (etapa.getPresupuestos() != null) {
            totalAsignado = etapa.getPresupuestos().stream()
                    .map(p -> p.getMontoAprobado())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalGastado = etapa.getPresupuestos().stream()
                    .map(p -> p.getMontoGastado() != null ? p.getMontoGastado() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return EtapaResponseDTO.builder()
                .idEtapa(etapa.getIdEtapa())
                .idProyecto(etapa.getProyecto().getIdProyecto())
                .nombre(etapa.getNombre())
                .descripcion(etapa.getDescripcion())
                .numeroOrden(etapa.getNumeroOrden())
                .fechaInicioPlan(etapa.getFechaInicioPlan())
                .fechaFinPlan(etapa.getFechaFinPlan())
                .fechaInicioReal(etapa.getFechaInicioReal()) // Agregamos fechas reales
                .fechaFinReal(etapa.getFechaFinReal())
                .estado(etapa.getEstado())
                .porcentajeAvance(etapa.getPorcentajeAvance())
                .presupuestoAsignado(totalAsignado) // Suma calculada
                .presupuestoGastado(totalGastado)   // Suma calculada
                .build();
    }
}