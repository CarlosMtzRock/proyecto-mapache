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

// aqui es donde pasa la magia de las actividades
@Service
@RequiredArgsConstructor
public class ActividadServiceImplement implements ActividadService {

    // nos traemos las herramientas que vamos a necesitar
    private final ActividadRepository actividadRepository;
    private final EtapaRepository etapaRepository;
    private final ActividadMapper actividadMapper;
    private final ProyectoService proyectoService;

    @Override
    @Transactional
    public Actividad crearActividad(Long idEtapa, ActividadRequestDTO dto) {
        // buscamos la etapa o si no, la quitamos
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // regla de oro: no puedes meterle cosas a una etapa ya muerta
        if(etapa.getEstado() == EstadoEtapa.COMPLETADA || etapa.getEstado() == EstadoEtapa.CANCELADA){
            throw new ReglasNegocioException("No se pueden agregar actividades a una etapa terminada");
        }

        // usamos el mapper para convertir el dto a la entidad chida
        Actividad actividad = actividadMapper.toEntity(dto, etapa);
        // y la guardamos en la base de datos
        Actividad guardada = actividadRepository.save(actividad);

        // ojo, al meter una actividad nueva (en 0%) el promedio de la etapa baja, asi que a recalcular
        recalcularAvanceEtapa(etapa);

        return guardada;
    }

    @Override
    @Transactional
    public Actividad actualizarAvance(Long idActividad, ActividadAvanceDTO dto) {
        // 1 primero encontramos la actividad que vamos a moverle
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        // 2 le actualizamos el avance con lo que nos mandaron
        actividad.setPorcentajeAvance(dto.getNuevoAvance());

        // ahora viene la magia de los estados
        if (dto.getNuevoAvance() == 100) {
            // si ya llego al 100, la marcamos como completada y le ponemos fecha de hoy
            actividad.setEstado("COMPLETADA");
            actividad.setFechaFinReal(LocalDate.now());
        } else if (dto.getNuevoAvance() > 0) {
            // si apenas empezo (mas de 0), la ponemos en progreso y si no tenia fecha de inicio, se la clavamos
            actividad.setEstado("EN_PROGRESO");
            if (actividad.getFechaInicioReal() == null) {
                actividad.setFechaInicioReal(LocalDate.now());
            }
        }

        // guardamos los cambios en la base de datos
        Actividad actualizada = actividadRepository.save(actividad);

        // 3 EFECTO DOMINO: esto mueve todo pa arriba, recalculamos la etapa (RA-02)
        recalcularAvanceEtapa(actividad.getEtapa());

        return actualizada;
    }

    // este metodo privado se encarga de recalcular todo lo de la etapa
    private void recalcularAvanceEtapa(Etapa etapa) {
        // traemos todas las actividades de la etapa para hacer cuentas
        List<Actividad> actividades = actividadRepository.findByEtapaIdEtapa(etapa.getIdEtapa());

        // si no hay actividades, pues no hacemos nada
        if (actividades.isEmpty()) return;

        // A a calcular el promedio de avance
        double sumaAvance = actividades.stream()
                .mapToInt(Actividad::getPorcentajeAvance)
                .sum();

        // y sacamos el promedio nuevo para la etapa
        int nuevoPromedioEtapa = (int) (sumaAvance / actividades.size());

        // B actualizamos el porcentaje de la etapa
        etapa.setPorcentajeAvance(nuevoPromedioEtapa);

        // C RA-02: Auto-Completado, si la etapa ya llego al 100
        if (nuevoPromedioEtapa == 100) {
            // solo si no estaba ya completada, pa no pisar la fecha fin real
            if (etapa.getEstado() != EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.COMPLETADA);
                etapa.setFechaFinReal(LocalDate.now());
            }
        } else {
            // si por alguna razon bajo del 100 (alguien reabrio una tarea), le quitamos el estado de completada
            if (etapa.getEstado() == EstadoEtapa.COMPLETADA) {
                etapa.setEstado(EstadoEtapa.EN_PROGRESO);
                etapa.setFechaFinReal(null);
            }
        }

        // guardamos los cambios de la etapa
        etapaRepository.save(etapa);

        // y ahora el cambio va pa'rriba, al proyecto
        // esto dispara RA-05 (Si la etapa se cerro, checa si el proyecto se cierra)
        proyectoService.recalcularEstadoProyecto(etapa.getProyecto().getIdProyecto());
    }

    // estos son mas sencillos, solo para listar y borrar
    @Override
    public List<Actividad> listarPorEtapa(Long idEtapa) {
        // este es facil, solo pedimos al repo la lista de actividades de la etapa y ya
        return actividadRepository.findByEtapaIdEtapa(idEtapa);
    }

    @Override
    public void eliminarActividad(Long idActividad) {
        // buscamos la actividad que quieren borrar, si no existe, error
        Actividad act = actividadRepository.findById(idActividad)
                .orElseThrow(()-> new ResourceNotFoundException("Actividad no encontrada"));

        // antes de que truene, guardamos la etapa pa poder recalcular despues
        Etapa etapa = act.getEtapa();

        // ahora si, adios actividad
        actividadRepository.delete(act);

        // como borramos una, el promedio cambia (chance y sube), asi que a recalcular todo
        recalcularAvanceEtapa(etapa);
    }
}
