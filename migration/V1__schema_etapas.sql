-- 1. TABLA: PROYECTO
CREATE TABLE proyecto (
                          id_proyecto BIGINT AUTO_INCREMENT PRIMARY KEY,
                          id_cliente BIGINT NOT NULL,
                          nombre VARCHAR(255) NOT NULL,
                          descripcion TEXT,
                          metodologia VARCHAR(50),
                          tipo VARCHAR(50),
                          prioridad VARCHAR(20),
                          fecha_inicio DATE NOT NULL,
                          fecha_fin_estimada DATE,
                          fecha_fin_real DATE,
                          presupuesto_total_objetivo DECIMAL(19, 2),
                          estado VARCHAR(50) NOT NULL,

    -- Validación de fechas
                          CONSTRAINT chk_proyecto_fechas CHECK (fecha_fin_estimada >= fecha_inicio)
) ENGINE=InnoDB;

-- 2. TABLA: ETAPA
CREATE TABLE etapa (
                       id_etapa BIGINT AUTO_INCREMENT PRIMARY KEY,
                       id_proyecto BIGINT NOT NULL,
                       nombre VARCHAR(150) NOT NULL,
                       descripcion TEXT,
                       numero_orden INT NOT NULL,
                       fecha_inicio_plan DATE NOT NULL,
                       fecha_fin_plan DATE NOT NULL,
                       fecha_inicio_real DATE,
                       fecha_fin_real DATE,
                       porcentaje_avance INT DEFAULT 0,
                       estado VARCHAR(50) DEFAULT 'PLANIFICADA',

                       CONSTRAINT fk_etapa_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE CASCADE,

    -- RN-02: Secuencialidad única por proyecto
                       CONSTRAINT uq_etapa_orden_proyecto UNIQUE (id_proyecto, numero_orden),

    -- RV-02: Coherencia de fechas planificadas
                       CONSTRAINT chk_etapa_fechas_plan CHECK (fecha_fin_plan >= fecha_inicio_plan),

    -- RV-02: Coherencia de fechas reales
                       CONSTRAINT chk_etapa_fechas_real CHECK (fecha_fin_real IS NULL OR fecha_fin_real >= fecha_inicio_real),

    -- RV-03: Avance porcentual válido
                       CONSTRAINT chk_etapa_avance CHECK (porcentaje_avance BETWEEN 0 AND 100),

    -- RN-04: Estados permitidos
                       CONSTRAINT chk_etapa_estado CHECK (estado IN ('PLANIFICADA', 'EN_PROGRESO', 'EN_PAUSA', 'COMPLETADA', 'CANCELADA', 'ATRASADA'))
) ENGINE=InnoDB;

-- 3. TABLA: PRESUPUESTO
CREATE TABLE presupuesto (
                             id_presupuesto BIGINT AUTO_INCREMENT PRIMARY KEY,
                             id_etapa BIGINT NOT NULL,
                             monto_aprobado DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
                             monto_gastado DECIMAL(19, 2) DEFAULT 0.00,
                             fecha_aprobacion DATE,
                             estado VARCHAR(50),
                             moneda VARCHAR(3) DEFAULT 'MXN',

                             CONSTRAINT fk_presupuesto_etapa FOREIGN KEY (id_etapa) REFERENCES etapa(id_etapa) ON DELETE CASCADE,

    -- RN-07: Valores monetarios no negativos
                             CONSTRAINT chk_presupuesto_gastado_positivo CHECK (monto_gastado >= 0),
                             CONSTRAINT chk_presupuesto_aprobado_positivo CHECK (monto_aprobado >= 0)
) ENGINE=InnoDB;

-- 4. TABLA: ACTIVIDAD
CREATE TABLE actividad (
                           id_actividad BIGINT AUTO_INCREMENT PRIMARY KEY,
                           id_etapa BIGINT NOT NULL,
                           id_requisito BIGINT, -- Referencia lógica (sin FK física por ahora)
                           nombre VARCHAR(200) NOT NULL,
                           tipo VARCHAR(50),
                           fecha_inicio_prog DATE,
                           fecha_fin_prog DATE,
                           fecha_inicio_real DATE,
                           fecha_fin_real DATE,
                           porcentaje_avance INT DEFAULT 0,
                           estado VARCHAR(50) DEFAULT 'PENDIENTE',

                           CONSTRAINT fk_actividad_etapa FOREIGN KEY (id_etapa) REFERENCES etapa(id_etapa) ON DELETE CASCADE,

    -- RV-03: Avance válido
                           CONSTRAINT chk_actividad_avance CHECK (porcentaje_avance BETWEEN 0 AND 100),

    -- Coherencia de fechas programadas
                           CONSTRAINT chk_actividad_fechas_prog CHECK (fecha_fin_prog IS NULL OR fecha_fin_prog >= fecha_inicio_prog)
) ENGINE=InnoDB;

CREATE INDEX idx_etapa_proyecto ON etapa(id_proyecto);
CREATE INDEX idx_presupuesto_etapa ON presupuesto(id_etapa);
CREATE INDEX idx_actividad_etapa ON actividad(id_etapa);