ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

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

-- ========================================================================
-- 2. Chequeo: verifica el estado de cama al ubicar paciente en internación
-- ========================================================================

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
        NULL; -- Por si hay alguna internación sin SE_UBICA asociada
END;
/

-- =============================================================================
-- 4. Chequeo: no permitir borrar habitación si tiene camas ocupadas o historial
-- =============================================================================

CREATE OR REPLACE TRIGGER tr_habitacion_no_delete_if_used
BEFORE DELETE ON HABITACION
FOR EACH ROW
DECLARE
    v_internaciones_activas NUMBER;
    v_historial             NUMBER;
BEGIN
    -- 1) Si hay internaciones ACTIVAS en esa habitacion, no se puede borrar
    --    (equivalente a "tiene camas ocupadas", pero sin tocar la tabla CAMA)
    SELECT COUNT(*)
    INTO v_internaciones_activas
    FROM SE_UBICA su
    JOIN INTERNACION i
        ON i.nro_internacion = su.nro_internacion
    WHERE su.nro_habitacion = :OLD.nro_habitacion   AND i.fecha_fin IS NULL;

    IF v_internaciones_activas > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20040,
            'No se puede eliminar la habitacion ' || :OLD.nro_habitacion ||
            ' porque tiene camas OCUPADAS (internaciones activas).'
        );
    END IF;

    -- 2) Si tiene historial en SE_UBICA, tampoco se puede borrar
    SELECT COUNT(*)
    INTO v_historial
    FROM SE_UBICA su
    WHERE su.nro_habitacion = :OLD.nro_habitacion;

    IF v_historial > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20041,
            'No se puede eliminar la habitacion ' || :OLD.nro_habitacion || ' porque tiene historial de uso (SE_UBICA).'
        );
    END IF;
END;
/

-- =============================================================================
-- 5. Chequeo: médico principal distinto al paciente en internación
-- =============================================================================

CREATE OR REPLACE TRIGGER trg_int_paciente_medico_distintos
BEFORE INSERT OR UPDATE OF tipo_documento, nro_documento, matricula
ON INTERNACION
FOR EACH ROW
DECLARE
    v_tipo_doc_medico   MEDICO.tipo_documento%TYPE;
    v_nro_doc_medico    MEDICO.nro_documento%TYPE;
BEGIN
    -- Buscamos los datos de persona del médico principal
    SELECT tipo_documento, nro_documento
    INTO v_tipo_doc_medico, v_nro_doc_medico
    FROM MEDICO
    WHERE matricula = :NEW.matricula;

    -- Si el médico y el paciente tienen el mismo doc, rechazamos la operación
    IF v_tipo_doc_medico = :NEW.tipo_documento  AND v_nro_doc_medico = :NEW.nro_documento THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'El médico principal no puede ser la misma persona que el paciente.'
        );
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Por coherencia con la FK, esto no debería ocurrir; si pasa, lo tratamos como error.
        RAISE_APPLICATION_ERROR(
            -20002,
            'No se encontró el médico con la matrícula ' || :NEW.matricula ||
            ' al validar la internación.'
        );
END;
/

