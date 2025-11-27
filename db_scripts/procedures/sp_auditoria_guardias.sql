ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- Auditoría sobre los usuarios que hacen cambios a datos que afectan el 
-- proceso de asignación de guardias (requerimiento del área de asignación de guardias) 

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
1) INICIO SESION SQLPLUS
docker exec -it oracle-hospital sqlplus hospital/hospital123@//localhost:1521/FREEPDB1

2) LE AGREGO UNA TUPLA A LA TABLA GUARDIA
UPDATE GUARDIA SET id_turno = 2 WHERE nro_guardia = 1;
INSERT INTO GUARDIA (fecha_hora, matricula, cod_especialidad, id_turno) VALUES (SYSTIMESTAMP, 1001, 101, 1);
DELETE FROM GUARDIA WHERE nro_guardia = 2;
COMMIT;

3.1) EJECUTO EL PROCEDIMIENTO ALMACENADO PARA VER LOS CAMBIOS AUDITADOS (SIN FILTROS, TODOS LOS CAMPOS)
VAR rc REFCURSOR
EXEC sp_auditoria_guardias(p_resultado => :rc);
PRINT rc;

3.2) EJEMPLO DE FILTRADO POR FECHAS 
VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_desde     => TO_TIMESTAMP('2025-11-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hasta     => TO_TIMESTAMP('2025-11-30 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        p_resultado => :rc
    );
    PRINT rc;

3.3) EJEMPLO DE FILTRADO POR FECHAS Y USUARIO
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_usuario   => 'HOSPITAL',
        p_desde     => TO_TIMESTAMP('2025-11-19 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_hasta     => TO_TIMESTAMP('2025-11-19 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        p_resultado => :rc
    );
    PRINT rc;

3.4) EJEMPLO DE FILTRADO SOLO POR UN USUARIO
    VAR rc REFCURSOR;
    EXEC sp_auditoria_guardias(
        p_usuario   => 'HOSPITAL',
        p_resultado => :rc
    );
    PRINT rc;


3.3) CONFIGURACION DE SALIDA PARA MEJOR VISUALIZACION

SET PAGESIZE 500
SET LINESIZE 200
SET LONG 4000
SET LONGCHUNKSIZE 4000
SET TRIMSPOOL ON
COLUMN usuario_bd        FORMAT A15
COLUMN operacion         FORMAT A8
COLUMN detalle_old       FORMAT A80 WORD_WRAPPED
COLUMN detalle_new       FORMAT A80 WORD_WRAPPED

VAR rc REFCURSOR
EXEC sp_auditoria_guardias(p_resultado => :rc);
PRINT rc;


*/

