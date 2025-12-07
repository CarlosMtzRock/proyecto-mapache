package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;

import java.util.List;

public interface EtapaService {

    // RF-01: Crear etapa
    EtapaResponseDTO crearEtapa(Long idProyecto, EtapaRequestDTO dto);

    // RF-02: Listar etapas
    List<EtapaResponseDTO> listarEtapasPorProyecto(Long idProyecto);
}