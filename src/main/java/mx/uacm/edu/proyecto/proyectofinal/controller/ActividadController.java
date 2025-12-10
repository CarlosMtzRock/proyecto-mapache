package mx.uacm.edu.proyecto.proyectofinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadAvanceDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.ActividadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    /**
     * Crear una nueva actividad en una etapa.
     * @param idEtapa El ID de la etapa donde se agregara la actividad.
     * @param actividadRequestDTO DTO con los datos de la actividad a crear.
     * @return 201 Created con la actividad creada.
     */
    @PostMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<ActividadResponseDTO> crearActividad(
            @PathVariable Long idEtapa,
            @Valid @RequestBody ActividadRequestDTO actividadRequestDTO) {
        ActividadResponseDTO nuevaActividad = actividadService.crearActividad(idEtapa, actividadRequestDTO);
        return new ResponseEntity<>(nuevaActividad, HttpStatus.CREATED);
    }

    /**
     * Listar todas las actividades de una etapa.
     * @param idEtapa El ID de la etapa de la que se listaran las actividades.
     * @return 200 OK con la lista de actividades.
     */
    @GetMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<List<ActividadResponseDTO>> listarActividadesPorEtapa(@PathVariable Long idEtapa) {
        List<ActividadResponseDTO> actividades = actividadService.listarActividadesPorEtapa(idEtapa);
        return ResponseEntity.ok(actividades);
    }

    /**
     * Obtener una actividad especifica por su ID.
     * @param idActividad El ID de la actividad a buscar.
     * @return 200 OK con los datos de la actividad.
     */
    @GetMapping("/actividades/{idActividad}")
    public ResponseEntity<ActividadResponseDTO> obtenerActividadPorId(@PathVariable Long idActividad) {
        ActividadResponseDTO actividad = actividadService.obtenerActividadPorId(idActividad);
        return ResponseEntity.ok(actividad);
    }

    /**
     * Actualizar los datos generales de una actividad.
     * @param idActividad El ID de la actividad a actualizar.
     * @param actividadRequestDTO DTO con los nuevos datos de la actividad.
     * @return 200 OK con la actividad actualizada.
     */
    @PutMapping("/actividades/{idActividad}")
    public ResponseEntity<ActividadResponseDTO> actualizarActividad(
            @PathVariable Long idActividad,
            @Valid @RequestBody ActividadRequestDTO actividadRequestDTO) {
        ActividadResponseDTO actividadActualizada = actividadService.actualizarActividad(idActividad, actividadRequestDTO);
        return ResponseEntity.ok(actividadActualizada);
    }

    /**
     * Endpoint especifico para actualizar solo el avance de una actividad.
     * @param idActividad El ID de la actividad a actualizar.
     * @param avanceDTO DTO con el nuevo porcentaje de avance.
     * @return 200 OK con la actividad actualizada.
     */
    @PatchMapping("/actividades/{idActividad}/avance")
    public ResponseEntity<ActividadResponseDTO> actualizarAvance(
            @PathVariable Long idActividad,
            @Valid @RequestBody ActividadAvanceDTO avanceDTO) {
        ActividadResponseDTO actividadActualizada = actividadService.actualizarAvance(idActividad, avanceDTO);
        return ResponseEntity.ok(actividadActualizada);
    }

    /**
     * Eliminar una actividad.
     * @param idActividad El ID de la actividad a eliminar.
     * @return 204 No Content si la eliminacion fue exitosa.
     */
    @DeleteMapping("/actividades/{idActividad}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Long idActividad) {
        actividadService.eliminarActividad(idActividad);
        return ResponseEntity.noContent().build();
    }
}
