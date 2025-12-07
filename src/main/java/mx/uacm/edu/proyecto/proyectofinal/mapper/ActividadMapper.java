package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import org.springframework.stereotype.Component;

@Component
public class ActividadMapper {

    public Actividad toEntity(ActividadRequestDTO dto, Etapa etapa) {
        return Actividad.builder()
                .etapa(etapa)
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .idRequisito(dto.getIdRequisito())
                .fechaInicioProg(dto.getFechaInicioProg())
                .fechaFinProg(dto.getFechaFinProg())
                .porcentajeAvance(dto.getPorcentajeAvance() != null ? dto.getPorcentajeAvance() : 0)
                .estado("PENDIENTE") // Estado inicial
                .build();
    }

}