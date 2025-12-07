package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.exception.ReglasNegocioException;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.repository.PresupuestoRepository;
import mx.uacm.edu.proyecto.proyectofinal.service.PresupuestoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PresupuestoServiceImplement implements PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public Presupuesto obtenerPorEtapa(Long idEtapa) {
        return presupuestoRepository.findByEtapaIdEtapa(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("No existe presupuesto asignado para la etapa con ID: " + idEtapa));
    }

    @Override
    @Transactional
    public Presupuesto actualizarPresupuesto(Long idPresupuesto, BigDecimal nuevoAprobado, BigDecimal nuevoGastado) {

        // 1. Buscar la entidad existente
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto con ID " + idPresupuesto + " no encontrado"));

        // 2. Validaciones Básicas (RN-07: No negativos)
        if (nuevoGastado != null && nuevoGastado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglasNegocioException("Error RN-07: El monto gastado no puede ser negativo.");
        }
        if (nuevoAprobado != null && nuevoAprobado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglasNegocioException("Error RN-07: El monto aprobado no puede ser negativo.");
        }

        // 3. RN-09: Techo Presupuestal (Solo si estamos intentando cambiar el monto aprobado)
        if (nuevoAprobado != null) {
            // A. Navegamos hacia arriba para obtener el límite del proyecto
            Proyecto proyecto = presupuesto.getEtapa().getProyecto();
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();
            Long idProyecto = proyecto.getIdProyecto();

            // B. Obtenemos la suma TOTAL actual de todos los presupuestos en BD
            BigDecimal sumaTotalActualEnBD = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);

            // C. Calculamos el "Total Proyectado"
            // Fórmula: (SumaTotalBD - MontoViejoDeEsteRegistro) + NuevoMonto
            BigDecimal montoViejo = presupuesto.getMontoAprobado();
            BigDecimal totalProyectado = sumaTotalActualEnBD.subtract(montoViejo).add(nuevoAprobado);

            // D. Validamos contra el techo
            if (totalProyectado.compareTo(techoProyecto) > 0) {
                // Cálculo de cuánto queda disponible para dar un mensaje útil
                BigDecimal disponibleReal = techoProyecto.subtract(sumaTotalActualEnBD.subtract(montoViejo));

                throw new ReglasNegocioException(
                        String.format("Error RN-09: El nuevo monto (%s) excede el presupuesto del proyecto. " +
                                        "Límite Proyecto: %s. Disponible para esta etapa: %s",
                                nuevoAprobado, techoProyecto, disponibleReal));
            }

            // Si pasa la validación, actualizamos
            presupuesto.setMontoAprobado(nuevoAprobado);
        }

        // 4. Actualizar Gasto (si aplica)
        if (nuevoGastado != null) {
            // Opcional: Podrías agregar una regla aquí como "No gastar más de lo aprobado"
            // if (nuevoGastado.compareTo(presupuesto.getMontoAprobado()) > 0) { ... warning ... }

            presupuesto.setMontoGastado(nuevoGastado);
        }

        // 5. Guardar cambios
        return presupuestoRepository.save(presupuesto);
    }
}