/* Scripts para la definicion de las FKs del modelo relacional y restricciones adicionales en Oracle */

ALTER SESSION SET CONTAINER = FREEPDB1;
-- Set the current schema to the hospital schema
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- Foreign key and additional constraints
ALTER TABLE PACIENTE ADD (
    CONSTRAINT fk_paciente_persona
        FOREIGN KEY (tipo_documento, nro_documento)
        REFERENCES PERSONA (tipo_documento, nro_documento)
        ON DELETE CASCADE,
    CONSTRAINT chk_paciente_sexo
        CHECK (sexo IN ('M', 'F', 'X'))
);

ALTER TABLE MEDICO ADD (
    CONSTRAINT fk_medico_persona
        FOREIGN KEY (tipo_documento, nro_documento)
        REFERENCES PERSONA (tipo_documento, nro_documento)
        ON DELETE CASCADE,
    CONSTRAINT chk_medico_max_guardia
        CHECK (max_cant_guardia >= 0)
);
-- Restriccion para periodo_vacaciones: debe ser NULL (por si es nuevo) o uno de los meses del año
ALTER TABLE MEDICO ADD (
    CONSTRAINT chk_medico_periodo_vacaciones
        CHECK (
            periodo_vacaciones IS NULL
            OR UPPER(periodo_vacaciones) IN (
                'ENERO', 'FEBRERO', 'MARZO', 'ABRIL',
                'MAYO', 'JUNIO', 'JULIO', 'AGOSTO',
                'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'
            )
        )
);


ALTER TABLE HABITACION ADD (
    CONSTRAINT fk_habitacion_sector
        FOREIGN KEY (id_sector)
        REFERENCES SECTOR (id_sector)
        ON DELETE CASCADE,
    CONSTRAINT chk_habitacion_piso
        CHECK (piso >= 0),
    CONSTRAINT chk_habitacion_orientacion
        CHECK (orientacion IN ('NORTE', 'SUR', 'ESTE', 'OESTE'))
);

ALTER TABLE CAMA ADD (
    CONSTRAINT fk_cama_habitacion
        FOREIGN KEY (nro_habitacion)
        REFERENCES HABITACION (nro_habitacion)
        ON DELETE CASCADE,
    CONSTRAINT chk_cama_estado
        CHECK (estado IN ('LIBRE', 'OCUPADA')),
    CONSTRAINT chk_cama_nro_cama
        CHECK (nro_cama > 0),
    CONSTRAINT chk_cama_nro_habitacion
        CHECK (nro_habitacion > 0)
);

ALTER TABLE ESPECIALIDAD
    ADD CONSTRAINT fk_especialidad_sector
        FOREIGN KEY (id_sector)
        REFERENCES SECTOR (id_sector)
        ON DELETE CASCADE;

ALTER TABLE SE_ESPECIALIZA_EN ADD (
    CONSTRAINT fk_se_especializa_en_medico
        FOREIGN KEY (matricula)
        REFERENCES MEDICO (matricula)
        ON DELETE CASCADE,
    CONSTRAINT fk_se_especializa_en_especialidad
        FOREIGN KEY (cod_especialidad)
        REFERENCES ESPECIALIDAD (cod_especialidad)
        ON DELETE CASCADE,
    CONSTRAINT chk_se_especializa_en_hace_guardia
        CHECK (hace_guardia IN (0, 1))
);

ALTER TABLE VISITA ADD (
    CONSTRAINT fk_visita_ronda
        FOREIGN KEY (id_ronda)
        REFERENCES RONDA (id_ronda)
        ON DELETE CASCADE,
    CONSTRAINT fk_visita_habitacion
        FOREIGN KEY (nro_habitacion)
        REFERENCES HABITACION (nro_habitacion)
        ON DELETE CASCADE
);

ALTER TABLE INTERNACION ADD (
    CONSTRAINT fk_internacion_paciente
        FOREIGN KEY (tipo_documento, nro_documento)
        REFERENCES PACIENTE (tipo_documento, nro_documento)
        ON DELETE CASCADE,
    CONSTRAINT fk_internacion_medico
        FOREIGN KEY (matricula)
        REFERENCES MEDICO (matricula)
        ON DELETE CASCADE,
    CONSTRAINT chk_internacion_fecha_inicio
        CHECK (fecha_fin IS NULL OR fecha_inicio <= fecha_fin)
);

ALTER TABLE SE_UBICA ADD (
    CONSTRAINT fk_se_ubica_internacion
        FOREIGN KEY (nro_internacion)
        REFERENCES INTERNACION (nro_internacion)
        ON DELETE CASCADE,
    CONSTRAINT fk_se_ubica_cama
        FOREIGN KEY (nro_cama, nro_habitacion)
        REFERENCES CAMA (nro_cama, nro_habitacion)
        ON DELETE CASCADE
);

ALTER TABLE RECORRIDO ADD (
    CONSTRAINT fk_recorrido_ronda
        FOREIGN KEY (id_ronda)
        REFERENCES RONDA (id_ronda)
        ON DELETE CASCADE,
    CONSTRAINT fk_recorrido_medico
        FOREIGN KEY (matricula)
        REFERENCES MEDICO (matricula)
        ON DELETE CASCADE,
    CONSTRAINT chk_recorrido_horas
        CHECK (hora_fin > hora_inicio)
);

ALTER TABLE COMENTA_SOBRE ADD (
    CONSTRAINT fk_comenta_sobre_recorrido
        FOREIGN KEY (id_recorrido)
        REFERENCES RECORRIDO (id_recorrido)
        ON DELETE CASCADE,
    CONSTRAINT fk_comenta_sobre_internacion
        FOREIGN KEY (nro_internacion)
        REFERENCES INTERNACION (nro_internacion)
        ON DELETE CASCADE
);

ALTER TABLE GUARDIA ADD (
    CONSTRAINT fk_guardia_se_especializa_en
        FOREIGN KEY (matricula, cod_especialidad)
        REFERENCES SE_ESPECIALIZA_EN (matricula, cod_especialidad)
        ON DELETE CASCADE,
    CONSTRAINT fk_guardia_turno
        FOREIGN KEY (id_turno)
        REFERENCES TURNO (id_turno)
        ON DELETE CASCADE
);

ALTER TABLE ATIENDE ADD (
    CONSTRAINT fk_atiende_especialidad
        FOREIGN KEY (cod_especialidad)
        REFERENCES ESPECIALIDAD (cod_especialidad)
        ON DELETE CASCADE,
    CONSTRAINT fk_atiende_turno
        FOREIGN KEY (id_turno)
        REFERENCES TURNO (id_turno)
        ON DELETE CASCADE
);
