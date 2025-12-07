package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.exception.ReglasNegocioException;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.mapper.EtapaMapper;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.repository.EtapaRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.PresupuestoRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.ProyectoRepository;
import mx.uacm.edu.proyecto.proyectofinal.service.EtapaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor // Inyección de dependencias por constructor (Lombok)
public class EtapaServiceImplent implements EtapaService {

    // Repositorios
    private final EtapaRepository etapaRepository;
    private final ProyectoRepository proyectoRepository;
    private final PresupuestoRepository presupuestoRepository;

    // Mapper para conversión de objetos
    private final EtapaMapper etapaMapper;

    /**
     * Crea una nueva etapa, valida reglas de negocio, reordena si es necesario
     * y asigna un presupuesto inicial.
     */
    @Override
    @Transactional // RNF-01: Garantiza atomicidad (si falla el presupuesto, no se guarda la etapa)
    public EtapaResponseDTO crearEtapa(Long idProyecto, EtapaRequestDTO dto) {

        // 1. RN-01: Validar que el proyecto exista
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe."));

        // 2. RV-02: Validar Coherencia de Fechas
        if (dto.getFechaFinPlan().isBefore(dto.getFechaInicioPlan())) {
            throw new ReglasNegocioException("Error de Fechas: La fecha de fin planificada no puede ser anterior a la fecha de inicio.");
        }

        // 3. RN-06: Validar Estado del Proyecto
        // Normalizamos a mayúsculas para evitar errores por "Cancelado" vs "CANCELADO"
        String estadoProyecto = proyecto.getEstado().toUpperCase();
        if ("CANCELADO".equals(estadoProyecto) || "CERRADO".equals(estadoProyecto)) {
            throw new ReglasNegocioException("No se pueden agregar etapas a un proyecto que está " + estadoProyecto);
        }

        // 4. RA-04: Reordenamiento Automatico en Cascada
        // Si el usuario intenta insertar en la posición 2 y ya existe la 2, empujamos 2->3, 3->4, etc
        boolean existeOrden = etapaRepository.existsByProyectoIdProyectoAndNumeroOrden(idProyecto, dto.getNumeroOrden());

        if (existeOrden) {
            // Ejecuta el UPDATE masivo en la BD
            etapaRepository.desplazarOrdenes(idProyecto, dto.getNumeroOrden());
        }

        // 5. Conversión DTO -> Entidad (Usando Mapper)
        Etapa nuevaEtapa = etapaMapper.toEntity(dto, proyecto);

        // 6. Guardado de la Etapa
        Etapa etapaGuardada = etapaRepository.save(nuevaEtapa);

        // 7. Gestión del Presupuesto Inicial
        // Validamos si viene nulo para asignar Cero
        BigDecimal montoInicial = dto.getPresupuestoInicial() != null ? dto.getPresupuestoInicial() : BigDecimal.ZERO;

        // Construcción manual del presupuesto
        Presupuesto presupuestoInicial = Presupuesto.builder()
                .etapa(etapaGuardada)
                .montoAprobado(montoInicial)
                .montoGastado(BigDecimal.ZERO) // Empieza sin gastos
                .fechaAprobacion(java.time.LocalDate.now())
                .estado("ACTIVO")
                .moneda("MXN")
                .build();

        // Guardado del Presupuesto
        presupuestoRepository.save(presupuestoInicial);

        // 8. Conversión Entidad -> DTO Respuesta (Usando Mapper)
        return etapaMapper.toResponse(etapaGuardada, presupuestoInicial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaResponseDTO> listarEtapasPorProyecto(Long idProyecto) {

        // 1. RN-01: Validar existencia del proyecto
        if (!proyectoRepository.existsById(idProyecto)) {
            throw new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe.");
        }

        // 2. Obtener entidades ordenadas de la BD
        List<Etapa> etapas = etapaRepository.findByProyectoIdProyectoOrderByNumeroOrdenAsc(idProyecto);

        // 3. Convertir a DTOs usando el Mapper
        return etapas.stream()
                .map(etapaMapper::toResponse)
                .toList();
    }
}