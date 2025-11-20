
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
        SELECT  id_auditoria,
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