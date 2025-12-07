CREATE TABLE actividad
(
    id_actividad      BIGINT AUTO_INCREMENT NOT NULL,
    id_etapa          BIGINT       NOT NULL,
    id_requisito      BIGINT NULL,
    nombre            VARCHAR(200) NOT NULL,
    tipo              VARCHAR(255) NULL,
    fecha_inicio_prog date NULL,
    fecha_fin_prog    date NULL,
    fecha_inicio_real date NULL,
    fecha_fin_real    date NULL,
    porcentaje_avance INT NULL,
    estado            VARCHAR(255) NOT NULL,
    CONSTRAINT pk_actividad PRIMARY KEY (id_actividad)
);

CREATE TABLE etapa
(
    id_etapa          BIGINT AUTO_INCREMENT NOT NULL,
    id_proyecto       BIGINT       NOT NULL,
    nombre            VARCHAR(150) NOT NULL,
    descripcion       VARCHAR(255) NULL,
    numero_orden      INT          NOT NULL,
    fecha_inicio_plan date         NOT NULL,
    fecha_fin_plan    date         NOT NULL,
    fecha_inicio_real date NULL,
    fecha_fin_real    date NULL,
    porcentaje_avance INT NULL,
    estado            VARCHAR(255) NOT NULL,
    CONSTRAINT pk_etapa PRIMARY KEY (id_etapa)
);

CREATE TABLE presupuesto
(
    id_presupuesto   BIGINT AUTO_INCREMENT NOT NULL,
    id_etapa         BIGINT         NOT NULL,
    monto_aprobado   DECIMAL(19, 2) NOT NULL,
    monto_gastado    DECIMAL(19, 2) NULL,
    fecha_aprobacion date NULL,
    estado           VARCHAR(255) NULL,
    moneda           VARCHAR(255) NULL,
    CONSTRAINT pk_presupuesto PRIMARY KEY (id_presupuesto)
);

CREATE TABLE proyecto
(
    id_proyecto                BIGINT AUTO_INCREMENT NOT NULL,
    id_cliente                 BIGINT       NOT NULL,
    nombre                     VARCHAR(255) NOT NULL,
    descripcion                VARCHAR(255) NULL,
    metodologia                VARCHAR(255) NULL,
    tipo                       VARCHAR(255) NULL,
    prioridad                  VARCHAR(255) NULL,
    fecha_inicio               date         NOT NULL,
    fecha_fin_estimada         date NULL,
    fecha_fin_real             date NULL,
    presupuesto_total_objetivo DECIMAL(19, 2) NULL,
    estado                     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_proyecto PRIMARY KEY (id_proyecto)
);

ALTER TABLE etapa
    ADD CONSTRAINT uc_eca6d4cd28cdd3ad7645ce1f7 UNIQUE (id_proyecto, numero_orden);

ALTER TABLE actividad
    ADD CONSTRAINT FK_ACTIVIDAD_ON_ID_ETAPA FOREIGN KEY (id_etapa) REFERENCES etapa (id_etapa);

ALTER TABLE etapa
    ADD CONSTRAINT FK_ETAPA_ON_ID_PROYECTO FOREIGN KEY (id_proyecto) REFERENCES proyecto (id_proyecto);

ALTER TABLE presupuesto
    ADD CONSTRAINT FK_PRESUPUESTO_ON_ID_ETAPA FOREIGN KEY (id_etapa) REFERENCES etapa (id_etapa);