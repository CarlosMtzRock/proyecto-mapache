package mx.uacm.edu.proyecto.proyectofinal.cotroller;

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
@RequiredArgsConstructor // Inyeccion de dependencias por constructor
public class EtapaController {

    private final EtapaService etapaService;

    /**
     * RF-01: Crear Etapa
     * Endpoint: POST /api/v1/proyectos/{idProyecto}/etapas
     * * @param idProyecto El ID del proyecto donde se agregara la etapa.
     * @param dto        El JSON con los datos de la etapa (Nombre, Fechas, Orden).
     * @return 201 Created con la etapa creada y su presupuesto inicial.
     */
    @PostMapping("/proyectos/{idProyecto}/etapas")
    public ResponseEntity<EtapaResponseDTO> crearEtapa(
            @PathVariable Long idProyecto,
            @Valid @RequestBody EtapaRequestDTO dto) {

        // 1. Llamamos al servicio
        EtapaResponseDTO response = etapaService.crearEtapa(idProyecto, dto);

        // 2. Retornamos respuesta HTTP 201
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * RF-02: Listar Etapas
     * Endpoint: GET /api/v1/proyectos/{idProyecto}/etapas
     */
    @GetMapping("/proyectos/{idProyecto}/etapas")
    public ResponseEntity<List<EtapaResponseDTO>> listarEtapas(@PathVariable Long idProyecto) {

        List<EtapaResponseDTO> etapas = etapaService.listarEtapasPorProyecto(idProyecto);

        // Si la lista está vacia, devuelve 200 OK con array vacío []
        return ResponseEntity.ok(etapas);
    }

    /**
     * RF-04: Actualizar Etapa (Cambio de estado o info)
     * Endpoint: PATCH /api/v1/etapas/{idEtapa}
     */
    @PatchMapping("/etapas/{idEtapa}")
    public ResponseEntity<EtapaResponseDTO> actualizarEtapa(
            @PathVariable Long idEtapa,
            @RequestBody EtapaActualizarDTO dto) { // @Valid opcional si agregas constraints al DTO

        EtapaResponseDTO response = etapaService.actualizarEtapa(idEtapa, dto);

        return ResponseEntity.ok(response);
    }

    /**
     * RF-06: Reordenar Etapa Manualmente
     * Endpoint: PATCH /api/v1/etapas/{idEtapa}/reordenar
     * Body: { "nuevoOrden": 3 }
     */
    @PatchMapping("/etapas/{idEtapa}/reordenar")
    public ResponseEntity<Void> reordenarEtapa(
            @PathVariable Long idEtapa,
            @RequestBody Map<String, Integer> body) {

        Integer nuevoOrden = body.get("nuevoOrden");
        if (nuevoOrden == null) {
            throw new IllegalArgumentException("El campo 'nuevoOrden' es obligatorio.");
        }

        etapaService.reordenarEtapa(idEtapa, nuevoOrden);

        return ResponseEntity.ok().build();
    }

    /**
     * RF-07: Eliminar Etapa
     * Endpoint: DELETE /api/v1/etapas/{idEtapa}
     */
    @DeleteMapping("/etapas/{idEtapa}")
    public ResponseEntity<Void> eliminarEtapa(@PathVariable Long idEtapa) {

        etapaService.eliminarEtapa(idEtapa);

        // Retornamos 204 No Content (Estándar para borrados exitosos)
        return ResponseEntity.noContent().build();
    }
}