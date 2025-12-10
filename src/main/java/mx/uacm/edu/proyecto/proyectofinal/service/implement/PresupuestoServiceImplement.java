package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.PresupuestoUpdateDTO;
import mx.uacm.edu.proyecto.proyectofinal.exception.ReglasNegocioException;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.mapper.PresupuestoMapper;
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
    private final PresupuestoMapper presupuestoMapper;

    @Override
    @Transactional(readOnly = true)
    public PresupuestoDTO obtenerPresupuestoPorEtapa(Long idEtapa) {
        Presupuesto presupuesto = presupuestoRepository.findByEtapaIdEtapa(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("No existe presupuesto para la etapa con ID: " + idEtapa));
        return presupuestoMapper.toDTO(presupuesto);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoDTO obtenerPresupuestoPorId(Long idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + idPresupuesto));
        return presupuestoMapper.toDTO(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoDTO actualizarPresupuesto(Long idPresupuesto, PresupuestoUpdateDTO dto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto con ID " + idPresupuesto + " no encontrado"));

        // Guardamos el monto aprobado final para usarlo en las validaciones
        BigDecimal montoAprobadoFinal = presupuesto.getMontoAprobado();

        // Validacion del monto aprobado (si se proporciona)
        if (dto.getMontoAprobado() != null) {
            Proyecto proyecto = presupuesto.getEtapa().getProyecto();
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();
            Long idProyecto = proyecto.getIdProyecto();
            BigDecimal sumaTotalActualEnBD = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);
            BigDecimal montoViejo = presupuesto.getMontoAprobado();
            BigDecimal totalProyectado = sumaTotalActualEnBD.subtract(montoViejo).add(dto.getMontoAprobado());

            if (totalProyectado.compareTo(techoProyecto) > 0) {
                BigDecimal disponibleReal = techoProyecto.subtract(sumaTotalActualEnBD.subtract(montoViejo));
                throw new ReglasNegocioException(
                        String.format("Error RN-09: El nuevo monto (%s) excede el presupuesto del proyecto. Limite: %s. Disponible: %s",
                                dto.getMontoAprobado(), techoProyecto, disponibleReal));
            }
            montoAprobadoFinal = dto.getMontoAprobado(); // Actualizamos el valor para la siguiente validación
            presupuesto.setMontoAprobado(dto.getMontoAprobado());
        }

        // Validacion del monto gastado (si se proporciona)
        if (dto.getMontoGastado() != null) {
            // --> INICIO DE LA NUEVA LÓGICA <--
            // Regla: El monto gastado no puede ser mayor que el monto aprobado final.
            if (dto.getMontoGastado().compareTo(montoAprobadoFinal) > 0) {
                throw new ReglasNegocioException(
                    String.format("El monto gastado (%s) no puede superar el monto aprobado (%s).",
                                  dto.getMontoGastado(), montoAprobadoFinal)
                );
            }
            // --> FIN DE LA NUEVA LÓGICA <--
            presupuesto.setMontoGastado(dto.getMontoGastado());
        }

        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);
        return presupuestoMapper.toDTO(presupuestoActualizado);
    }
}
