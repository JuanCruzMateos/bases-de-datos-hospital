-- update estado de cama
UPDATE HOSPITAL.CAMA 
SET ESTADO = 'OCUPADA'
WHERE TRUE
	AND NRO_HABITACION = 202
	AND NRO_CAMA = 1;
COMMIT;

-- modifico una guardia
UPDATE GUARDIA SET id_turno = 2 WHERE nro_guardia = 1;
INSERT INTO GUARDIA (fecha_hora, matricula, cod_especialidad, id_turno) VALUES (SYSTIMESTAMP, 1001, 101, 1);
DELETE FROM GUARDIA WHERE nro_guardia = 2;
COMMIT;


-- ejecutar un sp desde dbeaver
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id_sector NUMBER;
    v_descripcion VARCHAR2(100);
    v_camas_libres NUMBER;
BEGIN
    -- Call the procedure
    sp_camas_disponibles_resumen(v_cursor);
    
    -- Display results
    DBMS_OUTPUT.PUT_LINE('ID_SECTOR | DESCRIPCION | CAMAS_LIBRES');
    DBMS_OUTPUT.PUT_LINE('---------------------------------------');
    
    LOOP
        FETCH v_cursor INTO v_id_sector, v_descripcion, v_camas_libres;
        EXIT WHEN v_cursor%NOTFOUND;
        
        DBMS_OUTPUT.PUT_LINE(v_id_sector || ' | ' || v_descripcion || ' | ' || v_camas_libres);
    END LOOP;
    
    CLOSE v_cursor;
END;

