package mx.uacm.edu.proyecto.proyectofinal.service;

import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import java.math.BigDecimal;

public interface PresupuestoService {

    // Obtener presupuesto de una etapa
    Presupuesto obtenerPorEtapa(Long idEtapa);

    // Actualizar montos (Aprobado o Gastado)
    Presupuesto actualizarPresupuesto(Long idPresupuesto, BigDecimal nuevoAprobado, BigDecimal nuevoGastado);
}