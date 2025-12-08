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

// Este servicio maneja toda la logica de negocio para los presupuestos
@Service
@RequiredArgsConstructor
public class PresupuestoServiceImplement implements PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public Presupuesto obtenerPorEtapa(Long idEtapa) {
        // Buscamos el presupuesto especifico para una etapa
        // Si no lo encontramos, lanzamos una excepcion clara
        return presupuestoRepository.findByEtapaIdEtapa(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("No existe presupuesto asignado para la etapa con ID: " + idEtapa));
    }

    @Override
    @Transactional
    public Presupuesto actualizarPresupuesto(Long idPresupuesto, BigDecimal nuevoAprobado, BigDecimal nuevoGastado) {

        // Buscamos la entidad que vamos a modificar
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto con ID " + idPresupuesto + " no encontrado"));

        // RN-07: Validaciones basicas para no permitir numeros negativos
        if (nuevoGastado != null && nuevoGastado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglasNegocioException("Error RN-07: El monto gastado no puede ser negativo");
        }
        if (nuevoAprobado != null && nuevoAprobado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglasNegocioException("Error RN-07: El monto aprobado no puede ser negativo");
        }

        // RN-09: Validamos el techo presupuestal del proyecto, pero solo si intentan cambiar el monto aprobado
        if (nuevoAprobado != null) {
            // Navegamos hacia arriba en el modelo para obtener el limite del proyecto
            Proyecto proyecto = presupuesto.getEtapa().getProyecto();
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();
            Long idProyecto = proyecto.getIdProyecto();

            // Obtenemos la suma total de lo que ya esta asignado en todas las etapas del proyecto
            BigDecimal sumaTotalActualEnBD = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);

            // Calculamos cual seria el nuevo total si aplicamos este cambio
            // Formula: (SumaTotal - MontoViejoDeEstaEtapa) + NuevoMonto
            BigDecimal montoViejo = presupuesto.getMontoAprobado();
            BigDecimal totalProyectado = sumaTotalActualEnBD.subtract(montoViejo).add(nuevoAprobado);

            // Comparamos el total proyectado contra el limite del proyecto
            if (totalProyectado.compareTo(techoProyecto) > 0) {
                // Calculamos cuanto dinero queda realmente disponible para dar un mensaje de error mas util
                BigDecimal disponibleReal = techoProyecto.subtract(sumaTotalActualEnBD.subtract(montoViejo));

                throw new ReglasNegocioException(
                        String.format("Error RN-09: El nuevo monto (%s) excede el presupuesto del proyecto. " +
                                        "Limite Proyecto: %s. Disponible para esta etapa: %s",
                                nuevoAprobado, techoProyecto, disponibleReal));
            }

            // Si todas las validaciones pasan, actualizamos el monto aprobado
            presupuesto.setMontoAprobado(nuevoAprobado);
        }

        // Actualizamos el monto gastado si se proporciono
        if (nuevoGastado != null) {

            presupuesto.setMontoGastado(nuevoGastado);
        }

        //Guardamos todos los cambios en la base de datos
        return presupuestoRepository.save(presupuesto);
    }
}
