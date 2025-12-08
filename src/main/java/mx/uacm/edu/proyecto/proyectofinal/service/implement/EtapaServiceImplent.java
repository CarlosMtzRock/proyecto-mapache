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

// Este servicio se encarga de toda la lógica de negocio relacionada con las Etapas
@Service
@RequiredArgsConstructor // Inyecta las dependencias finales a través del constructor
public class EtapaServiceImplent implements EtapaService {

    // Dependencias de repositorios para el acceso a datos
    private final EtapaRepository etapaRepository;
    private final ProyectoRepository proyectoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ActividadRepository actividadRepository;

    // Dependencia de otros servicios para colaborar
    private final ProyectoService proyectoService;

    // Mapper para convertir entre DTOs y Entidades
    private final EtapaMapper etapaMapper;

    /**
     * Crea una nueva etapa, aplicando validaciones y asignando un presupuesto inicial
     */
    @Override
    @Transactional // RNF-01: Asegura que la operación sea atómica. Si algo falla, se revierte todo
    public EtapaResponseDTO crearEtapa(Long idProyecto, EtapaRequestDTO dto) {

        // RN-01: Validamos que el proyecto asociado exista
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe"));

        // RV-02: Verificamos la coherencia de las fechas
        if (dto.getFechaFinPlan().isBefore(dto.getFechaInicioPlan())) {
            throw new ReglasNegocioException("Error de Fechas: La fecha de fin no puede ser anterior a la de inicio");
        }

        //  RN-06: No se deben agregar etapas a proyectos ya finalizados
        String estadoProyecto = proyecto.getEstado().toUpperCase();
        if ("CANCELADO".equals(estadoProyecto) || "CERRADO".equals(estadoProyecto)) {
            throw new ReglasNegocioException("No se pueden agregar etapas a un proyecto en estado " + estadoProyecto);
        }

        //  RA-04: Implementamos el reordenamiento automático
        // Si se intenta insertar en una posición ocupada, desplazamos las etapas existentes
        boolean existeOrden = etapaRepository.existsByProyectoIdProyectoAndNumeroOrden(idProyecto, dto.getNumeroOrden());
        if (existeOrden) {
            etapaRepository.desplazarOrdenes(idProyecto, dto.getNumeroOrden());
        }

        // Convertimos el DTO a una entidad Etapa para guardarla
        Etapa nuevaEtapa = etapaMapper.toEntity(dto, proyecto);

        // Persistimos la nueva etapa en la base de datos
        Etapa etapaGuardada = etapaRepository.save(nuevaEtapa);

        //  Gestionamos el presupuesto inicial de la etapa
        BigDecimal montoInicial = dto.getPresupuestoInicial() != null ? dto.getPresupuestoInicial() : BigDecimal.ZERO;

        //Lógica para la regla RN-09
        if (montoInicial.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();
            BigDecimal sumaActual = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);
            BigDecimal nuevoTotal = sumaActual.add(montoInicial);

            // Si el nuevo total supera el límite del proyecto, lanzamos una excepción
            if (nuevoTotal.compareTo(techoProyecto) > 0) {
                throw new ReglasNegocioException(
                        String.format("Error RN-09: El presupuesto excede el límite del proyecto. Límite: %s, Asignado: %s, Intento: %s",
                                techoProyecto, sumaActual, montoInicial));
            }
        }

        // Creamos y guardamos el registro del presupuesto inicial
        Presupuesto presupuestoInicial = Presupuesto.builder()
                .etapa(etapaGuardada)
                .montoAprobado(montoInicial)
                .montoGastado(BigDecimal.ZERO)
                .fechaAprobacion(java.time.LocalDate.now())
                .estado("ACTIVO")
                .moneda("MXN")
                .build();
        presupuestoRepository.save(presupuestoInicial);

        // Disparamos RA-09 y RA-08: Recalculamos el estado global del proyecto
        proyectoService.recalcularEstadoProyecto(idProyecto);

