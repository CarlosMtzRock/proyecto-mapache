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

    // 1. Crear Actividad en una Etapa
    @PostMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<Actividad> crearActividad(
            @PathVariable Long idEtapa,
            @Valid @RequestBody ActividadRequestDTO dto) {

        Actividad nueva = actividadService.crearActividad(idEtapa, dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    // 2. Listar Actividades de una Etapa
    @GetMapping("/etapas/{idEtapa}/actividades")
    public ResponseEntity<List<Actividad>> listarActividades(@PathVariable Long idEtapa) {
        return ResponseEntity.ok(actividadService.listarPorEtapa(idEtapa));
    }

    // 3. Actualizar Avance (¡El más importante!)
    @PatchMapping("/actividades/{idActividad}/avance")
    public ResponseEntity<Actividad> actualizarAvance(
            @PathVariable Long idActividad,
            @Valid @RequestBody ActividadAvanceDTO dto) {

        Actividad actualizada = actividadService.actualizarAvance(idActividad, dto);
        return ResponseEntity.ok(actualizada);
    }

    // 4. Eliminar Actividad
    @DeleteMapping("/actividades/{idActividad}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Long idActividad) {
        actividadService.eliminarActividad(idActividad);
        return ResponseEntity.noContent().build();
    }
}