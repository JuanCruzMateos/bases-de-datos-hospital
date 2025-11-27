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
            'El medico principal no puede ser la misma persona que el paciente.'
        );
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Por coherencia con la FK, esto no debería ocurrir; si pasa, lo tratamos como error.
        RAISE_APPLICATION_ERROR(
            -20002,
            'No se encontro el medico con la matricula ' || :NEW.matricula ||
            ' al validar la internación.'
        );
END;
/


-- ========================================================================
-- 8. Chequeo: si el tipo_doc es DNI, el varchar2 del cuil contiene el numero del dni
-- ========================================================================

CREATE OR REPLACE TRIGGER tr_medico_cuil_dni
BEFORE INSERT OR UPDATE OF tipo_documento, cuil_cuit ON MEDICO
FOR EACH ROW
DECLARE
BEGIN
    IF :NEW.tipo_documento = 'DNI' AND :NEW.cuil_cuit NOT LIKE '%' || :NEW.nro_documento || '%' THEN
        RAISE_APPLICATION_ERROR(-20001, 'El cuil/cuit debe contener el numero de dni.');  -- 20001 es el codigo de error por defecto de Oracle.
    END IF;
END;
/

-- ========================================================================
-- 9. Chequeo: un paciente no puede tener dos internaciones activas simultáneamente
-- ========================================================================

CREATE OR REPLACE TRIGGER tr_internacion_unica_activa
BEFORE INSERT OR UPDATE OF fecha_fin ON INTERNACION
FOR EACH ROW
DECLARE
    v_internaciones_activas NUMBER;
BEGIN
    -- Solo verificar si la internación nueva/actualizada está activa (fecha_fin IS NULL)
    IF :NEW.fecha_fin IS NULL THEN
        -- Contar internaciones activas del mismo paciente (excluyendo la actual)
        SELECT COUNT(*)
        INTO v_internaciones_activas
        FROM INTERNACION
        WHERE tipo_documento = :NEW.tipo_documento
            AND nro_documento = :NEW.nro_documento
            AND fecha_fin IS NULL
            AND nro_internacion != :NEW.nro_internacion; -- Excluir la internación actual en caso de UPDATE
        
        IF v_internaciones_activas > 0 THEN
            RAISE_APPLICATION_ERROR(
                -20050,
                'El paciente con ' || :NEW.tipo_documento || ' ' || :NEW.nro_documento ||
                ' ya tiene una internacion activa. No puede tener dos internaciones simultaneas.'
            );
        END IF;
    END IF;
END;
/

-- ========================================================================
-- 10. Chequeo: el médico principal debe tener al menos una especialidad 
--     asociada con el sector de la habitación
-- ========================================================================

CREATE OR REPLACE TRIGGER tr_medico_especialidad_sector_ubica
BEFORE INSERT ON SE_UBICA
FOR EACH ROW
DECLARE
    v_matricula_medico  INTERNACION.matricula%TYPE;
    v_id_sector         HABITACION.id_sector%TYPE;
    v_especialidades    NUMBER;
BEGIN
    -- 1) Obtener el médico principal de la internación
    SELECT matricula
    INTO v_matricula_medico
    FROM INTERNACION
    WHERE nro_internacion = :NEW.nro_internacion;
    
    -- 2) Obtener el sector de la habitación donde se ubica
    SELECT id_sector
    INTO v_id_sector
    FROM HABITACION
    WHERE nro_habitacion = :NEW.nro_habitacion;
    
    -- 3) Verificar que el médico tenga al menos una especialidad en ese sector
    SELECT COUNT(*)
    INTO v_especialidades
    FROM SE_ESPECIALIZA_EN see
    JOIN ESPECIALIDAD e ON see.cod_especialidad = e.cod_especialidad
    WHERE see.matricula = v_matricula_medico
        AND e.id_sector = v_id_sector;
    
    IF v_especialidades = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20051,
            'El medico principal (matricula ' || v_matricula_medico ||
            ') no tiene ninguna especialidad asociada al sector ' || v_id_sector ||
            ' de la habitacion ' || :NEW.nro_habitacion || '.'
        );
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20052,
            'Error al validar medico/sector: datos no encontrados.'
        );
END;
/

-- Trigger complementario
-- considera que primero se crea la internacion y luego se asigna el médico
CREATE OR REPLACE TRIGGER tr_medico_especialidad_sector_int
BEFORE INSERT OR UPDATE OF matricula ON INTERNACION
FOR EACH ROW
DECLARE
    v_id_sector         HABITACION.id_sector%TYPE;
    v_especialidades    NUMBER;
    v_tiene_ubicacion   NUMBER;
