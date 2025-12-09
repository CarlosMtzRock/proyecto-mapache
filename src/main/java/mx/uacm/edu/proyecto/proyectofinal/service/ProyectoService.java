package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;

import java.util.List;

public interface ProyectoService {

    // Metodos del CRUD
    ProyectoResponseDTO crearProyecto(ProyectoRequestDTO proyectoRequestDTO);
    List<ProyectoResponseDTO> obtenerTodosLosProyectos();
    ProyectoResponseDTO obtenerProyectoPorId(Long id);
    ProyectoResponseDTO actualizarProyecto(Long id, ProyectoRequestDTO proyectoRequestDTO);
    void eliminarProyecto(Long id);


    // Metodos de Logica de Negocio
    // Metodo disparador que recalcula todo (Avance y Estado)
    void recalcularEstadoProyecto(Long idProyecto);

    // RF-08: Obtener reporte ejecutivo
    DashboardDTO obtenerDashboard(Long idProyecto);
}
