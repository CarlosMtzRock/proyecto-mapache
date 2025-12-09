package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import org.springframework.stereotype.Component;

@Component
public class ActividadMapper {

    // Convierte un DTO de peticion a una Entidad para crear una nueva actividad
    public Actividad toEntity(ActividadRequestDTO dto, Etapa etapa) {
        return Actividad.builder()
                .etapa(etapa)
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .idRequisito(dto.getIdRequisito())
                .fechaInicioProg(dto.getFechaInicioProg())
                .fechaFinProg(dto.getFechaFinProg())
                .porcentajeAvance(dto.getPorcentajeAvance() != null ? dto.getPorcentajeAvance() : 0)
                .estado("PENDIENTE")
                .build();
    }

    // Convierte una Entidad a un DTO de respuesta
    public ActividadResponseDTO toResponse(Actividad entity) {
        ActividadResponseDTO dto = new ActividadResponseDTO();
        dto.setIdActividad(entity.getIdActividad());
        dto.setIdEtapa(entity.getEtapa().getIdEtapa());
        dto.setIdRequisito(entity.getIdRequisito());
        dto.setNombre(entity.getNombre());
        dto.setTipo(entity.getTipo());
        dto.setFechaInicioProg(entity.getFechaInicioProg());
        dto.setFechaFinProg(entity.getFechaFinProg());
        dto.setFechaInicioReal(entity.getFechaInicioReal());
        dto.setFechaFinReal(entity.getFechaFinReal());
        dto.setPorcentajeAvance(entity.getPorcentajeAvance());
        dto.setEstado(entity.getEstado());
        return dto;
    }

    // Actualiza una entidad existente con datos de un DTO de peticion
    public void updateEntity(ActividadRequestDTO dto, Actividad entity) {
        entity.setNombre(dto.getNombre());
        entity.setTipo(dto.getTipo());
        entity.setIdRequisito(dto.getIdRequisito());
        entity.setFechaInicioProg(dto.getFechaInicioProg());
        entity.setFechaFinProg(dto.getFechaFinProg());

    }
}
