ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- ========================================================
-- Auditoria: listado completo de auditoria de guardias
-- ========================================================

CREATE OR REPLACE PROCEDURE sp_auditoria_guardias (
    p_usuario   IN AUDITORIA_GUARDIA.usuario_bd%TYPE      DEFAULT NULL,
    p_desde     IN AUDITORIA_GUARDIA.fecha_hora_reg%TYPE  DEFAULT NULL,
    p_hasta     IN AUDITORIA_GUARDIA.fecha_hora_reg%TYPE  DEFAULT NULL,
    p_resultado OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_resultado FOR
        SELECT  id_auditoria,       -- ID de la auditoría
                fecha_hora_reg,     -- Fecha y hora del registro de auditoría (trigger)
                usuario_bd,         -- Usuario de la base de datos que realizó un cambio sobre GUARDIA  
                operacion,          -- Tipo de operación que disparo el trigger: INSERT, UPDATE, DELETE
                nro_guardia,        -- Número de guardia afectada
                fecha_hora_guard,   -- Fecha y hora de la guardia afectada
                matricula,      
                cod_especialidad,
                id_turno,
                detalle_old,        -- Detalle de los valores anteriores (antes del cambio)
                detalle_new         -- Detalle de los nuevos valores (después del cambio)
        FROM    AUDITORIA_GUARDIA
        WHERE   (p_usuario IS NULL OR usuario_bd = p_usuario)       -- Si el parámetro viene NULL, NO filtro por ese atributo
        AND     (p_desde  IS NULL OR fecha_hora_reg >= p_desde)     -- Si viene con un valor, SÍ filtro por ese atributo
        AND     (p_hasta  IS NULL OR fecha_hora_reg <= p_hasta)
        ORDER BY fecha_hora_reg DESC;
END;
/

/*
Ejemplos de invocación por consola SQL*Plus:

    1) Listar TODA la auditoría (sin filtros)
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(p_resultado => :rc);
    PRINT rc;

    2) Listar auditoría SOLO de un usuario
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_usuario   => 'HOSPITAL',
        p_resultado => :rc
    );
    PRINT rc;

    3) Listar auditoría en un RANGO de fechas (todos los usuarios)
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_desde     => TO_TIMESTAMP('2025-11-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hasta     => TO_TIMESTAMP('2025-11-30 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        p_resultado => :rc
    );
    PRINT rc;

    4) Listar auditoría de UN usuario en un RANGO de fechas
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_usuario   => 'HOSPITAL',
        p_desde     => TO_TIMESTAMP('2025-11-19 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hasta     => TO_TIMESTAMP('2025-11-19 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        p_resultado => :rc
    );
    PRINT rc;
*/