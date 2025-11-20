ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- =========================================================
-- 1. Resumen: cantidad de camas LIBRES por sector
-- =========================================================

CREATE OR REPLACE PROCEDURE sp_camas_disponibles_resumen (p_resultado OUT SYS_REFCURSOR) AS
BEGIN
        OPEN p_resultado FOR
        SELECT  s.id_sector,
                s.descripcion,
                COUNT(c.nro_cama) AS camas_libres
        FROM    SECTOR s
        JOIN    HABITACION h    ON   h.id_sector = s.id_sector
        JOIN    CAMA c          ON   c.nro_habitacion = h.nro_habitacion
        WHERE   c.estado = 'LIBRE'
        GROUP BY    s.id_sector, s.descripcion
        ORDER BY    s.descripcion;
END;
/

-- =========================================================
-- 2. Detalle: camas LIBRES de un sector específico
-- =========================================================

CREATE OR REPLACE PROCEDURE sp_camas_disponibles_detalle (
        p_id_sector IN  SECTOR.id_sector%TYPE,
        p_resultado OUT SYS_REFCURSOR
) AS
BEGIN
        OPEN p_resultado FOR
        SELECT  s.id_sector,
                s.descripcion,
                h.nro_habitacion,
                h.piso,
                h.orientacion,
                c.nro_cama,
                c.estado
        FROM    SECTOR s
        JOIN    HABITACION h ON   h.id_sector = s.id_sector
        JOIN    CAMA c       ON   c.nro_habitacion = h.nro_habitacion
        WHERE   c.estado   = 'LIBRE'    AND     s.id_sector = p_id_sector
        ORDER BY    h.piso,
                    h.nro_habitacion,
                    c.nro_cama;
END;
/
