package mx.uacm.edu.proyecto.proyectofinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    /**
     * Endpoint para crear un nuevo proyecto.
     * @param proyectoRequestDTO DTO con los datos del proyecto a crear.
     * @return 201 Created con el proyecto recien creado.
     */
    @PostMapping
    public ResponseEntity<ProyectoResponseDTO> crearProyecto(@Valid @RequestBody ProyectoRequestDTO proyectoRequestDTO) {
        ProyectoResponseDTO nuevoProyecto = proyectoService.crearProyecto(proyectoRequestDTO);
        return new ResponseEntity<>(nuevoProyecto, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener una lista de todos los proyectos.
     * @return 200 OK con la lista de proyectos.
     */
    @GetMapping
    public ResponseEntity<List<ProyectoResponseDTO>> obtenerTodosLosProyectos() {
        List<ProyectoResponseDTO> proyectos = proyectoService.obtenerTodosLosProyectos();
        return ResponseEntity.ok(proyectos);
    }

    /**
     * Endpoint para obtener un proyecto especifico por su ID.
     * @param id El ID del proyecto a buscar.
     * @return 200 OK con los datos del proyecto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> obtenerProyectoPorId(@PathVariable Long id) {
        ProyectoResponseDTO proyecto = proyectoService.obtenerProyectoPorId(id);
        return ResponseEntity.ok(proyecto);
    }

    /**
     * Endpoint para actualizar un proyecto existente.
     * @param id El ID del proyecto a actualizar.
     * @param proyectoRequestDTO DTO con los nuevos datos del proyecto.
     * @return 200 OK con el proyecto actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> actualizarProyecto(@PathVariable Long id, @Valid @RequestBody ProyectoRequestDTO proyectoRequestDTO) {
        ProyectoResponseDTO proyectoActualizado = proyectoService.actualizarProyecto(id, proyectoRequestDTO);
        return ResponseEntity.ok(proyectoActualizado);
    }

    /**
     * Endpoint para eliminar un proyecto.
     * @param id El ID del proyecto a eliminar.
     * @return 204 No Content si la eliminacion fue exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyecto(@PathVariable Long id) {
        proyectoService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-08: Dashboard de Proyecto
     * Endpoint: GET /api/v1/proyectos/{idProyecto}/dashboard
     * @param idProyecto El ID del proyecto para generar el dashboard.
     * @return 200 OK con el DTO del dashboard.
     */
    @GetMapping("/{idProyecto}/dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard(@PathVariable Long idProyecto) {
        DashboardDTO reporte = proyectoService.obtenerDashboard(idProyecto);
        return ResponseEntity.ok(reporte);
    }
}
