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
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtapaServiceImplent implements EtapaService {

    private final EtapaRepository etapaRepository;
    private final ProyectoRepository proyectoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ActividadRepository actividadRepository;
    private final ProyectoService proyectoService;
    private final EtapaMapper etapaMapper;

    @Override
    @Transactional
    public EtapaResponseDTO crearEtapa(Long idProyecto, EtapaRequestDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe"));

        if (dto.getFechaFinPlan().isBefore(dto.getFechaInicioPlan())) {
            throw new ReglasNegocioException("Error de Fechas: La fecha de fin no puede ser anterior a la de inicio");
        }

        String estadoProyecto = proyecto.getEstado().toUpperCase();
        if ("CANCELADO".equals(estadoProyecto) || "CERRADO".equals(estadoProyecto)) {
            throw new ReglasNegocioException("No se pueden agregar etapas a un proyecto en estado " + estadoProyecto);
        }

        boolean existeOrden = etapaRepository.existsByProyectoIdProyectoAndNumeroOrden(idProyecto, dto.getNumeroOrden());
        if (existeOrden) {
            etapaRepository.desplazarOrdenes(idProyecto, dto.getNumeroOrden());
        }

        Etapa nuevaEtapa = etapaMapper.toEntity(dto, proyecto);
        Etapa etapaGuardada = etapaRepository.save(nuevaEtapa);

        BigDecimal montoInicial = dto.getPresupuestoInicial() != null ? dto.getPresupuestoInicial() : BigDecimal.ZERO;

        if (montoInicial.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal techoProyecto = proyecto.getPresupuestoTotalObjetivo();
            BigDecimal sumaActual = presupuestoRepository.sumarPresupuestosPorProyecto(idProyecto);
            BigDecimal nuevoTotal = sumaActual.add(montoInicial);

            if (nuevoTotal.compareTo(techoProyecto) > 0) {
                throw new ReglasNegocioException(
                        String.format("Error RN-09: El presupuesto excede el límite del proyecto. Límite: %s, Asignado: %s, Intento: %s",
                                techoProyecto, sumaActual, montoInicial));
            }
        }

        Presupuesto presupuestoInicial = Presupuesto.builder()
                .etapa(etapaGuardada)
                .montoAprobado(montoInicial)
                .montoGastado(BigDecimal.ZERO)
                .fechaAprobacion(java.time.LocalDate.now())
                .estado("ACTIVO")
                .moneda("MXN")
                .build();
        presupuestoRepository.save(presupuestoInicial);

        proyectoService.recalcularEstadoProyecto(idProyecto);

        return etapaMapper.toResponse(etapaGuardada, presupuestoInicial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaResponseDTO> listarEtapasPorProyecto(Long idProyecto) {
        if (!proyectoRepository.existsById(idProyecto)) {
            throw new ResourceNotFoundException("El proyecto con ID " + idProyecto + " no existe");
        }
        List<Etapa> etapas = etapaRepository.findByProyectoIdProyectoOrderByNumeroOrdenAsc(idProyecto);
        return etapas.stream()
                .map(etapaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EtapaResponseDTO obtenerEtapaPorId(Long idEtapa) {
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada con ID: " + idEtapa));
        return etapaMapper.toResponse(etapa);
    }

    @Override
    @Transactional
    public EtapaResponseDTO actualizarEtapa(Long idEtapa, EtapaActualizarDTO dto) {
        Etapa etapa = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        if (dto.getNombre() != null) etapa.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) etapa.setDescripcion(dto.getDescripcion());

        if (dto.getNuevoEstado() != null && !dto.getNuevoEstado().equals(etapa.getEstado())) {
            EstadoEtapa estadoActual = etapa.getEstado();
            EstadoEtapa estadoNuevo = dto.getNuevoEstado();

            validarTransicion(estadoActual, estadoNuevo);

            // Validaciones especificas para cuando se intenta iniciar una etapa
            if (estadoNuevo == EstadoEtapa.EN_PROGRESO) {
                // Validacion para RN6
                String estadoProyecto = etapa.getProyecto().getEstado().toUpperCase();
                if (!"EN_PROGRESO".equals(estadoProyecto)) {
                    throw new ReglasNegocioException(
                        "Error RN-06: No se puede iniciar la etapa porque el proyecto esta en estado '" + estadoProyecto + "'."
                    );
                }
                // Validacion para RN8
                LocalDate limiteInferior = etapa.getFechaInicioPlan().minusDays(7);
                if (LocalDate.now().isBefore(limiteInferior)) {
                    throw new ReglasNegocioException(
                        "Error RN-08: No se puede iniciar la etapa. La fecha actual es mas de 7 dias anterior a la fecha planificada (" + etapa.getFechaInicioPlan() + ")"
                    );
                }
            }

            switch (estadoNuevo) {
                case EN_PROGRESO:
                    // Validacion para RN3
                    if (etapaRepository.contarActividadesPorEtapa(idEtapa) == 0) {
                        throw new ReglasNegocioException("Error RN-03: No se puede iniciar la etapa sin actividades registradas");
                    }
                    // Regla Automatica RA7
                    if (etapa.getFechaInicioReal() == null) {
                        etapa.setFechaInicioReal(java.time.LocalDate.now());
                    }
                    break;
                case COMPLETADA:
                    // Validacion para RN5
                    if (etapa.getPorcentajeAvance() < 100) {
                        throw new ReglasNegocioException("Error RN-05: No se puede completar la etapa con un avance menor al 100%");
                    }
                    // Validacion para RV5
                    long pendientes = etapaRepository.contarActividadesPendientes(idEtapa);
                    if (pendientes > 0) {
                        throw new ReglasNegocioException("Error RV-05: Existen " + pendientes + " actividades pendientes. Deben cerrarse primero");
                    }
                    // Regla Automatica RA2 (parcial)
                    etapa.setFechaFinReal(java.time.LocalDate.now());
                    break;
            }
            etapa.setEstado(estadoNuevo);
        }

        Etapa etapaGuardada = etapaRepository.save(etapa);
        Presupuesto presupuesto = presupuestoRepository.findByEtapaIdEtapa(idEtapa).orElse(null);
        return etapaMapper.toResponse(etapaGuardada, presupuesto);
    }

    @Override
    @Transactional
    public void reordenarEtapa(Long idEtapa, Integer nuevoOrden) {
        Etapa etapaMover = etapaRepository.findById(idEtapa)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa no encontrada"));

        Long idProyecto = etapaMover.getProyecto().getIdProyecto();
        Integer ordenActual = etapaMover.getNumeroOrden();

        if (ordenActual.equals(nuevoOrden)) return;

        List<Etapa> etapas = etapaRepository.findByProyectoIdProyectoOrderByNumeroOrdenAsc(idProyecto);

        etapas.removeIf(e -> e.getIdEtapa().equals(idEtapa));
        int indiceDestino = Math.max(0, Math.min(nuevoOrden - 1, etapas.size()));
        etapas.add(indiceDestino, etapaMover);

        int temporalBase = 1_000_000;
        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setNumeroOrden(temporalBase + i);
        }
        etapaRepository.saveAllAndFlush(etapas);

        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setNumeroOrden(i + 1);
        }
        etapaRepository.saveAll(etapas);
    }

    private void validarTransicion(EstadoEtapa actual, EstadoEtapa nuevo) {
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

        if (actividadRepository.existsByEtapaIdEtapa(idEtapa)) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque tiene actividades asociadas");
        }

        if (presupuestoRepository.existsByEtapaIdEtapaAndMontoGastadoGreaterThan(idEtapa, BigDecimal.ZERO)) {
            throw new ReglasNegocioException("Error RV-04: No se puede eliminar la etapa porque tiene gastos ejecutados");
        }

        Long idProyecto = etapa.getProyecto().getIdProyecto();
        etapaRepository.delete(etapa);
        proyectoService.recalcularEstadoProyecto(idProyecto);
    }
}
