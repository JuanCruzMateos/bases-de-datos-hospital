
-- =============================================================================
-- 1. Internacion: crear Internación con asignación automática de cama
-- =============================================================================
CREATE OR REPLACE PROCEDURE sp_crear_internacion (
    p_tipo_documento  IN INTERNACION.tipo_documento%TYPE,
    p_nro_documento   IN INTERNACION.nro_documento%TYPE,
    p_matricula       IN INTERNACION.matricula%TYPE,
    p_fecha_inicio    IN INTERNACION.fecha_inicio%TYPE,
    p_fecha_fin       IN INTERNACION.fecha_fin%TYPE DEFAULT NULL,
    p_nro_habitacion  IN CAMA.nro_habitacion%TYPE DEFAULT NULL,
    p_nro_cama        IN CAMA.nro_cama%TYPE        DEFAULT NULL,
    p_nro_internacion OUT INTERNACION.nro_internacion%TYPE
) AS
    v_nro_internacion  INTERNACION.nro_internacion%TYPE;
    v_nro_habitacion   CAMA.nro_habitacion%TYPE;
    v_nro_cama         CAMA.nro_cama%TYPE;
    v_usar_cama_espec  BOOLEAN := FALSE;
BEGIN
    -- 1) Crear la internación
    INSERT INTO INTERNACION (
        fecha_inicio,
        fecha_fin,
        tipo_documento,
        nro_documento,
        matricula
    ) VALUES (
        p_fecha_inicio,
        p_fecha_fin,
        p_tipo_documento,
        p_nro_documento,
        p_matricula
    )
    RETURNING nro_internacion INTO v_nro_internacion;

    -- 2) Decidir cama/habitación
    IF p_nro_habitacion IS NOT NULL AND p_nro_cama IS NOT NULL THEN
        -- Caso A: asignamos cama y habitación
        v_usar_cama_espec := TRUE;

        SELECT nro_habitacion, nro_cama
        INTO   v_nro_habitacion, v_nro_cama
        FROM   CAMA
        WHERE  nro_habitacion = p_nro_habitacion
            AND  nro_cama       = p_nro_cama
            AND  estado         = 'LIBRE';

    ELSIF p_nro_habitacion IS NULL AND p_nro_cama IS NULL THEN
        -- Caso B: no se asigno una cama, le asignamos la primera LIBRE
        v_usar_cama_espec := FALSE;

        SELECT nro_habitacion, nro_cama
        INTO   v_nro_habitacion, v_nro_cama
        FROM (
            SELECT c.nro_habitacion, c.nro_cama
            FROM   CAMA c
            WHERE  c.estado = 'LIBRE'
            ORDER BY c.nro_habitacion, c.nro_cama
        )
        WHERE ROWNUM = 1;

    ELSE
        -- Caso inválido: mandaron solo una de las dos cosas
        RAISE_APPLICATION_ERROR(
            -20002,
            'Debe indicar AMBOS campos: nro_habitacion y nro_cama, o ninguno.'
        );
    END IF;

    -- 3) Crear la ubicación en SE_UBICA: el trigger tr_se_ubica_cama_estado valida
    INSERT INTO SE_UBICA (
        nro_internacion,
        fecha_hora_ingreso,
        nro_cama,
        nro_habitacion
    ) VALUES (
        v_nro_internacion,
        SYSTIMESTAMP,
        v_nro_cama,
        v_nro_habitacion
    );

    -- 4) Devolvemos el número de internación a la app
    p_nro_internacion := v_nro_internacion;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        IF v_usar_cama_espec THEN
            -- No existe esa cama o no está LIBRE
            RAISE_APPLICATION_ERROR(
                -20003,
                'La cama/habitación indicada no existe o no está LIBRE.'
            );
        ELSE
            -- No hay ninguna cama LIBRE
            RAISE_APPLICATION_ERROR(
                -20001,
                'No hay camas libres disponibles para asignar la internación.'
            );
        END IF;
END;
/
