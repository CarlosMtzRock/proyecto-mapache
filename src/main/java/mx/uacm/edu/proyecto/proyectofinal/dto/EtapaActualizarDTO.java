package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Data;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;

@Data
public class EtapaActualizarDTO {
    // El nuevo nombre de la etapa
    private String nombre;
    // La nueva descripción de la etapa
    private String descripcion;
    // El nuevo estado de la etapa
    private EstadoEtapa nuevoEstado; 
}