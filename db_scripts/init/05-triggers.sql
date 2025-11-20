
-- =========================================================
-- 1. Auditoria: registro de cambios en la tabla GUARDIA
-- =========================================================

CREATE OR REPLACE TRIGGER tr_aud_guardia
AFTER INSERT OR UPDATE OR DELETE ON GUARDIA
FOR EACH ROW
DECLARE
    v_operacion   VARCHAR2(10);
    v_detalle_old VARCHAR2(4000);
    v_detalle_new VARCHAR2(4000);
BEGIN
    IF INSERTING THEN
        v_operacion   := 'INSERT';
        v_detalle_old := NULL;
        v_detalle_new :=
                'nro_guardia='        || :NEW.nro_guardia
            || ', fecha_hora='       || TO_CHAR(:NEW.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
            || ', matricula='        || :NEW.matricula
            || ', cod_especialidad=' || :NEW.cod_especialidad
            || ', id_turno='         || :NEW.id_turno;

    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_detalle_old :=
                'nro_guardia='        || :OLD.nro_guardia
            || ', fecha_hora='       || TO_CHAR(:OLD.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
            || ', matricula='        || :OLD.matricula
            || ', cod_especialidad=' || :OLD.cod_especialidad
            || ', id_turno='         || :OLD.id_turno;

        v_detalle_new :=
                'nro_guardia='        || :NEW.nro_guardia
            || ', fecha_hora='       || TO_CHAR(:NEW.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
            || ', matricula='        || :NEW.matricula
            || ', cod_especialidad=' || :NEW.cod_especialidad
            || ', id_turno='         || :NEW.id_turno;

    ELSIF DELETING THEN
        v_operacion   := 'DELETE';
        v_detalle_old :=
                'nro_guardia='        || :OLD.nro_guardia
            || ', fecha_hora='       || TO_CHAR(:OLD.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
            || ', matricula='        || :OLD.matricula
            || ', cod_especialidad=' || :OLD.cod_especialidad
            || ', id_turno='         || :OLD.id_turno;
        v_detalle_new := NULL;
    END IF;

    INSERT INTO AUDITORIA_GUARDIA (
        fecha_hora_reg,
        usuario_bd,
        operacion,
        nro_guardia,
        fecha_hora_guard,
        matricula,
        cod_especialidad,
        id_turno,
        detalle_old,
        detalle_new
    ) VALUES (
        SYSTIMESTAMP,
        USER,
        v_operacion,
        NVL(:NEW.nro_guardia,      :OLD.nro_guardia),
        NVL(:NEW.fecha_hora,       :OLD.fecha_hora),
        NVL(:NEW.matricula,        :OLD.matricula),
        NVL(:NEW.cod_especialidad, :OLD.cod_especialidad),
        NVL(:NEW.id_turno,         :OLD.id_turno),
        v_detalle_old,
        v_detalle_new
    );
END;
/

-- =============================================================
-- 2. Chequeo: estado de cama al ubicar paciente en internación
-- =============================================================

CREATE OR REPLACE TRIGGER tr_se_ubica_cama_estado
BEFORE INSERT ON SE_UBICA
FOR EACH ROW
DECLARE
    v_estado_cama   CAMA.estado%TYPE;
    v_fecha_inicio  INTERNACION.fecha_inicio%TYPE;
    v_fecha_fin     INTERNACION.fecha_fin%TYPE;
BEGIN
    -- 1) Verificar que la cama exista y esté LIBRE
    SELECT estado
    INTO v_estado_cama
    FROM CAMA
    WHERE nro_cama       = :NEW.nro_cama   
        AND nro_habitacion = :NEW.nro_habitacion;

    IF v_estado_cama <> 'LIBRE' THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'La cama ' || :NEW.nro_cama || ' de la habitacion ' || :NEW.nro_habitacion ||
            ' no esta LIBRE.'
        );
    END IF;

    -- 2) Verificar que la fecha de ingreso este dentro del periodo de la internacion
    SELECT fecha_inicio, fecha_fin
    INTO v_fecha_inicio, v_fecha_fin
    FROM INTERNACION
    WHERE nro_internacion = :NEW.nro_internacion;

    IF :NEW.fecha_hora_ingreso < v_fecha_inicio OR (v_fecha_fin IS NOT NULL AND :NEW.fecha_hora_ingreso > v_fecha_fin) THEN RAISE_APPLICATION_ERROR(
            -20002,
            'La fecha/hora de ingreso no esta dentro del periodo de la internacion.'
        );
    END IF;

    -- 3) Marcar la cama como OCUPADA
    UPDATE CAMA
        SET estado = 'OCUPADA'
    WHERE nro_cama       = :NEW.nro_cama    
        AND nro_habitacion = :NEW.nro_habitacion;
END;
/

-- ==================================================================
-- 3. Chequeo: liberación automática de cama al finalizar internación
-- ==================================================================

CREATE OR REPLACE TRIGGER tr_internacion_libera_cama
AFTER UPDATE OF fecha_fin ON INTERNACION
FOR EACH ROW
WHEN (OLD.fecha_fin IS NULL AND NEW.fecha_fin IS NOT NULL)
DECLARE
    v_nro_cama       CAMA.nro_cama%TYPE;
    v_nro_habitacion CAMA.nro_habitacion%TYPE;
BEGIN
    -- Buscar la última cama donde estuvo esta internación
    SELECT su.nro_cama, su.nro_habitacion
    INTO v_nro_cama, v_nro_habitacion
    FROM SE_UBICA su
    WHERE su.nro_internacion = :NEW.nro_internacion
    ORDER BY su.fecha_hora_ingreso DESC
    FETCH FIRST 1 ROW ONLY;

    -- Liberar esa cama
    UPDATE CAMA
        SET estado = 'LIBRE'
    WHERE nro_cama       = v_nro_cama
        AND nro_habitacion = v_nro_habitacion;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL; -- Por si hubiera alguna internación sin SE_UBICA (raro, pero mejor no romper)
END;
/
