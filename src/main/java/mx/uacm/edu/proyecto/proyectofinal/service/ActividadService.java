package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadAvanceDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.ActividadResponseDTO;

import java.util.List;

public interface ActividadService {

    // Metodos del CRUD
    ActividadResponseDTO crearActividad(Long idEtapa, ActividadRequestDTO dto);
    List<ActividadResponseDTO> listarActividadesPorEtapa(Long idEtapa);
    ActividadResponseDTO obtenerActividadPorId(Long idActividad);
    ActividadResponseDTO actualizarActividad(Long idActividad, ActividadRequestDTO dto);
    void eliminarActividad(Long idActividad);

    // Metodos de Logica de Negocio
    ActividadResponseDTO actualizarAvance(Long idActividad, ActividadAvanceDTO dto);
}
