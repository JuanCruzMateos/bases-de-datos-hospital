
-- =========================================================
-- Business Rules:
-- 1. A doctor cannot be on guard duty if they are on vacation that day
-- 2. Vacations should not overlap for the same doctor
-- 3. Date range must be valid (start <= end)
-- 4. All dates must be non-null
--
-- Error Codes:
-- -20098: Null dates provided
-- -20099: Invalid date range (start > end)
-- -20100: Doctor does not exist
-- -20101: Overlapping vacations detected
-- -20102: Guard duty conflicts during vacation period
--
-- Transaction Control:
-- - Uses SERIALIZABLE isolation level to prevent race conditions
-- - Uses SAVEPOINT for rollback capability
-- - Commits only on successful completion
-- - Rolls back to savepoint on any error
-- - Handles ORA-08177 serialization conflicts (retry recommended)
-- =========================================================

SET SERVEROUTPUT ON;

ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

DECLARE
    v_matricula         NUMBER := 1001;  -- Cambiar
    v_fecha_inicio      DATE := TO_DATE('2024-12-20', 'YYYY-MM-DD');
    v_fecha_fin         DATE := TO_DATE('2024-12-31', 'YYYY-MM-DD');
    
    v_guardias_conflicto NUMBER;
    v_vacaciones_solapadas NUMBER;
    v_medico_existe      NUMBER;
BEGIN
    -- Start transaction with SERIALIZABLE isolation level
    -- This prevents race conditions between validation checks and INSERT
    SAVEPOINT inicio_transaccion;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

    -- 1. Validate date range
    IF v_fecha_inicio > v_fecha_fin THEN
        RAISE_APPLICATION_ERROR(
            -20099,
            'Error: La fecha de inicio (' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || 
            ') debe ser anterior o igual a la fecha de fin (' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'
        );
    END IF;
    
    IF v_fecha_inicio IS NULL OR v_fecha_fin IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20098,
            'Error: Las fechas de inicio y fin no pueden ser nulas.'
        );
    END IF;

    -- 2. Validate that the doctor exists
    SELECT COUNT(*)
    INTO v_medico_existe
    FROM MEDICO
    WHERE matricula = v_matricula;
    
    IF v_medico_existe = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20100,
            'Error: El medico con matricula ' || v_matricula || ' no existe.'
        );
    END IF;
    
    -- 3. Check for overlapping vacations for the same doctor
    -- Two periods overlap if: start1 < end2 AND start2 < end1
    SELECT COUNT(*)
    INTO v_vacaciones_solapadas
    FROM VACACIONES
    WHERE matricula = v_matricula
        AND fecha_inicio < v_fecha_fin
        AND fecha_fin > v_fecha_inicio;
    
    IF v_vacaciones_solapadas > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20101,
            'Error: El medico ya tiene vacaciones que se solapan con el periodo solicitado ' ||
            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'
        );
    END IF;
    
    -- 4. Check for guard duty conflicts (Restriction #11)
    -- A doctor cannot be on guard if they are on vacation that day
    SELECT COUNT(*)
    INTO v_guardias_conflicto
    FROM GUARDIA
    WHERE matricula = v_matricula
        AND TRUNC(fecha_hora) BETWEEN v_fecha_inicio AND v_fecha_fin;
    
    IF v_guardias_conflicto > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20102,
            'Error: El medico tiene ' || v_guardias_conflicto || ' guardia(s) programada(s) durante el periodo de vacaciones ' ||
            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || '). ' ||
            'Debe reasignar o cancelar las guardias antes de solicitar vacaciones.'
        );
    END IF;
    
    -- 5. Insert the vacation
    INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin)
    VALUES (v_matricula, v_fecha_inicio, v_fecha_fin);
    
    -- Commit the transaction
    COMMIT;
    
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Exito: Vacaciones agregadas correctamente');
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Medico (matricula): ' || v_matricula);
    DBMS_OUTPUT.PUT_LINE('Periodo: ' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD'));
    DBMS_OUTPUT.PUT_LINE('Dias de vacaciones: ' || (v_fecha_fin - v_fecha_inicio + 1));
    DBMS_OUTPUT.PUT_LINE('===================================');
    
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback on any error
        ROLLBACK TO inicio_transaccion;
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('TRANSACCION REVERTIDA');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        
        -- Special handling for serialization conflicts
        IF SQLCODE = -8177 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('NOTA: Conflicto de serializacion detectado.');
            DBMS_OUTPUT.PUT_LINE('Otra transaccion modifico los datos concurrentemente.');
            DBMS_OUTPUT.PUT_LINE('Se recomienda reintentar la operacion.');
        END IF;
        
        DBMS_OUTPUT.PUT_LINE('===================================');
        RAISE;
END;
/

-- =========================================================
-- Additional query: Check existing vacations for a doctor
-- =========================================================
-- Uncomment to see existing vacations
/*
SELECT 
    v.matricula,
    m.cuil_cuit,
    p.nombre,
    p.apellido,
    v.fecha_inicio,
    v.fecha_fin,
    (v.fecha_fin - v.fecha_inicio + 1) AS dias_vacaciones
FROM VACACIONES v
JOIN MEDICO m ON v.matricula = m.matricula
JOIN PERSONA p ON m.tipo_documento = p.tipo_documento AND m.nro_documento = p.nro_documento
WHERE v.matricula = 1
ORDER BY v.fecha_inicio;
*/

-- =========================================================
-- Additional query: Check guardias during a period
-- =========================================================
-- Uncomment to check if a doctor has guardias in a specific period
/*
SELECT 
    g.nro_guardia,
    g.fecha_hora,
    g.matricula,
    e.descripcion AS especialidad,
    t.horario AS turno
FROM GUARDIA g
JOIN ESPECIALIDAD e ON g.cod_especialidad = e.cod_especialidad
JOIN TURNO t ON g.id_turno = t.id_turno
WHERE g.matricula = 1
    AND TRUNC(g.fecha_hora) BETWEEN TO_DATE('2024-12-20', 'YYYY-MM-DD') 
                                AND TO_DATE('2024-12-31', 'YYYY-MM-DD')
ORDER BY g.fecha_hora;
*/