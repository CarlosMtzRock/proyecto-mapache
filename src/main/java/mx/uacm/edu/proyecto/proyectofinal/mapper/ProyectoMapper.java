package mx.uacm.edu.proyecto.proyectofinal.mapper;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO; // Lo definiremos en el siguiente paso
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Declaramos esta clase como un componente de Spring para que pueda ser inyectada.
@Component
public class ProyectoMapper {
    /**
     * Convierte una entidad Proyecto y datos calculados a un DTO para el Dashboard.
     */
    public DashboardDTO toDashboardDTO(Proyecto proyecto,
                                       Integer avanceCalculado,
                                       BigDecimal gastadoTotal,
                                       long etapasAtrasadas) {

        // Utilizamos el patrón Builder para construir el objeto DashboardDTO.
        return DashboardDTO.builder()
                // Mapea,os los datos del proyecto y los valores calculados al DTO.
                .idProyecto(proyecto.getIdProyecto())
                .nombreProyecto(proyecto.getNombre())
                .estadoProyecto(proyecto.getEstado())
                .avanceGlobal(avanceCalculado)
                .presupuestoTotal(proyecto.getPresupuestoTotalObjetivo())
                .gastoEjecutado(gastadoTotal)
                // Calculamos el presupuesto remanente restando el gasto total del presupuesto total.
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastadoTotal))
                .etapasConRetraso(etapasAtrasadas)
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                // Construye y devuelve el objeto DTO.
                .build();
    }
}
