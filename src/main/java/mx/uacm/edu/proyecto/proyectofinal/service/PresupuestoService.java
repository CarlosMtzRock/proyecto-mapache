package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoUpdateDTO;

public interface PresupuestoService {

    // Obtener el presupuesto asociado a una etapa
    PresupuestoDTO obtenerPresupuestoPorEtapa(Long idEtapa);

    // Obtener un presupuesto por su propio ID
    PresupuestoDTO obtenerPresupuestoPorId(Long idPresupuesto);

    // Actualizar los montos de un presupuesto
    PresupuestoDTO actualizarPresupuesto(Long idPresupuesto, PresupuestoUpdateDTO dto);
}
