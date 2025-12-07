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

@Service
@RequiredArgsConstructor
public class ProyectoServiceImplement implements ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final EtapaRepository etapaRepository;


    private final PresupuestoRepository presupuestoRepository;
    private final ProyectoMapper proyectoMapper;

    @Override
    @Transactional
    public void recalcularEstadoProyecto(Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        List<Etapa> etapas = etapaRepository.findByProyectoIdProyecto(idProyecto);

        if (etapas.isEmpty()) {
            return; // Nada que calcular
        }

        // 1. RA-09: Recálculo de Avance Global (Promedio simple)
        // Podría ser ponderado si las etapas tuvieran "peso", pero usaremos promedio simple.
        double sumaAvance = etapas.stream().mapToInt(Etapa::getPorcentajeAvance).sum();
        int promedioGlobal = (int) (sumaAvance / etapas.size());

        // Aquí asumiríamos que Proyecto tiene un campo 'porcentajeAvance'.
        // Si no lo tiene en la Entity, habría que agregarlo.
        // Por ahora lo simulamos o actualizamos solo el estado.
        // proyecto.setPorcentajeAvance(promedioGlobal);

        // 2. RA-05 y RA-08: Gestión de Estado Automático
        boolean hayPendientes = etapas.stream()
                .anyMatch(e -> e.getEstado() != EstadoEtapa.COMPLETADA && e.getEstado() != EstadoEtapa.CANCELADA);

        if (!hayPendientes) {
            // RA-05: Todas están completadas/canceladas -> CERRAR PROYECTO
            if (!"COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("COMPLETADO");
                proyecto.setFechaFinReal(LocalDate.now());
                System.out.println("🚀 Proyecto " + idProyecto + " marcado como COMPLETADO automáticamente.");
            }
        } else {
            // RA-08: Hay cosas pendientes -> ABRIR PROYECTO (Regresión)
            // Si estaba completado y agregaron una etapa nueva, vuelve a EN_PROGRESO
            if ("COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("EN_PROGRESO");
                proyecto.setFechaFinReal(null); // Limpiamos la fecha fin
                System.out.println("🔄 Proyecto " + idProyecto + " reactivado a EN_PROGRESO.");
            }
        }

        proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional(readOnly = true) // Importante: Solo lectura para mejorar rendimiento
    public DashboardDTO obtenerDashboard(Long idProyecto) {

        // 1. Obtener entidad base
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // 2. Obtener Métricas de Etapa (Tiempo y Cantidad)
        Double promedioAvance = etapaRepository.obtenerPromedioAvance(idProyecto);
        Long totalEtapas = etapaRepository.countByProyectoIdProyecto(idProyecto);
        Long etapasCompletadas = etapaRepository.countByProyectoIdProyectoAndEstado(idProyecto, EstadoEtapa.COMPLETADA);
        Long etapasAtrasadas = etapaRepository.contarEtapasAtrasadas(idProyecto);

        // 3. Obtener Métricas Financieras (Dinero)
        BigDecimal gastoTotal = presupuestoRepository.sumarGastoRealPorProyecto(idProyecto);

        // 4. Armar el DTO usando el Mapper
        // (Nota: Estamos reutilizando el mapper que creamos en el paso anterior, ajustando parámetros)

        return DashboardDTO.builder()
                .idProyecto(proyecto.getIdProyecto())
                .nombreProyecto(proyecto.getNombre())
                .estadoProyecto(proyecto.getEstado())
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                // Métricas
                .avanceGlobal(promedioAvance.intValue()) // Convertimos double a int (ej: 45.6 -> 45%)
                .etapasTotales(totalEtapas)
                .etapasCompletadas(etapasCompletadas)
                .etapasConRetraso(etapasAtrasadas)
                // Finanzas
                .presupuestoTotal(proyecto.getPresupuestoTotalObjetivo())
                .gastoEjecutado(gastoTotal)
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastoTotal))
                .build();
    }
}