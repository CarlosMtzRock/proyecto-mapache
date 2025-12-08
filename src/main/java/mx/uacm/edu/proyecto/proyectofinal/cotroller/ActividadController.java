package mx.uacm.edu.proyecto.proyectofinal.cotroller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadAvanceDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.model.Actividad;
import mx.uacm.edu.proyecto.proyectofinal.service.ActividadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    /**
     * Endpoint: POST /api/v1/etapas/{idEtapa}/actividades
     * @param idEtapa El ID de la etapa donde se agregara la actividad.
     * @param dto El JSON con los datos de la actividad (Nombre, Fechas, Orden).
     * @return 201 Created con la actividad creada.
     */
    @PostMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<Actividad> crearActividad(
            @PathVariable Long idEtapa,
            @Valid @RequestBody ActividadRequestDTO dto) {

        Actividad nueva = actividadService.crearActividad(idEtapa, dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    /**
     * Endpoint: GET /api/v1/etapas/{idEtapa}/actividades
     * @param idEtapa El ID de la etapa de la que se listaran las actividades.
     * @return 200 OK con la lista de actividades.
     */
    @GetMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<List<Actividad>> listarActividades(@PathVariable Long idEtapa) {
        return ResponseEntity.ok(actividadService.listarPorEtapa(idEtapa));
    }

    /**
     * Endpoint: PATCH /api/v1/actividades/{idActividad}/avance
     * @param idActividad El ID de la actividad que se actualizara.
     * @param dto El JSON con el nuevo avance de la actividad.
     * @return 200 OK con la actividad actualizada.
     */
    @PatchMapping("/actividades/{idActividad}/avance")
    public ResponseEntity<Actividad> actualizarAvance(
            @PathVariable Long idActividad,
            @Valid @RequestBody ActividadAvanceDTO dto) {

        Actividad actualizada = actividadService.actualizarAvance(idActividad, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Endpoint: DELETE /api/v1/actividades/{idActividad}
     * @param idActividad El ID de la actividad que se eliminara.
     * @return 204 No Content.
     */
    @DeleteMapping("/actividades/{idActividad}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Long idActividad) {
        actividadService.eliminarActividad(idActividad);
        return ResponseEntity.noContent().build();
    }
}
