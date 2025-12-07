package mx.uacm.edu.proyecto.proyectofinal.service.implement;

import lombok.RequiredArgsConstructor;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaActualizarDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaRequestDTO;
import mx.uacm.edu.proyecto.proyectofinal.dto.EtapaResponseDTO;
import mx.uacm.edu.proyecto.proyectofinal.exception.ReglasNegocioException;
import mx.uacm.edu.proyecto.proyectofinal.exception.ResourceNotFoundException;
import mx.uacm.edu.proyecto.proyectofinal.mapper.EtapaMapper;
import mx.uacm.edu.proyecto.proyectofinal.model.EstadoEtapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Etapa;
import mx.uacm.edu.proyecto.proyectofinal.model.Presupuesto;
import mx.uacm.edu.proyecto.proyectofinal.model.Proyecto;
import mx.uacm.edu.proyecto.proyectofinal.repository.ActividadRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.EtapaRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.PresupuestoRepository;
import mx.uacm.edu.proyecto.proyectofinal.repository.ProyectoRepository;
import mx.uacm.edu.proyecto.proyectofinal.service.EtapaService;
import mx.uacm.edu.proyecto.proyectofinal.service.ProyectoService;
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
    private final ActividadRepository actividadRepository;

    private final ProyectoService proyectoService;

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

        // --- INICIO LÓGICA RN-09 (Techo Presupuestal) ---
        if (montoInicial.compareTo(BigDecimal.ZERO) > 0) {
            // A. Obtenemos el techo del proyecto
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();

            // B. Obtenemos cuánto llevamos gastado/asignado hasta ahora en las OTRAS etapas
            BigDecimal sumaActual = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);

            // C. Calculamos el nuevo total hipotético
            BigDecimal nuevoTotal = sumaActual.add(montoInicial);

            // D. Comparamos
            if (nuevoTotal.compareTo(techoProyecto) > 0) {
                throw new ReglasNegocioException(
                        String.format("Error RN-09: El presupuesto excede el límite del proyecto. " +
                                        "Límite: %s, Asignado Actual: %s, Intento: %s",
                                techoProyecto, sumaActual, montoInicial));
            }
        }

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


        // Disparar RA-09 y RA-08 (Recálculo global y posible reapertura)
        proyectoService.recalcularEstadoProyecto(idProyecto);

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
    @Override
    @Transactional
    public EtapaResponseDTO actualizarEtapa(Long idEtapa, EtapaActualizarDTO dto) {

        // 1. Buscar la etapa
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // 2. Actualizar datos básicos (Nombre/Descripción) si vienen en el DTO
        if (dto.getNombre() != null) etapa.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) etapa.setDescripcion(dto.getDescripcion());

        // 3. MÁQUINA DE ESTADOS (RN-04)
        if (dto.getNuevoEstado() != null && !dto.getNuevoEstado().equals(etapa.getEstado())) {

            EstadoEtapa estadoActual = etapa.getEstado();
            EstadoEtapa estadoNuevo = dto.getNuevoEstado();

            validarTransicion(estadoActual, estadoNuevo); // Método auxiliar privado

            // Lógica específica por cada transición
            switch (estadoNuevo) {
                case EN_PROGRESO:
                    // RN-03: Debe tener al menos 1 actividad
                    long totalActividades = etapaRepository.contarActividadesPorEtapa(idEtapa);
                    if (totalActividades == 0) {
                        throw new ReglasNegocioException("Error RN-03: No se puede iniciar la etapa sin actividades registradas.");
                    }
                    // RA-07: Registrar fecha inicio real automáticamente
                    if (etapa.getFechaInicioReal() == null) {
                        etapa.setFechaInicioReal(java.time.LocalDate.now());
                    }
                    break;

                case COMPLETADA:
                    // RN-05: El avance debe ser 100%
                    if (etapa.getPorcentajeAvance() < 100) {
                        throw new ReglasNegocioException("Error RN-05: No se puede completar la etapa si el avance es menor al 100%.");
                    }
                    // RV-05: No puede haber actividades pendientes
                    long pendientes = etapaRepository.contarActividadesPendientes(idEtapa);
                    if (pendientes > 0) {
                        throw new ReglasNegocioException("Error RV-05: Existen " + pendientes + " actividades pendientes. Ciérrelas o cancélelas antes de terminar la etapa.");
                    }
                    // RA-02: Registrar fecha fin real
                    etapa.setFechaFinReal(java.time.LocalDate.now());
                    break;

                // Casos como CANCELADA o EN_PAUSA son directos, no requieren validación extra aquí
            }

            // Aplicar el cambio
            etapa.setEstado(estadoNuevo);
        }

        Etapa etapaGuardada = etapaRepository.save(etapa);

        // Necesitamos el presupuesto para el mapper
        Presupuesto presupuesto = presupuestoRepository.findByEtapaIdEtapa(idEtapa).orElse(null);

        return etapaMapper.toResponse(etapaGuardada, presupuesto);
    }

    @Override
    @Transactional // RNF-01: CRÍTICO. Si algo falla, todo vuelve a como estaba.
    public void reordenarEtapa(Long idEtapa, Integer nuevoOrden) {

        // 1. Obtener la etapa y datos actuales
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        Long idProyecto = etapa.getProyecto().getIdProyecto();
        Integer ordenActual = etapa.getNumeroOrden();

        // Validar: Si el orden es el mismo, no hacemos nada
        if (ordenActual.equals(nuevoOrden)) return;

        // Validar: No permitir orden 0 o negativo (excepto nuestro temporal interno)
        if (nuevoOrden < 1) {
            throw new ReglasNegocioException("El número de orden debe ser mayor a 0.");
        }

        // 2. PASO TÁCTICO: Sacar la etapa de la fila temporalmente
        // La movemos a -1 para que no estorbe (evitar Duplicate entry error)
        etapa.setNumeroOrden(-1);
        etapaRepository.saveAndFlush(etapa); // Flush fuerza el cambio inmediato en BD

        // 3. REACOMODAR VECINAS
        if (nuevoOrden < ordenActual) {
            // CASO A: Mover hacia arriba (ej. de 5 a 2)
            // Las que estaban en 2, 3, 4 pasan a ser 3, 4, 5
            etapaRepository.empujarEtapasHaciaAbajo(idProyecto, nuevoOrden, ordenActual);
        } else {
            // CASO B: Mover hacia abajo (ej. de 2 a 5)
            // Las que estaban en 3, 4, 5 pasan a ser 2, 3, 4
            etapaRepository.jalarEtapasHaciaArriba(idProyecto, nuevoOrden, ordenActual);
        }

        // 4. INSERTAR EN DESTINO FINAL
        // Recuperamos la etapa (que ahora tiene orden -1 en memoria/bd) y la ponemos en su lugar
        etapa.setNumeroOrden(nuevoOrden);
        etapaRepository.save(etapa);
    }

    /**
     * Método Auxiliar para validar RN-04 (Transiciones permitidas)
     */
    private void validarTransicion(EstadoEtapa actual, EstadoEtapa nuevo) {
        // Regla: No se puede revivir una etapa finalizada
        if (actual == EstadoEtapa.COMPLETADA || actual == EstadoEtapa.CANCELADA) {
            throw new ReglasNegocioException("Error RN-04: La etapa está en estado final (" + actual + ") y no se puede modificar.");
        }

        // Matriz de Transiciones (Simplificada)
        boolean transicionValida = false;

        switch (actual) {
            case PLANIFICADA:
                // Solo puede pasar a EN_PROGRESO o CANCELADA
                if (nuevo == EstadoEtapa.EN_PROGRESO || nuevo == EstadoEtapa.CANCELADA) transicionValida = true;
                break;
            case EN_PROGRESO:
                // Puede pausarse, completarse o cancelarse
                if (nuevo == EstadoEtapa.EN_PAUSA || nuevo == EstadoEtapa.COMPLETADA || nuevo == EstadoEtapa.CANCELADA) transicionValida = true;
                break;
            case EN_PAUSA:
                // Solo puede volver a EN_PROGRESO
                if (nuevo == EstadoEtapa.EN_PROGRESO) transicionValida = true;
                break;
            default:
                break;
        }

        if (!transicionValida) {
            throw new ReglasNegocioException("Error RN-04: Transición de estado inválida: De " + actual + " a " + nuevo);
        }
    }

    @Override
    @Transactional
    public void eliminarEtapa(Long idEtapa) {

        // 1. Verificar existencia
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // 2. RV-04 (Parte A): Validar si tiene actividades
        if (actividadRepository.existsByEtapaIdEtapa(idEtapa)) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque tiene actividades asociadas. Elimínelas primero.");
        }

        // 3. RV-04 (Parte B): Validar si tiene presupuesto EJECUTADO
        // Nota: Permitimos borrar si el gastado es 0 (porque el sistema crea uno automático al inicio)
        boolean tieneGastos = presupuestoRepository.existsByEtapaIdEtapaAndMontoGastadoGreaterThan(idEtapa, BigDecimal.ZERO);

        if (tieneGastos) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque ya tiene ejecución presupuestal (Dinero gastado).");
        }

        // 4. Capturar ID del proyecto antes de borrar (para el recálculo)
        Long idProyecto = etapa.getProyecto().getIdProyecto();

        // 5. BORRADO
        // Gracias a CascadeType.ALL en la entidad Etapa, esto borrará también el presupuesto asociado (que está en 0)
        etapaRepository.delete(etapa);

        // 6. RA-09: Recalcular el estado del proyecto
        // (Al borrar una etapa, el promedio del proyecto cambia)
        proyectoService.recalcularEstadoProyecto(idProyecto);

        // Opcional: Aquí podrías llamar a un método "compactarOrdenes(idProyecto)"
        // para que si borras la 2, la 3 se vuelva 2. (Lo veremos en el siguiente paso de Reordenamiento).
    }
}