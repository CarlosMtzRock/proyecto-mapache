package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.DashboardDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ProyectoUpdateDTO;

import java.util.List;

public interface ProyectoService {

    // --- Metodos del CRUD ---
    ProyectoResponseDTO crearProyecto(ProyectoRequestDTO proyectoRequestDTO);
    List<ProyectoResponseDTO> obtenerTodosLosProyectos();
    ProyectoResponseDTO obtenerProyectoPorId(Long id);
    ProyectoResponseDTO actualizarProyecto(Long id, ProyectoRequestDTO proyectoRequestDTO);
    ProyectoResponseDTO actualizarParcialmenteProyecto(Long id, ProyectoUpdateDTO proyectoUpdateDTO); // Nuevo metodo para PATCH
    void eliminarProyecto(Long id);


    // --- Metodos de Logica de Negocio ---
    void recalcularEstadoProyecto(Long idProyecto);
    DashboardDTO obtenerDashboard(Long idProyecto);
}
