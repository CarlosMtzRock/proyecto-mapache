package mx.uacm.edu.proyecto.proyectofinal.cotroller;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    /**
     * RF-08: Dashboard de Proyecto
     * Endpoint: GET /api/v1/proyectos/{idProyecto}/dashboard
     */
    @GetMapping("/{idProyecto}/dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard(@PathVariable Long idProyecto) {

        DashboardDTO reporte = proyectoService.obtenerDashboard(idProyecto);

        return ResponseEntity.ok(reporte);
    }
}