package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaActualizarDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;

import java.util.List;

public interface EtapaService {

    // RF-01: Crear etapa
    EtapaResponseDTO crearEtapa(Long idProyecto, EtapaRequestDTO dto);

    // RF-02: Listar etapas
    List<EtapaResponseDTO> listarEtapasPorProyecto(Long idProyecto);

    // Obtener una sola etapa por su ID
    EtapaResponseDTO obtenerEtapaPorId(Long idEtapa);

    // RF-04: Actualizar etapa (Datos generales o Cambio de Estado)
    EtapaResponseDTO actualizarEtapa(Long idEtapa, EtapaActualizarDTO dto);

    // RF-06: Reordenar etapa manual
    void reordenarEtapa(Long idEtapa, Integer nuevoOrden);

    // RF-07: Eliminar etapa solo si cumple RV-04
    void eliminarEtapa(Long idEtapa);
}
