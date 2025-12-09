package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProyectoMapper {

    // Convierte de DTO de peticion a Entidad para crear un nuevo proyecto
    public Proyecto toEntity(ProyectoRequestDTO dto) {
        return Proyecto.builder()
                .idCliente(dto.getIdCliente())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .metodologia(dto.getMetodologia())
                .tipo(dto.getTipo())
                .prioridad(dto.getPrioridad())
                .fechaInicio(dto.getFechaInicio())
                .fechaFinEstimada(dto.getFechaFinEstimada())
                .presupuestoTotalObjetivo(dto.getPresupuestoTotalObjetivo())
                .estado(dto.getEstado())
                .build();
    }

    // Convierte de Entidad a DTO de respuesta
    public ProyectoResponseDTO toResponse(Proyecto entity) {
        ProyectoResponseDTO dto = new ProyectoResponseDTO();
        dto.setIdProyecto(entity.getIdProyecto());
        dto.setIdCliente(entity.getIdCliente());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setMetodologia(entity.getMetodologia());
        dto.setTipo(entity.getTipo());
        dto.setPrioridad(entity.getPrioridad());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaFinEstimada(entity.getFechaFinEstimada());
        dto.setFechaFinReal(entity.getFechaFinReal());
        dto.setPresupuestoTotalObjetivo(entity.getPresupuestoTotalObjetivo());
        dto.setEstado(entity.getEstado());
        return dto;
    }

    // Actualiza una entidad existente con datos de un DTO de peticion
    public void updateEntity(ProyectoRequestDTO dto, Proyecto entity) {
        entity.setIdCliente(dto.getIdCliente());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setMetodologia(dto.getMetodologia());
        entity.setTipo(dto.getTipo());
        entity.setPrioridad(dto.getPrioridad());
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFinEstimada(dto.getFechaFinEstimada());
        entity.setPresupuestoTotalObjetivo(dto.getPresupuestoTotalObjetivo());
        entity.setEstado(dto.getEstado());
    }


    /**
     * Convierte una entidad Proyecto y datos calculados a un DTO para el Dashboard.
     */
    public DashboardDTO toDashboardDTO(Proyecto proyecto,
                                       Integer avanceCalculado,
                                       BigDecimal gastadoTotal,
                                       long etapasAtrasadas) {

        return DashboardDTO.builder()
                .idProyecto(proyecto.getIdProyecto())
                .nombreProyecto(proyecto.getNombre())
                .estadoProyecto(proyecto.getEstado())
                .avanceGlobal(avanceCalculado)
                .presupuestoTotal(proyecto.getPresupuestoTotalObjetivo())
                .gastoEjecutado(gastadoTotal)
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastadoTotal))
                .etapasConRetraso(etapasAtrasadas)
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                .build();
    }
}