BEGIN
    -- Verificar si la internación ya tiene una ubicación asignada (SE_UBICA)
    SELECT COUNT(*)
    INTO v_tiene_ubicacion
    FROM SE_UBICA
    WHERE nro_internacion = :NEW.nro_internacion;
    
    -- Solo validar si ya existe una ubicación
    IF v_tiene_ubicacion > 0 THEN
        -- Obtener el sector de la habitación actual (la más reciente)
        SELECT h.id_sector
        INTO v_id_sector
        FROM SE_UBICA su
        JOIN HABITACION h ON su.nro_habitacion = h.nro_habitacion
        WHERE su.nro_internacion = :NEW.nro_internacion
        ORDER BY su.fecha_hora_ingreso DESC
        FETCH FIRST 1 ROW ONLY;
        
        -- Verificar que el médico tenga al menos una especialidad en ese sector
        SELECT COUNT(*)
        INTO v_especialidades
        FROM SE_ESPECIALIZA_EN see
        JOIN ESPECIALIDAD e ON see.cod_especialidad = e.cod_especialidad
        WHERE see.matricula = :NEW.matricula
            AND e.id_sector = v_id_sector;
        
        IF v_especialidades = 0 THEN
            RAISE_APPLICATION_ERROR(
                -20053,
                'El medico (matricula ' || :NEW.matricula ||
                ') no tiene ninguna especialidad asociada al sector ' || v_id_sector ||
                ' donde esta ubicado el paciente.'
            );
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20054,
            'Error al validar medico/sector en internacion: datos no encontrados.'
        );
END;
/

-- ========================================================================
-- 11. Chequeo: un médico no puede estar de guardia si está de vacaciones
-- ========================================================================

CREATE OR REPLACE TRIGGER tr_guardia_no_vacaciones
BEFORE INSERT OR UPDATE OF matricula, fecha_hora ON GUARDIA
FOR EACH ROW
DECLARE
    v_en_vacaciones NUMBER;
BEGIN
    -- Verificar si el médico está de vacaciones en la fecha de la guardia
    SELECT COUNT(*)
    INTO v_en_vacaciones
    FROM VACACIONES
    WHERE matricula = :NEW.matricula
        AND TRUNC(:NEW.fecha_hora) BETWEEN fecha_inicio AND fecha_fin;
    
    IF v_en_vacaciones > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20110,
            'Error: El medico con matricula ' || :NEW.matricula || 
            ' esta de vacaciones el dia ' || TO_CHAR(:NEW.fecha_hora, 'YYYY-MM-DD') ||
            '. No se puede asignar una guardia.'
        );
    END IF;
END;
/

-- -- Trigger complementario: no permitir agregar vacaciones si hay guardias
-- CREATE OR REPLACE TRIGGER tr_vacaciones_no_guardias
-- BEFORE INSERT OR UPDATE ON VACACIONES
-- FOR EACH ROW
-- DECLARE
--     v_guardias_conflicto NUMBER;
-- BEGIN
--     -- Verificar que fecha_inicio < fecha_fin
--     IF :NEW.fecha_inicio >= :NEW.fecha_fin THEN
--         RAISE_APPLICATION_ERROR(
--             -20111,
--             'Error: La fecha de inicio debe ser anterior a la fecha de fin.'
--         );
--     END IF;
    
--     -- Verificar si el médico tiene guardias durante el periodo de vacaciones
--     SELECT COUNT(*)
--     INTO v_guardias_conflicto
--     FROM GUARDIA
--     WHERE matricula = :NEW.matricula
--         AND TRUNC(fecha_hora) BETWEEN :NEW.fecha_inicio AND :NEW.fecha_fin;
    
--     IF v_guardias_conflicto > 0 THEN
--         RAISE_APPLICATION_ERROR(
--             -20112,
--             'Error: El medico tiene ' || v_guardias_conflicto || 
--             ' guardia(s) programada(s) durante el periodo de vacaciones ' ||
--             '(' || TO_CHAR(:NEW.fecha_inicio, 'YYYY-MM-DD') || ' - ' || 
--             TO_CHAR(:NEW.fecha_fin, 'YYYY-MM-DD') || '). ' ||
--             'Debe reasignar o cancelar las guardias primero.'
--         );
--     END IF;
    
--     -- Verificar que no haya solapamiento de vacaciones
--     DECLARE
--         v_vacaciones_solapadas NUMBER;
--     BEGIN
--         SELECT COUNT(*)
--         INTO v_vacaciones_solapadas
--         FROM VACACIONES
--         WHERE matricula = :NEW.matricula
--             AND fecha_inicio < :NEW.fecha_fin
--             AND fecha_fin > :NEW.fecha_inicio
--             AND NOT (fecha_inicio = :NEW.fecha_inicio AND fecha_fin = :NEW.fecha_fin);
        
--         IF v_vacaciones_solapadas > 0 THEN
--             RAISE_APPLICATION_ERROR(
--                 -20113,
--                 'Error: Ya existen vacaciones que se solapan con el periodo solicitado.'
--             );
--         END IF;
--     END;
-- END;
-- /
