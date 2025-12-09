package mx.uacm.edu.proyecto.proyectofinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoUpdateDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.PresupuestoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    /**
     * Obtener el presupuesto de una etapa especifica.
     * @param idEtapa El ID de la etapa cuyo presupuesto se quiere obtener.
     * @return 200 OK con los datos del presupuesto.
     */
    @GetMapping("/etapas/{idEtapa}/presupuesto")
    public ResponseEntity<PresupuestoDTO> obtenerPresupuestoPorEtapa(@PathVariable Long idEtapa) {
        PresupuestoDTO presupuesto = presupuestoService.obtenerPresupuestoPorEtapa(idEtapa);
        return ResponseEntity.ok(presupuesto);
    }

    /**
     * Obtener un presupuesto por su propio ID.
     * @param idPresupuesto El ID del presupuesto a buscar.
     * @return 200 OK con los datos del presupuesto.
     */
    @GetMapping("/presupuestos/{idPresupuesto}")
    public ResponseEntity<PresupuestoDTO> obtenerPresupuestoPorId(@PathVariable Long idPresupuesto) {
        PresupuestoDTO presupuesto = presupuestoService.obtenerPresupuestoPorId(idPresupuesto);
        return ResponseEntity.ok(presupuesto);
    }

    /**
     * Actualizar los montos de un presupuesto.
     * @param idPresupuesto El ID del presupuesto a actualizar.
     * @param presupuestoUpdateDTO DTO con los montos a modificar.
     * @return 200 OK con el presupuesto actualizado.
     */
    @PutMapping("/presupuestos/{idPresupuesto}")
    public ResponseEntity<PresupuestoDTO> actualizarPresupuesto(
            @PathVariable Long idPresupuesto,
            @Valid @RequestBody PresupuestoUpdateDTO presupuestoUpdateDTO) {
        PresupuestoDTO presupuestoActualizado = presupuestoService.actualizarPresupuesto(idPresupuesto, presupuestoUpdateDTO);
        return ResponseEntity.ok(presupuestoActualizado);
    }
}