        // Devolvemos un DTO con la información de la etapa y su presupuesto
        return etapaMapper.toResponse(etapaGuardada, presupuestoInicial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaResponseDTO> listarEtapasPorProyecto(Long idProyecto) {
        // RN-01: Validamos que el proyecto exista antes de listar sus etapas
        if (!proyectoRepository.existsById(idProyecto)) {
            throw new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe");
        }

        // Obtenemos las etapas ordenadas por su número de orden
        List<Etapa> etapas = etapaRepository.findByProyectoIdProyectoOrderByNumeroOrdenAsc(idProyecto);

        // Convertimos la lista de entidades a una lista de DTOs para la respuesta
        return etapas.stream()
                .map(etapaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EtapaResponseDTO actualizarEtapa(Long idEtapa, EtapaActualizarDTO dto) {
        // Buscamos la etapa a actualizar
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // Actualizamos los campos básicos si se proporcionaron en el DTO
        if (dto.getNombre() != null) etapa.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) etapa.setDescripcion(dto.getDescripcion());

        // RN-04: Gestionamos la transición de estados si se solicita un cambio
        if (dto.getNuevoEstado() != null && !dto.getNuevoEstado().equals(etapa.getEstado())) {
            EstadoEtapa estadoActual = etapa.getEstado();
            EstadoEtapa estadoNuevo = dto.getNuevoEstado();

            validarTransicion(estadoActual, estadoNuevo); // Usamos un método auxiliar para la lógica de transición

            // Aplicamos reglas específicas para ciertas transiciones
            switch (estadoNuevo) {
                case EN_PROGRESO:
                    // RN-03: No se puede iniciar una etapa sin actividades
                    if (etapaRepository.contarActividadesPorEtapa(idEtapa) == 0) {
                        throw new ReglasNegocioException("Error RN-03: No se puede iniciar la etapa sin actividades registradas");
                    }
                    // RA-07: Registramos la fecha de inicio real automáticamente
                    if (etapa.getFechaInicioReal() == null) {
                        etapa.setFechaInicioReal(java.time.LocalDate.now());
                    }
                    break;

                case COMPLETADA:
                    // RN-05: El avance debe ser del 100% para completar la etapa
                    if (etapa.getPorcentajeAvance() < 100) {
                        throw new ReglasNegocioException("Error RN-05: No se puede completar la etapa con un avance menor al 100%");
                    }
                    // RV-05: No debe haber actividades pendientes
                    long pendientes = etapaRepository.contarActividadesPendientes(idEtapa);
                    if (pendientes > 0) {
                        throw new ReglasNegocioException("Error RV-05: Existen " + pendientes + " actividades pendientes. Deben cerrarse primero");
                    }
                    // RA-02: Registramos la fecha de fin real
                    etapa.setFechaFinReal(java.time.LocalDate.now());
                    break;
            }

            // Aplicamos el nuevo estado a la etapa
            etapa.setEstado(estadoNuevo);
        }

        Etapa etapaGuardada = etapaRepository.save(etapa);

        // Obtenemos el presupuesto para construir la respuesta completa
        Presupuesto presupuesto = presupuestoRepository.findByEtapaIdEtapa(idEtapa).orElse(null);

        return etapaMapper.toResponse(etapaGuardada, presupuesto);
    }

    @Override
    @Transactional // RNF-01: Operación crítica que debe ser atómica
    public void reordenarEtapa(Long idEtapa, Integer nuevoOrden) {
        Etapa etapaMover = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        Long idProyecto = etapaMover.getProyecto().getIdProyecto();
        Integer ordenActual = etapaMover.getNumeroOrden();

        if (ordenActual.equals(nuevoOrden)) return; // No hay nada que hacer

        // Obtenemos todas las etapas del proyecto
        List<Etapa> etapas = etapaRepository.findByProyectoIdProyectoOrderByNumeroOrdenAsc(idProyecto);

        // Reordenamos la lista en memoria
        etapas.removeIf(e -> e.getIdEtapa().equals(idEtapa));
        int indiceDestino = Math.max(0, Math.min(nuevoOrden - 1, etapas.size()));
        etapas.add(indiceDestino, etapaMover);

        // Estrategia de reordenamiento seguro para evitar conflictos de constraint
        // 1. Asignamos números de orden temporales muy altos
        int temporalBase = 1_000_000;
        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setNumeroOrden(temporalBase + i);
        }
        etapaRepository.saveAllAndFlush(etapas);

        // 2. Asignamos los números de orden finales y correctos
        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setNumeroOrden(i + 1);
        }
        etapaRepository.saveAll(etapas);
    }

    /**
     * Método auxiliar para centralizar la lógica de la máquina de estados (RN-04)
     */
    private void validarTransicion(EstadoEtapa actual, EstadoEtapa nuevo) {
        // Una etapa en estado final no puede cambiar
        if (actual == EstadoEtapa.COMPLETADA || actual == EstadoEtapa.CANCELADA) {
            throw new ReglasNegocioException("Error RN-04: La etapa está en un estado final (" + actual + ") y no puede modificarse");
        }

        boolean transicionValida = false;
        switch (actual) {
            case PLANIFICADA:
                transicionValida = (nuevo == EstadoEtapa.EN_PROGRESO || nuevo == EstadoEtapa.CANCELADA);
                break;
            case EN_PROGRESO:
                transicionValida = (nuevo == EstadoEtapa.EN_PAUSA || nuevo == EstadoEtapa.COMPLETADA || nuevo == EstadoEtapa.CANCELADA);
                break;
            case EN_PAUSA:
                transicionValida = (nuevo == EstadoEtapa.EN_PROGRESO);
                break;
        }

        if (!transicionValida) {
            throw new ReglasNegocioException("Error RN-04: Transición de estado no permitida de " + actual + " a " + nuevo);
        }
    }

    @Override
    @Transactional
    public void eliminarEtapa(Long idEtapa) {
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        // RV-04 (Parte A): No se puede eliminar si tiene actividades
        if (actividadRepository.existsByEtapaIdEtapa(idEtapa)) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque tiene actividades asociadas");
        }

        // RV-04 (Parte B): No se puede eliminar si ya tiene gastos registrados
        if (presupuestoRepository.existsByEtapaIdEtapaAndMontoGastadoGreaterThan(idEtapa, BigDecimal.ZERO)) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque tiene gastos ejecutados");
        }

        Long idProyecto = etapa.getProyecto().getIdProyecto();

        // La configuración de CascadeType.ALL se encargará de eliminar el presupuesto asociado
        etapaRepository.delete(etapa);

        // RA-09: Recalculamos el estado del proyecto, ya que la eliminación de una etapa afecta el promedio
        proyectoService.recalcularEstadoProyecto(idProyecto);
    }
}
