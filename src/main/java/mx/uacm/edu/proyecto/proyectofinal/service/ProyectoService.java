package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;

public interface ProyectoService {
    // Método disparador que recalcula todo (Avance y Estado)
    void recalcularEstadoProyecto(Long idProyecto);

    // RF-08: Obtener reporte ejecutivo
    DashboardDTO obtenerDashboard(Long idProyecto);
}