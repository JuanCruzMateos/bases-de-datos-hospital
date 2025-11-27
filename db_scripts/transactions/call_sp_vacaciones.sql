-- =========================================================
-- Transaction Script: Call sp_agregar_vacaciones
-- Purpose: Invoke the vacation procedure with proper transaction control
--
-- This script demonstrates how to call the stored procedure with:
-- - SERIALIZABLE isolation level for maximum data consistency
-- - Proper error handling and rollback capability
-- - Transaction control using SAVEPOINT
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
    v_matricula         NUMBER := 1001;  -- Cambiar segun necesidad
    v_fecha_inicio      DATE := TO_DATE('2025-12-20', 'YYYY-MM-DD');
    v_fecha_fin         DATE := TO_DATE('2025-12-31', 'YYYY-MM-DD');
BEGIN
    -- Start transaction con isolation SERIALIZABLE
    -- Esto previene Dirty Reads, Non-Repeatable Reads y Phantom Records
    -- Default es READ COMMITTED (otras alternativas son: READ UNCOMMITTED, REPEATABLE READ)
    SAVEPOINT inicio_transaccion;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Iniciando transaccion con nivel de aislamiento SERIALIZABLE');
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('');
    
    -- Call the stored procedure
    sp_agregar_vacaciones(
        p_matricula    => v_matricula,
        p_fecha_inicio => v_fecha_inicio,
        p_fecha_fin    => v_fecha_fin
    );
    
    -- Note: COMMIT is handled inside the procedure
    
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback to savepoint on any error
        ROLLBACK TO inicio_transaccion;
        
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('TRANSACCION REVERTIDA (SAVEPOINT)');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        
        -- Special handling for serialization conflicts
        IF SQLCODE = -8177 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('NOTA: Conflicto de serializacion detectado (ORA-08177).');
            DBMS_OUTPUT.PUT_LINE('Otra transaccion modifico los datos concurrentemente.');
            DBMS_OUTPUT.PUT_LINE('Se recomienda reintentar la operacion.');
        END IF;
        
        DBMS_OUTPUT.PUT_LINE('===================================');
        
        -- Re-raise to ensure error is visible
        RAISE;
END;
/

-- =========================================================
-- Alternative: Multiple Vacation Periods in One Transaction
-- =========================================================
/*
DECLARE
    TYPE vacation_record IS RECORD (
        matricula      NUMBER,
        fecha_inicio   DATE,
        fecha_fin      DATE
    );
    
    TYPE vacation_table IS TABLE OF vacation_record;
    
    v_vacations vacation_table := vacation_table(
        vacation_record(1001, TO_DATE('2025-12-20', 'YYYY-MM-DD'), TO_DATE('2025-12-31', 'YYYY-MM-DD')),
        vacation_record(1002, TO_DATE('2025-12-15', 'YYYY-MM-DD'), TO_DATE('2025-12-25', 'YYYY-MM-DD')),
        vacation_record(1003, TO_DATE('2026-01-05', 'YYYY-MM-DD'), TO_DATE('2026-01-15', 'YYYY-MM-DD'))
    );
BEGIN
    SAVEPOINT inicio_transaccion;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Procesando ' || v_vacations.COUNT || ' solicitudes de vacaciones');
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('');
    
    -- Process each vacation request
    FOR i IN 1..v_vacations.COUNT LOOP
        DBMS_OUTPUT.PUT_LINE('--- Procesando solicitud ' || i || ' de ' || v_vacations.COUNT || ' ---');
        
        sp_agregar_vacaciones(
            p_matricula    => v_vacations(i).matricula,
            p_fecha_inicio => v_vacations(i).fecha_inicio,
            p_fecha_fin    => v_vacations(i).fecha_fin
        );
        
        DBMS_OUTPUT.PUT_LINE('');
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('===================================');
    DBMS_OUTPUT.PUT_LINE('Todas las solicitudes procesadas exitosamente');
    DBMS_OUTPUT.PUT_LINE('===================================');
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO inicio_transaccion;
        
        DBMS_OUTPUT.PUT_LINE('');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('ERROR: TRANSACCION COMPLETA REVERTIDA');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        
        IF SQLCODE = -8177 THEN
            DBMS_OUTPUT.PUT_LINE('');
            DBMS_OUTPUT.PUT_LINE('NOTA: Conflicto de serializacion detectado.');
            DBMS_OUTPUT.PUT_LINE('Se recomienda reintentar la operacion completa.');
        END IF;
        
        DBMS_OUTPUT.PUT_LINE('===================================');
        RAISE;
END;
/
*/

-- =========================================================
-- Test Scenarios
-- =========================================================

-- Scenario 1: Successful vacation addition
-- Change variables at the top to: 1001, '2025-12-20', '2025-12-31'

-- Scenario 2: Overlapping vacation (should fail)
-- First run with: 1001, '2025-12-20', '2025-12-31'
-- Then run with: 1001, '2025-12-25', '2026-01-05' (overlaps)

-- Scenario 3: Invalid date range (should fail)
-- Change variables to: 1001, '2025-12-31', '2025-12-20' (end before start)

-- Scenario 4: Doctor with guardias during vacation period (should fail)
-- First check guardias for a doctor in a period, then try to add vacation

-- Scenario 5: Non-existent doctor (should fail)
-- Change matricula to: 9999

