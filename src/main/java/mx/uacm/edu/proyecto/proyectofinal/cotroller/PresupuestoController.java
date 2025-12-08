package mx.uacm.edu.proyecto.proyectofinal.cotroller;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.service.PresupuestoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/presupuestos")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    /**
     * Actualizar montos de un presupuesto.
     * Validamos RN-07 y RN-09.
     * PATCH /api/v1/presupuestos/{idPresupuesto}
     */
    @PatchMapping("/{idPresupuesto}")
    public ResponseEntity<Presupuesto> actualizarPresupuesto(
            @PathVariable Long idPresupuesto,
            @RequestBody Map<String, BigDecimal> cambios) { // Usamos un Map simple para recibir solo lo que cambia

        BigDecimal nuevoAprobado = cambios.get("montoAprobado");
        BigDecimal nuevoGastado = cambios.get("montoGastado");

        Presupuesto actualizado = presupuestoService.actualizarPresupuesto(idPresupuesto, nuevoAprobado, nuevoGastado);

        return ResponseEntity.ok(actualizado);
    }
}