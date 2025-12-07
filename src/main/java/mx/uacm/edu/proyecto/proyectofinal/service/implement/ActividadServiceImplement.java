package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadAvanceDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.exception.ReglasNegocioException;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.mapper.ActividadMapper;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import mx.uacm.edu.proyecto.proyectofinal.repository.ActividadRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.EtapaRepository;
import mx.uacm.edu.proyecto.proyectofinal.service.ActividadService;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActividadServiceImplement implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final EtapaRepository etapaRepository;
    private final ActividadMapper actividadMapper;

    private final ProyectoService proyectoService; // <--- Inyectar

    @Override
    @Transactional
    public Actividad crearActividad(Long idEtapa, ActividadRequestDTO dto) {
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // Regla: No agregar actividades a etapas cerradas
        if(etapa.getEstado() == EstadoEtapa.COMPLETADA || etapa.getEstado() == EstadoEtapa.CANCELADA){
            throw new ReglasNegocioException("No se pueden agregar actividades a una etapa terminada.");
        }

        Actividad actividad = actividadMapper.toEntity(dto, etapa);
        Actividad guardada = actividadRepository.save(actividad);

        // Importante: Al agregar una nueva actividad (con 0%), el promedio de la etapa baja.
        recalcularAvanceEtapa(etapa);

        return guardada;
    }

    @Override
    @Transactional
    public Actividad actualizarAvance(Long idActividad, ActividadAvanceDTO dto) {
        // 1. Obtener actividad
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        // 2. Actualizar el propio avance
        actividad.setPorcentajeAvance(dto.getNuevoAvance());

        // Lógica de estado de la actividad
        if (dto.getNuevoAvance() == 100) {
            actividad.setEstado("COMPLETADA");
            actividad.setFechaFinReal(LocalDate.now());
        } else if (dto.getNuevoAvance() > 0) {
            actividad.setEstado("EN_PROGRESO");
            if (actividad.getFechaInicioReal() == null) {
                actividad.setFechaInicioReal(LocalDate.now());
            }
        }

        Actividad actualizada = actividadRepository.save(actividad);

        // 3. EFECTO DOMINÓ: Recalcular Etapa (RA-02 y Recálculo de Avance)
        recalcularAvanceEtapa(actividad.getEtapa());

        return actualizada;
    }

    // Método Privado para la Lógica de la Etapa
    private void recalcularAvanceEtapa(Etapa etapa) {
        List<Actividad> actividades = actividadRepository.findByEtapaIdEtapa(etapa.getIdEtapa());

        if (actividades.isEmpty()) return;

        // A. Calcular Promedio
        double sumaAvance = actividades.stream()
                .mapToInt(Actividad::getPorcentajeAvance)
                .sum();

        int nuevoPromedioEtapa = (int) (sumaAvance / actividades.size());

        // B. Actualizar Etapa
        etapa.setPorcentajeAvance(nuevoPromedioEtapa);

        // C. RA-02: Auto-Completado
        if (nuevoPromedioEtapa == 100) {
            // Solo si no estaba ya completada (para no sobrescribir fecha fin real)
            if (etapa.getEstado() != EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.COMPLETADA);
                etapa.setFechaFinReal(LocalDate.now());
            }
        } else {
            // Si por error se reabrió una tarea, la etapa debe dejar de estar completada
            if (etapa.getEstado() == EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.EN_PROGRESO);
                etapa.setFechaFinReal(null);
            }
        }

        etapaRepository.save(etapa);

        // Propagar el cambio hacia arriba (Hacia el Proyecto)
        // Esto dispara RA-05 (Si la etapa se cerró, checa si el proyecto se cierra)
        proyectoService.recalcularEstadoProyecto(etapa.getProyecto().getIdProyecto());
    }

    // listar y eliminar
    @Override
    public List<Actividad> listarPorEtapa(Long idEtapa) {
        return actividadRepository.findByEtapaIdEtapa(idEtapa);
    }

    @Override
    public void eliminarActividad(Long idActividad) {
        Actividad act = actividadRepository.findById(idActividad)
                .orElseThrow(()-> new ResourceNotFoundException("Actividad no encontrada"));

        // Antes de borrar, guardamos la referencia a la etapa para recalcular después
        Etapa etapa = act.getEtapa();

        actividadRepository.delete(act);

        // Al borrar, el promedio cambia (sube), hay que recalcular
        recalcularAvanceEtapa(etapa);
    }
}