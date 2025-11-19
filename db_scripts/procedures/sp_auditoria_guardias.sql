
-- ========================================================
-- 1. Auditoria: listado completo de auditoria de guardias
-- ========================================================

CREATE OR REPLACE PROCEDURE sp_auditoria_guardias_todo (
    p_resultado OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_resultado FOR
        SELECT  id_auditoria,
                fecha_hora_reg,     -- fecha cuándo se hizo el cambio
                usuario_bd,
                operacion,
                nro_guardia,
                fecha_hora_guard,   -- fecha asignada a la guardia
                matricula,
                cod_especialidad,
                id_turno,
                detalle_old,
                detalle_new
        FROM    AUDITORIA_GUARDIA
        ORDER BY fecha_hora_reg DESC;
END;
/

-- ====================================================================
-- 2. Auditoria: listado completo de auditoria de guardias por usuario
-- ====================================================================

CREATE OR REPLACE PROCEDURE sp_auditoria_guardias_por_usuario (
    p_usuario   IN  AUDITORIA_GUARDIA.usuario_bd%TYPE,
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
        WHERE   usuario_bd = p_usuario
        ORDER BY fecha_hora_reg DESC;
END;
/
