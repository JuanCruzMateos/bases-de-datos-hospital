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

-- =========================================================
-- 3. Insercion: agregar una nueva cama a una habitación
-- =========================================================

CREATE OR REPLACE PROCEDURE sp_agregar_cama (
        p_nro_habitacion IN  HABITACION.nro_habitacion%TYPE,
        p_nro_cama       IN OUT CAMA.nro_cama%TYPE
) AS
        v_count    NUMBER;
        v_nro_cama CAMA.nro_cama%TYPE;
BEGIN
        -- 0) Normalizamos: si viene 0 o negativo, lo tratamos como NULL
        IF p_nro_cama <= 0 THEN
                p_nro_cama := NULL;
        END IF;

        -- 1) Verificamos que la habitación exista
        SELECT COUNT(*)
        INTO   v_count
        FROM   HABITACION
        WHERE  nro_habitacion = p_nro_habitacion;

        IF v_count = 0 THEN
                RAISE_APPLICATION_ERROR(
                -20030,
                'La habitación ' || p_nro_habitacion || ' no existe.'
                );
        END IF;

        -- 2) Decidimos el número de cama
        IF p_nro_cama IS NOT NULL THEN
                -- Caso A: el usuario pidió un número específico

                -- 2.a) Verificamos que no exista ya esa cama en esa habitación
                SELECT COUNT(*)
                INTO   v_count
                FROM   CAMA
                WHERE  nro_habitacion = p_nro_habitacion
                AND    nro_cama       = p_nro_cama;

                IF v_count > 0 THEN
                        RAISE_APPLICATION_ERROR(
                        -20032,
                        'La cama ' || p_nro_cama || ' ya existe en la habitación ' || p_nro_habitacion || '.'
                        );
                END IF;

                v_nro_cama := p_nro_cama;
        ELSE
                -- Caso B: campo vacío: asignar siguiente número
                SELECT NVL(MAX(nro_cama) + 1, 1)
                INTO   v_nro_cama
                FROM   CAMA
                WHERE  nro_habitacion = p_nro_habitacion;

                p_nro_cama := v_nro_cama;  -- devolvemos el número generado
        END IF;

        -- 3) Insertamos la cama nueva como LIBRE
        INSERT INTO CAMA (nro_habitacion, nro_cama, estado)
        VALUES (p_nro_habitacion, v_nro_cama, 'LIBRE');
END;
/

-- =========================================================
-- 4. Eliminacion: eliminar o desactivar una cama
-- =========================================================

CREATE OR REPLACE PROCEDURE sp_eliminar_o_desactivar_cama (
        p_nro_habitacion IN CAMA.nro_habitacion%TYPE,
        p_nro_cama       IN CAMA.nro_cama%TYPE
) AS
        v_estado   CAMA.estado%TYPE;
        v_count    NUMBER;
BEGIN
        -- 1) Verificar que la cama exista y obtener su estado
        SELECT estado
        INTO   v_estado
        FROM   CAMA
        WHERE  nro_habitacion = p_nro_habitacion
        AND  nro_cama       = p_nro_cama;

        -- 2) Solo se pueden eliminar camas LIBRES
        IF v_estado <> 'LIBRE' THEN
                RAISE_APPLICATION_ERROR(
                -20031,
                'Solo se pueden eliminar o desactivar camas en estado LIBRE.'
                );
        END IF;

        -- 3) Verificamos si tiene historial en SE_UBICA
        SELECT COUNT(*)
        INTO   v_count
        FROM   SE_UBICA
        WHERE  nro_habitacion = p_nro_habitacion
        AND  nro_cama       = p_nro_cama;

        IF v_count = 0 THEN
                -- Caso A: nunca se usó: se puede eliminar físicamente
                DELETE FROM CAMA
                WHERE nro_habitacion = p_nro_habitacion
                AND nro_cama       = p_nro_cama;
        ELSE
                -- Caso B: tiene historial: la marcamos FUERA_DE_SERVICIO
                UPDATE CAMA
                SET estado = 'FUERA_DE_SERVICIO'
                WHERE nro_habitacion = p_nro_habitacion
                AND nro_cama       = p_nro_cama;
        END IF;

EXCEPTION
        WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                -20030,
                'La cama indicada no existe en esa habitación.'
                );
END;
/
