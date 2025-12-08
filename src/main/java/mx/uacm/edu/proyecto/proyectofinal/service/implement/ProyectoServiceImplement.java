package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.mapper.ProyectoMapper;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import mx.uacm.edu.proyecto.proyectofinal.repository.EtapaRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.PresupuestoRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.ProyectoRepository;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Este servicio se encarga de la logica de negocio a nivel de Proyecto
@Service
@RequiredArgsConstructor
public class ProyectoServiceImplement implements ProyectoService {

    // Inyectamos los repositorios y mappers que necesitamos
    private final ProyectoRepository proyectoRepository;
    private final EtapaRepository etapaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ProyectoMapper proyectoMapper;

    @Override
    @Transactional
    public void recalcularEstadoProyecto(Long idProyecto) {
        // Buscamos el proyecto que vamos a recalcular
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // Obtenemos todas sus etapas para hacer los calculos
        List<Etapa> etapas = etapaRepository.findByProyectoIdProyecto(idProyecto);

        // Si no hay etapas, no hay nada que calcular
        if (etapas.isEmpty()) {
            return;
        }

        // RA-09: Recalculamos el avance global del proyecto
        // Usamos un promedio simple del avance de todas las etapas
        double sumaAvance = etapas.stream().mapToInt(Etapa::getPorcentajeAvance).sum();
        int promedioGlobal = (int) (sumaAvance / etapas.size());

        // Aqui actualizariamos un campo 'porcentajeAvance' en la entidad Proyecto si existiera
        // proyecto.setPorcentajeAvance(promedioGlobal);

        // RA-05 y RA-08: Gestionamos el estado del proyecto automaticamente
        // Verificamos si todavia hay etapas pendientes (que no esten completadas o canceladas)
        boolean hayPendientes = etapas.stream()
                .anyMatch(e -> e.getEstado() != EstadoEtapa.COMPLETADA && e.getEstado() != EstadoEtapa.CANCELADA);

        if (!hayPendientes) {
            // RA-05: Si no hay pendientes, significa que todo termino, asi que cerramos el proyecto
            if (!"COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("COMPLETADO");
                proyecto.setFechaFinReal(LocalDate.now());
            }
        } else {
            // RA-08: Si hay pendientes, el proyecto debe estar activo
            // Esto es util si un proyecto ya estaba 'COMPLETADO' y se le agrega una nueva etapa
            if ("COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("EN_PROGRESO");
                proyecto.setFechaFinReal(null); // Limpiamos la fecha de fin porque se reabrio
            }
        }

        // Guardamos los cambios en el estado del proyecto
        proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional(readOnly = true) // Marcamos como solo lectura para optimizar la consulta
    public DashboardDTO obtenerDashboard(Long idProyecto) {

        // 1. Obtenemos la entidad principal del proyecto
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // 2. Obtenemos metricas relacionadas con las etapas (tiempo y cantidad)
        Double promedioAvance = etapaRepository.obtenerPromedioAvance(idProyecto);
        Long totalEtapas = etapaRepository.countByProyectoIdProyecto(idProyecto);
        Long etapasCompletadas = etapaRepository.countByProyectoIdProyectoAndEstado(idProyecto, EstadoEtapa.COMPLETADA);
        Long etapasAtrasadas = etapaRepository.contarEtapasAtrasadas(idProyecto);

        // 3. Obtenemos metricas financieras (dinero)
        BigDecimal gastoTotal = presupuestoRepository.sumarGastoRealPorProyecto(idProyecto);

        // 4. Construimos el DTO del Dashboard con toda la informacion recolectada
        // Usamos el patron Builder para que el codigo sea mas legible
        return DashboardDTO.builder()
                .idProyecto(proyecto.getIdProyecto())
                .nombreProyecto(proyecto.getNombre())
                .estadoProyecto(proyecto.getEstado())
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                // Metricas de avance
                .avanceGlobal(promedioAvance.intValue()) // Convertimos el double a un entero para el porcentaje
                .etapasTotales(totalEtapas)
                .etapasCompletadas(etapasCompletadas)
                .etapasConRetraso(etapasAtrasadas)
                // Metricas de finanzas
                .presupuestoTotal(proyecto.getPresupuestoTotalObjetivo())
                .gastoEjecutado(gastoTotal)
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastoTotal))
                .build();
    }
}
