package mx.uacm.edu.proyecto.proyectofinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoUpdateDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @PostMapping
    public ResponseEntity<ProyectoResponseDTO> crearProyecto(@Valid @RequestBody ProyectoRequestDTO proyectoRequestDTO) {
        ProyectoResponseDTO nuevoProyecto = proyectoService.crearProyecto(proyectoRequestDTO);
        return new ResponseEntity<>(nuevoProyecto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProyectoResponseDTO>> obtenerTodosLosProyectos() {
        List<ProyectoResponseDTO> proyectos = proyectoService.obtenerTodosLosProyectos();
        return ResponseEntity.ok(proyectos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> obtenerProyectoPorId(@PathVariable Long id) {
        ProyectoResponseDTO proyecto = proyectoService.obtenerProyectoPorId(id);
        return ResponseEntity.ok(proyecto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> actualizarProyecto(@PathVariable Long id, @Valid @RequestBody ProyectoRequestDTO proyectoRequestDTO) {
        ProyectoResponseDTO proyectoActualizado = proyectoService.actualizarProyecto(id, proyectoRequestDTO);
        return ResponseEntity.ok(proyectoActualizado);
    }

    /**
     * Endpoint para actualizar parcialmente un proyecto.
     * @param id El ID del proyecto a actualizar.
     * @param proyectoUpdateDTO DTO con los campos a modificar.
     * @return 200 OK con el proyecto actualizado.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> actualizarParcialmenteProyecto(@PathVariable Long id, @Valid @RequestBody ProyectoUpdateDTO proyectoUpdateDTO) {
        ProyectoResponseDTO proyectoActualizado = proyectoService.actualizarParcialmenteProyecto(id, proyectoUpdateDTO);
        return ResponseEntity.ok(proyectoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyecto(@PathVariable Long id) {
        proyectoService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idProyecto}/dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard(@PathVariable Long idProyecto) {
        DashboardDTO reporte = proyectoService.obtenerDashboard(idProyecto);
        return ResponseEntity.ok(reporte);
    }
}
