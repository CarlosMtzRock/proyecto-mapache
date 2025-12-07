package mx.uacm.edu.proyecto.proyectofinal.mapper;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EtapaMapper {

    // Inyección de dependencias dentro del mapper
    private final PresupuestoMapper presupuestoMapper;

    public Etapa toEntity(EtapaRequestDTO dto, Proyecto proyecto) {
        return Etapa.builder()
                .proyecto(proyecto)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .numeroOrden(dto.getNumeroOrden())
                .fechaInicioPlan(dto.getFechaInicioPlan())
                .fechaFinPlan(dto.getFechaFinPlan())
                .estado(EstadoEtapa.PLANIFICADA)
                .porcentajeAvance(0)
                .build();
    }

    // Versión 1: Cuando recibimos Etapa + Presupuesto específico
    public EtapaResponseDTO toResponse(Etapa etapa, Presupuesto presupuesto) {
        return buildResponse(etapa,
                presupuesto != null ? presupuesto.getMontoAprobado() : BigDecimal.ZERO,
                presupuesto != null ? presupuesto.getMontoGastado() : BigDecimal.ZERO);
    }

    // Versión 2: Cuando recibimos solo Etapa (lista) y sumamos sus presupuestos
    public EtapaResponseDTO toResponse(Etapa etapa) {
        BigDecimal totalAsignado = BigDecimal.ZERO;
        BigDecimal totalGastado = BigDecimal.ZERO;

        if (etapa.getPresupuestos() != null && !etapa.getPresupuestos().isEmpty()) {
            totalAsignado = etapa.getPresupuestos().stream()
                    .map(Presupuesto::getMontoAprobado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalGastado = etapa.getPresupuestos().stream()
                    .map(p -> p.getMontoGastado() != null ? p.getMontoGastado() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return buildResponse(etapa, totalAsignado, totalGastado);
    }

    // Método privado para evitar repetir código de construcción
    private EtapaResponseDTO buildResponse(Etapa etapa, BigDecimal asignado, BigDecimal gastado) {
        return EtapaResponseDTO.builder()
                .idEtapa(etapa.getIdEtapa())
                .idProyecto(etapa.getProyecto().getIdProyecto())
                .nombre(etapa.getNombre())
                .descripcion(etapa.getDescripcion())
                .numeroOrden(etapa.getNumeroOrden())
                .fechaInicioPlan(etapa.getFechaInicioPlan())
                .fechaFinPlan(etapa.getFechaFinPlan())
                .fechaInicioReal(etapa.getFechaInicioReal())
                .fechaFinReal(etapa.getFechaFinReal())
                .estado(etapa.getEstado())
                .porcentajeAvance(etapa.getPorcentajeAvance())
                .presupuestoAsignado(asignado)
                .presupuestoGastado(gastado)
                .build();
    }
}