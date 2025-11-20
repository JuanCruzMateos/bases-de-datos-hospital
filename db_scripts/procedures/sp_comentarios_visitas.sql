ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- =========================================================
-- 1. Internaciones: lista de internaciones de un paciente
-- =========================================================

CREATE OR REPLACE PROCEDURE sp_internaciones_paciente (
    p_tipo_doc   IN PACIENTE.tipo_documento%TYPE,
    p_nro_doc    IN PACIENTE.nro_documento%TYPE,
    p_resultado  OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_resultado FOR
        SELECT  i.nro_internacion,
                i.fecha_inicio,
                i.fecha_fin
        FROM    INTERNACION i
        WHERE   i.tipo_documento = p_tipo_doc
        AND     i.nro_documento  = p_nro_doc
        ORDER BY i.fecha_inicio DESC;
END;
/

-- =============================================================================
-- 2. Comenatrios: comentarios de visitas medicas de una internación especifica
-- =============================================================================

CREATE OR REPLACE PROCEDURE sp_comentarios_visitas (
    p_nro_internacion IN  INTERNACION.nro_internacion%TYPE,
    p_resultado       OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_resultado FOR
        SELECT
            cs.nro_internacion,
            -- Paciente
            per_pac.apellido || ', ' || per_pac.nombre AS paciente,
            -- Médico que hizo el comentario
            per_med.apellido || ', ' || per_med.nombre AS medico,
            -- Datos de la visita
            r.fecha_recorrido,
            r.hora_inicio,
            r.hora_fin,
            -- Comentario
            cs.comentario
        FROM        COMENTA_SOBRE cs
        JOIN        INTERNACION i   ON  i.nro_internacion = cs.nro_internacion
        JOIN        PACIENTE p      ON  p.tipo_documento = i.tipo_documento         AND p.nro_documento  = i.nro_documento
        JOIN        PERSONA per_pac ON  per_pac.tipo_documento = p.tipo_documento   AND per_pac.nro_documento  = p.nro_documento
        JOIN        RECORRIDO r     ON  r.id_recorrido = cs.id_recorrido
        JOIN        MEDICO m        ON  m.matricula = r.matricula
        JOIN        PERSONA per_med ON  per_med.tipo_documento = m.tipo_documento   AND per_med.nro_documento  = m.nro_documento
        WHERE       cs.nro_internacion = p_nro_internacion
        ORDER BY    r.fecha_recorrido,
                    r.hora_inicio;
END;
/
