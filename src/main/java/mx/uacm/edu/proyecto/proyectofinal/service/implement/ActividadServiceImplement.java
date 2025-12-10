package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadAvanceDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadResponseDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActividadServiceImplement implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final EtapaRepository etapaRepository;
    private final ActividadMapper actividadMapper;
    private final ProyectoService proyectoService;

    @Override
    @Transactional
    public ActividadResponseDTO crearActividad(Long idEtapa, ActividadRequestDTO dto) {
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        if (etapa.getEstado() == EstadoEtapa.COMPLETADA || etapa.getEstado() == EstadoEtapa.CANCELADA) {
            throw new ReglasNegocioException("No se pueden agregar actividades a una etapa terminada");
        }

        // Lógica de auto-inicio de etapa
        boolean esPrimeraActividad = !actividadRepository.existsByEtapaIdEtapa(idEtapa);
        if (etapa.getEstado() == EstadoEtapa.PLANIFICADA && esPrimeraActividad) {
            // Validamos las reglas del proyecto antes de iniciar la etapa
            String estadoProyecto = etapa.getProyecto().getEstado().toUpperCase();
            if (!"EN_PROGRESO".equals(estadoProyecto)) {
                throw new ReglasNegocioException(
                    "Error RN-06: No se puede iniciar la etapa (y crear la actividad) porque el proyecto esta en estado '" + estadoProyecto + "'."
                );
            }
            LocalDate limiteInferior = etapa.getFechaInicioPlan().minusDays(7);
            if (LocalDate.now().isBefore(limiteInferior)) {
                throw new ReglasNegocioException(
                    "Error RN-08: No se puede iniciar la etapa (y crear la actividad). La fecha actual es mas de 7 dias anterior a la fecha planificada (" + etapa.getFechaInicioPlan() + ")"
                );
            }

            etapa.setEstado(EstadoEtapa.EN_PROGRESO);
            etapa.setFechaInicioReal(LocalDate.now()); // Regla RA7
            etapaRepository.save(etapa);
        }

        Actividad actividad = actividadMapper.toEntity(dto, etapa);
        Actividad guardada = actividadRepository.save(actividad);

        recalcularAvanceEtapa(etapa);

        return actividadMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadResponseDTO> listarActividadesPorEtapa(Long idEtapa) {
        if (!etapaRepository.existsById(idEtapa)) {
            throw new ResourceNotFoundException("Etapa no encontrada con ID: " + idEtapa);
        }
        return actividadRepository.findByEtapaIdEtapa(idEtapa).stream()
                .map(actividadMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadResponseDTO obtenerActividadPorId(Long idActividad) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con ID: " + idActividad));
        return actividadMapper.toResponse(actividad);
    }

    @Override
    @Transactional
    public ActividadResponseDTO actualizarActividad(Long idActividad, ActividadRequestDTO dto) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        
        actividadMapper.updateEntity(dto, actividad);
        
        Actividad actualizada = actividadRepository.save(actividad);
        return actividadMapper.toResponse(actualizada);
    }

    @Override
    @Transactional
    public void eliminarActividad(Long idActividad) {
        Actividad act = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        Etapa etapa = act.getEtapa();
        actividadRepository.delete(act);
        recalcularAvanceEtapa(etapa);
    }

    @Override
    @Transactional
    public ActividadResponseDTO actualizarAvance(Long idActividad, ActividadAvanceDTO dto) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        actividad.setPorcentajeAvance(dto.getNuevoAvance());

        if (dto.getNuevoAvance() == 100) {
            actividad.setEstado("COMPLETADA");
            actividad.setFechaFinReal(LocalDate.now());
        } else if (dto.getNuevoAvance() > 0 && !"COMPLETADA".equals(actividad.getEstado())) {
            actividad.setEstado("EN_PROGRESO");
            if (actividad.getFechaInicioReal() == null) {
                actividad.setFechaInicioReal(LocalDate.now());
            }
        }

        Actividad actualizada = actividadRepository.save(actividad);
        recalcularAvanceEtapa(actividad.getEtapa());
        return actividadMapper.toResponse(actualizada);
    }

    private void recalcularAvanceEtapa(Etapa etapa) {
        List<Actividad> actividades = actividadRepository.findByEtapaIdEtapa(etapa.getIdEtapa());

        if (actividades.isEmpty()) {
            etapa.setPorcentajeAvance(0);
        } else {
            double sumaAvance = actividades.stream()
                    .mapToInt(Actividad::getPorcentajeAvance)
                    .sum();
            int nuevoPromedioEtapa = (int) (sumaAvance / actividades.size());
            etapa.setPorcentajeAvance(nuevoPromedioEtapa);
        }

        if (etapa.getPorcentajeAvance() == 100) {
            if (etapa.getEstado() != EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.COMPLETADA);
                etapa.setFechaFinReal(LocalDate.now());
            }
        } else {
            if (etapa.getEstado() == EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.EN_PROGRESO);
                etapa.setFechaFinReal(null);
            }
        }

        etapaRepository.save(etapa);
        proyectoService.recalcularEstadoProyecto(etapa.getProyecto().getIdProyecto());
    }
}
