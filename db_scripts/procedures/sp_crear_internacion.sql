
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

-- =============================================================================
-- 2. Internacion: cambiar cama de una internación activa
-- =============================================================================

CREATE OR REPLACE PROCEDURE sp_cambiar_cama_internacion (
    p_nro_internacion IN INTERNACION.nro_internacion%TYPE,
    p_nro_habitacion  IN CAMA.nro_habitacion%TYPE,
    p_nro_cama        IN CAMA.nro_cama%TYPE,
    p_fecha_ingreso   IN SE_UBICA.fecha_hora_ingreso%TYPE DEFAULT SYSTIMESTAMP
) AS
    v_fecha_inicio   INTERNACION.fecha_inicio%TYPE;
    v_fecha_fin      INTERNACION.fecha_fin%TYPE;
    v_old_cama       CAMA.nro_cama%TYPE;
    v_old_habitacion CAMA.nro_habitacion%TYPE;
BEGIN
    -- 1) Verificamos que la internacion exista y este abierta
    SELECT fecha_inicio, fecha_fin
    INTO   v_fecha_inicio, v_fecha_fin
    FROM   INTERNACION
    WHERE  nro_internacion = p_nro_internacion;

    IF v_fecha_fin IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(
            -20010,
            'No se puede cambiar la cama de una internacion ya finalizada.'
        );
    END IF;

    -- 2) Obtenemos la ultima ubicacion registrada
    SELECT su.nro_cama, su.nro_habitacion
    INTO   v_old_cama, v_old_habitacion
    FROM   SE_UBICA su
    WHERE  su.nro_internacion = p_nro_internacion
    ORDER BY su.fecha_hora_ingreso DESC
    FETCH FIRST 1 ROW ONLY;

    -- Si la cama nueva es la misma, no hacemos nada
    IF v_old_cama = p_nro_cama  AND v_old_habitacion = p_nro_habitacion THEN
        RETURN;
    END IF;

    -- 3) Liberar la cama anterior
    UPDATE CAMA
        SET estado = 'LIBRE'
    WHERE nro_cama       = v_old_cama   AND nro_habitacion = v_old_habitacion;

    /*
    4) Insertar nueva ubicacion
        El trigger tr_se_ubica_cama_estado:
            - verificara que la nueva cama este LIBRE
            - verificara que la fecha este dentro de la internacion
            - marcara la nueva cama como OCUPADA
    */
    INSERT INTO SE_UBICA (
        nro_internacion,
        fecha_hora_ingreso,
        nro_cama,
        nro_habitacion
    ) VALUES (
        p_nro_internacion,
        p_fecha_ingreso,
        p_nro_cama,
        p_nro_habitacion
    );

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- No hay ubicaciones para esa internacion
        RAISE_APPLICATION_ERROR(
            -20011,
            'No se puede cambiar la cama: la internacion no tiene ubicaciones registradas.'
        );
END;
/
