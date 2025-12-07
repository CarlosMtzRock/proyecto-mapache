package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO; // Lo definiremos en el siguiente paso
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProyectoMapper {


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
                // Calculamos el remanente
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastadoTotal))
                .etapasConRetraso(etapasAtrasadas)
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                .build();
    }
}