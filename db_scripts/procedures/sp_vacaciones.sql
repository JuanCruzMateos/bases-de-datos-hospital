-- =========================================================
-- Stored Procedure: sp_agregar_vacaciones
-- Purpose: Add vacation period for a doctor with validations
--
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
-- Parameters:
--   p_matricula     - Doctor's registration number
--   p_fecha_inicio  - Vacation start date
--   p_fecha_fin     - Vacation end date
--
-- Usage Example:
--   EXEC sp_agregar_vacaciones(1001, TO_DATE('2025-12-20', 'YYYY-MM-DD'), TO_DATE('2025-12-31', 'YYYY-MM-DD'));
-- =========================================================

ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

CREATE OR REPLACE PROCEDURE sp_agregar_vacaciones (
    p_matricula     IN NUMBER,
    p_fecha_inicio  IN DATE,
    p_fecha_fin     IN DATE
)
IS
    v_guardias_conflicto    NUMBER;
    v_vacaciones_solapadas  NUMBER;
    v_medico_existe         NUMBER;
    v_nombre                VARCHAR2(100);
    v_apellido              VARCHAR2(100);
    v_dias_vacaciones       NUMBER;
BEGIN
    -- Validate que el rango de fechas es valido
    IF p_fecha_inicio IS NULL OR p_fecha_fin IS NULL THEN
        RAISE_APPLICATION_ERROR(
            -20098,
            'Error: Las fechas de inicio y fin no pueden ser nulas.'
        );
    END IF;
    
    IF p_fecha_inicio > p_fecha_fin THEN
        RAISE_APPLICATION_ERROR(
            -20099,
            'Error: La fecha de inicio (' || TO_CHAR(p_fecha_inicio, 'YYYY-MM-DD') || 
            ') debe ser anterior o igual a la fecha de fin (' || TO_CHAR(p_fecha_fin, 'YYYY-MM-DD') || ').'
        );
    END IF;

    -- Validate que el medico existe y obtener sus datos
    BEGIN
        SELECT p.nombre, p.apellido
        INTO v_nombre, v_apellido
        FROM MEDICO m
        JOIN PERSONA p ON m.tipo_documento = p.tipo_documento 
                      AND m.nro_documento = p.nro_documento
        WHERE m.matricula = p_matricula;
        
        v_medico_existe := 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(
                -20100,
                'Error: El medico con matricula ' || p_matricula || ' no existe.'
            );
    END;
    
    -- Check para prevenir que un medico tenga vacaciones que se solapen con el periodo solicitado
    -- Dos periodos se solapan si: start1 < end2 AND start2 < end1
    SELECT COUNT(*)
    INTO v_vacaciones_solapadas
    FROM VACACIONES
    WHERE matricula = p_matricula
        AND fecha_inicio < p_fecha_fin
        AND fecha_fin > p_fecha_inicio;
    
    IF v_vacaciones_solapadas > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20101,
            'Error: El medico ya tiene vacaciones que se solapan con el periodo solicitado ' ||
            '(' || TO_CHAR(p_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(p_fecha_fin, 'YYYY-MM-DD') || ').'
        );
    END IF;
    
    -- Check para prevenir que un medico esté en guardia durante su periodo de vacaciones
    -- Un medico no puede estar en guardia durante su periodo de vacaciones
    SELECT COUNT(*)
    INTO v_guardias_conflicto
    FROM GUARDIA
    WHERE matricula = p_matricula
        AND TRUNC(fecha_hora) BETWEEN p_fecha_inicio AND p_fecha_fin;
    
    IF v_guardias_conflicto > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20102,
            'Error: El medico tiene ' || v_guardias_conflicto || ' guardia(s) programada(s) durante el periodo de vacaciones ' ||
            '(' || TO_CHAR(p_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(p_fecha_fin, 'YYYY-MM-DD') || '). ' ||
            'Debe reasignar o cancelar las guardias antes de solicitar vacaciones.'
        );
    END IF;
    
    -- Insert the vacation
    INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin)
    VALUES (p_matricula, p_fecha_inicio, p_fecha_fin);
    
    -- Calculate vacation days
    v_dias_vacaciones := p_fecha_fin - p_fecha_inicio + 1;
    
    -- Success message
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Exito: Vacaciones agregadas correctamente');
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Medico: ' || v_nombre || ' ' || v_apellido);
    DBMS_OUTPUT.PUT_LINE('Matricula: ' || p_matricula);
    DBMS_OUTPUT.PUT_LINE('Periodo: ' || TO_CHAR(p_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(p_fecha_fin, 'YYYY-MM-DD'));
    DBMS_OUTPUT.PUT_LINE('Dias de vacaciones: ' || v_dias_vacaciones);
    DBMS_OUTPUT.PUT_LINE('===================================');
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback on any error
        ROLLBACK;
        
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('ERROR: TRANSACCION REVERTIDA');
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
        
        -- Re-raise the exception
        RAISE;
END sp_agregar_vacaciones;
/

-- =========================================================
-- Test Examples
-- =========================================================

-- Example 1: Add vacation successfully
-- EXEC sp_agregar_vacaciones(1001, TO_DATE('2025-12-20', 'YYYY-MM-DD'), TO_DATE('2025-12-31', 'YYYY-MM-DD'));

-- Example 2: Try to add overlapping vacation (should fail)
-- EXEC sp_agregar_vacaciones(1001, TO_DATE('2025-12-25', 'YYYY-MM-DD'), TO_DATE('2026-01-05', 'YYYY-MM-DD'));

-- Example 3: Try with invalid date range (should fail)
-- EXEC sp_agregar_vacaciones(1001, TO_DATE('2025-12-31', 'YYYY-MM-DD'), TO_DATE('2025-12-20', 'YYYY-MM-DD'));

-- Example 4: Try with non-existent doctor (should fail)
-- EXEC sp_agregar_vacaciones(9999, TO_DATE('2025-12-20', 'YYYY-MM-DD'), TO_DATE('2025-12-31', 'YYYY-MM-DD'));

-- =========================================================
-- Helper Query: Check existing vacations for a doctor
-- =========================================================
/*
SELECT 
    v.matricula,
    p.nombre || ' ' || p.apellido AS medico,
    v.fecha_inicio,
    v.fecha_fin,
    (v.fecha_fin - v.fecha_inicio + 1) AS dias_vacaciones
FROM VACACIONES v
JOIN MEDICO m ON v.matricula = m.matricula
JOIN PERSONA p ON m.tipo_documento = p.tipo_documento 
              AND m.nro_documento = p.nro_documento
WHERE v.matricula = 1001
ORDER BY v.fecha_inicio;
*/

-- =========================================================
-- Helper Query: Check guardias during a period
-- =========================================================
/*
SELECT 
    g.nro_guardia,
    g.fecha_hora,
    g.matricula,
    p.nombre || ' ' || p.apellido AS medico,
    e.descripcion AS especialidad,
    t.horario AS turno
FROM GUARDIA g
JOIN MEDICO m ON g.matricula = m.matricula
JOIN PERSONA p ON m.tipo_documento = p.tipo_documento 
              AND m.nro_documento = p.nro_documento
JOIN ESPECIALIDAD e ON g.cod_especialidad = e.cod_especialidad
JOIN TURNO t ON g.id_turno = t.id_turno
WHERE g.matricula = 1001
    AND TRUNC(g.fecha_hora) BETWEEN TO_DATE('2025-12-20', 'YYYY-MM-DD') 
                                AND TO_DATE('2025-12-31', 'YYYY-MM-DD')
ORDER BY g.fecha_hora;
*/

