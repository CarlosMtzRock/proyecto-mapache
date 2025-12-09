package mx.uacm.edu.proyecto.proyectofinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaActualizarDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.EtapaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EtapaController {

    private final EtapaService etapaService;

    /**
     * RF-01: Crear una nueva etapa en un proyecto.
     * @param idProyecto El ID del proyecto donde se agregara la etapa.
     * @param etapaRequestDTO DTO con los datos de la etapa a crear.
     * @return 201 Created con la etapa creada.
     */
    @PostMapping("/proyectos/{idProyecto}/etapas")
    public ResponseEntity<EtapaResponseDTO> crearEtapa(
            @PathVariable Long idProyecto,
            @Valid @RequestBody EtapaRequestDTO etapaRequestDTO) {
        EtapaResponseDTO nuevaEtapa = etapaService.crearEtapa(idProyecto, etapaRequestDTO);
        return new ResponseEntity<>(nuevaEtapa, HttpStatus.CREATED);
    }

    /**
     * RF-02: Listar todas las etapas de un proyecto.
     * @param idProyecto El ID del proyecto del que se listaran las etapas.
     * @return 200 OK con la lista de etapas.
     */
    @GetMapping("/proyectos/{idProyecto}/etapas")
    public ResponseEntity<List<EtapaResponseDTO>> listarEtapasPorProyecto(@PathVariable Long idProyecto) {
        List<EtapaResponseDTO> etapas = etapaService.listarEtapasPorProyecto(idProyecto);
        return ResponseEntity.ok(etapas);
    }

    /**
     * Obtener una etapa especifica por su ID.
     * @param idEtapa El ID de la etapa a buscar.
     * @return 200 OK con los datos de la etapa.
     */
    @GetMapping("/etapas/{idEtapa}")
    public ResponseEntity<EtapaResponseDTO> obtenerEtapaPorId(@PathVariable Long idEtapa) {
        EtapaResponseDTO etapa = etapaService.obtenerEtapaPorId(idEtapa);
        return ResponseEntity.ok(etapa);
    }

    /**
     * RF-04: Actualizar una etapa (nombre, descripcion o estado).
     * @param idEtapa El ID de la etapa a actualizar.
     * @param etapaActualizarDTO DTO con los datos a modificar.
     * @return 200 OK con la etapa actualizada.
     */
    @PatchMapping("/etapas/{idEtapa}")
    public ResponseEntity<EtapaResponseDTO> actualizarEtapa(
            @PathVariable Long idEtapa,
            @Valid @RequestBody EtapaActualizarDTO etapaActualizarDTO) {
        EtapaResponseDTO etapaActualizada = etapaService.actualizarEtapa(idEtapa, etapaActualizarDTO);
        return ResponseEntity.ok(etapaActualizada);
    }

    /**
     * RF-06: Reordenar una etapa.
     * @param idEtapa El ID de la etapa a reordenar.
     * @param body Un JSON con la clave "nuevoOrden" y el valor numerico.
     * @return 200 OK si la operacion fue exitosa.
     */
    @PatchMapping("/etapas/{idEtapa}/reordenar")
    public ResponseEntity<Void> reordenarEtapa(
            @PathVariable Long idEtapa,
            @RequestBody Map<String, Integer> body) {
        Integer nuevoOrden = body.get("nuevoOrden");
        if (nuevoOrden == null) {
            // Opcional: lanzar una excepcion mas especifica
            throw new IllegalArgumentException("El campo 'nuevoOrden' es obligatorio en el cuerpo de la peticion.");
        }
        etapaService.reordenarEtapa(idEtapa, nuevoOrden);
        return ResponseEntity.ok().build();
    }

    /**
     * RF-07: Eliminar una etapa.
     * @param idEtapa El ID de la etapa a eliminar.
     * @return 204 No Content si la eliminacion fue exitosa.
     */
    @DeleteMapping("/etapas/{idEtapa}")
    public ResponseEntity<Void> eliminarEtapa(@PathVariable Long idEtapa) {
        etapaService.eliminarEtapa(idEtapa);
        return ResponseEntity.noContent().build();
    }
}
