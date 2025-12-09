package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoServiceImplement implements ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final EtapaRepository etapaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ProyectoMapper proyectoMapper;

    @Override
    @Transactional
    public ProyectoResponseDTO crearProyecto(ProyectoRequestDTO proyectoRequestDTO) {
        Proyecto proyecto = proyectoMapper.toEntity(proyectoRequestDTO);
        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);
        return proyectoMapper.toResponse(proyectoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoResponseDTO> obtenerTodosLosProyectos() {
        return proyectoRepository.findAll().stream()
                .map(proyectoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoResponseDTO obtenerProyectoPorId(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
        return proyectoMapper.toResponse(proyecto);
    }

    @Override
    @Transactional
    public ProyectoResponseDTO actualizarProyecto(Long id, ProyectoRequestDTO proyectoRequestDTO) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
        
        proyectoMapper.updateEntity(proyectoRequestDTO, proyecto);
        
        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);
        return proyectoMapper.toResponse(proyectoActualizado);
    }

    @Override
    @Transactional
    public void eliminarProyecto(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
        
        // Opcional: Agregar validaciones antes de borrar. Por ejemplo, no borrar si tiene etapas.
        
        proyectoRepository.delete(proyecto);
    }

    @Override
    @Transactional
    public void recalcularEstadoProyecto(Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        List<Etapa> etapas = etapaRepository.findByProyectoIdProyecto(idProyecto);

        if (etapas.isEmpty()) {
            return;
        }

        double sumaAvance = etapas.stream().mapToInt(Etapa::getPorcentajeAvance).sum();
        int promedioGlobal = (int) (sumaAvance / etapas.size());

        boolean hayPendientes = etapas.stream()
                .anyMatch(e -> e.getEstado() != EstadoEtapa.COMPLETADA && e.getEstado() != EstadoEtapa.CANCELADA);

        if (!hayPendientes) {
            if (!"COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("COMPLETADO");
                proyecto.setFechaFinReal(LocalDate.now());
            }
        } else {
            if ("COMPLETADO".equals(proyecto.getEstado())) {
                proyecto.setEstado("EN_PROGRESO");
                proyecto.setFechaFinReal(null);
            }
        }

        proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO obtenerDashboard(Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        Double promedioAvance = etapaRepository.obtenerPromedioAvance(idProyecto);
        Long totalEtapas = etapaRepository.countByProyectoIdProyecto(idProyecto);
        Long etapasCompletadas = etapaRepository.countByProyectoIdProyectoAndEstado(idProyecto, EstadoEtapa.COMPLETADA);
        Long etapasAtrasadas = etapaRepository.contarEtapasAtrasadas(idProyecto);
        BigDecimal gastoTotal = presupuestoRepository.sumarGastoRealPorProyecto(idProyecto);

        return DashboardDTO.builder()
                .idProyecto(proyecto.getIdProyecto())
                .nombreProyecto(proyecto.getNombre())
                .estadoProyecto(proyecto.getEstado())
                .fechaFinEstimada(proyecto.getFechaFinEstimada())
                .avanceGlobal(promedioAvance.intValue())
                .etapasTotales(totalEtapas)
                .etapasCompletadas(etapasCompletadas)
                .etapasConRetraso(etapasAtrasadas)
                .presupuestoTotal(proyecto.getPresupuestoTotalObjetivo())
                .gastoEjecutado(gastoTotal)
                .presupuestoRestante(proyecto.getPresupuestoTotalObjetivo().subtract(gastoTotal))
                .build();
    }
}
