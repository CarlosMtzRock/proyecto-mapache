package mx.uacm.edu.proyecto.proyectofinal.cotroller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.EtapaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}