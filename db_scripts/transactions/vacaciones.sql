
-- =========================================================
-- Reglas de negocio:
-- 1. Un medico no puede estar de guardia si esta de vacaciones ese dia
-- 2. Las vacaciones no deben solaparse para el mismo medico
-- 3. El rango de fechas debe ser valido (inicio <= fin)
-- 4. Todas las fechas deben ser no nulas
--
-- Codigos de error:
-- -20098: Fechas nulas ingresadas
-- -20099: Rango de fechas invalido (inicio > fin)
-- -20100: El medico no existe
-- -20101: Vacaciones solapadas detectadas
-- -20102: Conflicto de guardias durante el periodo de vacaciones
--
-- Control de la transaccion:
-- - Usa nivel SERIALIZABLE para prevenir condiciones de carrera
-- - Usa SAVEPOINT para permitir rollback
-- - Hace commit solo si todo finaliza correctamente
-- - Hace rollback al savepoint ante cualquier error
-- - Maneja conflictos de serializacion ORA-08177 (se recomienda reintentar)
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
    -- Inicia la transaccion con nivel SERIALIZABLE
    -- Previene condiciones de carrera entre las validaciones y el INSERT
    SAVEPOINT inicio_transaccion;
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

    -- 1. Validar rango de fechas
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

    -- 2. Validar que el medico exista
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
    
    -- 3. Revisar vacaciones solapadas para el mismo medico
    -- Dos periodos se solapan si: inicio1 < fin2 Y inicio2 < fin1
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
    
    -- 4. Revisar conflictos de guardias (Restriccion #11)
    -- Un medico no puede estar de guardia si esta de vacaciones ese dia
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
    
    -- 5. Insertar las vacaciones
    INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin)
    VALUES (v_matricula, v_fecha_inicio, v_fecha_fin);
    
    -- Hacer commit de la transaccion
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
        -- Rollback ante cualquier error
        ROLLBACK TO inicio_transaccion;
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('TRANSACCION REVERTIDA');
        DBMS_OUTPUT.PUT_LINE('===================================');
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Codigo de error: ' || SQLCODE);
        
        -- Manejo especial para conflictos de serializacion
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
-- Consulta adicional: Ver vacaciones existentes para un medico
-- =========================================================
-- Descomentar para ver vacaciones existentes
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
-- Consulta adicional: Ver guardias durante un periodo
-- =========================================================
-- Descomentar para ver si un medico tiene guardias en un periodo especifico
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
