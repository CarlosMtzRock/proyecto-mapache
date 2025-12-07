package mx.uacm.edu.proyecto.proyectofinal.dto;

import lombok.Data;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;

@Data
public class EtapaActualizarDTO {
    private String nombre;
    private String descripcion;
    private EstadoEtapa nuevoEstado; // El estado al que queremos movernos
}