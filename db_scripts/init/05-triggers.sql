
-- =========================================================
-- 1. Auditoria: registro de cambios en la tabla GUARDIA
-- =========================================================

CREATE OR REPLACE TRIGGER tr_aud_guardia
AFTER INSERT OR UPDATE OR DELETE ON GUARDIA
FOR EACH ROW
DECLARE
    v_operacion   VARCHAR2(10);
    v_detalle_old VARCHAR2(4000);
    v_detalle_new VARCHAR2(4000);
BEGIN
    IF INSERTING THEN
        v_operacion   := 'INSERT';
        v_detalle_old := NULL;
        v_detalle_new :=
              'nro_guardia='        || :NEW.nro_guardia
           || ', fecha_hora='       || TO_CHAR(:NEW.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
           || ', matricula='        || :NEW.matricula
           || ', cod_especialidad=' || :NEW.cod_especialidad
           || ', id_turno='         || :NEW.id_turno;

    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_detalle_old :=
              'nro_guardia='        || :OLD.nro_guardia
           || ', fecha_hora='       || TO_CHAR(:OLD.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
           || ', matricula='        || :OLD.matricula
           || ', cod_especialidad=' || :OLD.cod_especialidad
           || ', id_turno='         || :OLD.id_turno;

        v_detalle_new :=
              'nro_guardia='        || :NEW.nro_guardia
           || ', fecha_hora='       || TO_CHAR(:NEW.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
           || ', matricula='        || :NEW.matricula
           || ', cod_especialidad=' || :NEW.cod_especialidad
           || ', id_turno='         || :NEW.id_turno;

    ELSIF DELETING THEN
        v_operacion   := 'DELETE';
        v_detalle_old :=
              'nro_guardia='        || :OLD.nro_guardia
           || ', fecha_hora='       || TO_CHAR(:OLD.fecha_hora, 'YYYY-MM-DD HH24:MI:SS')
           || ', matricula='        || :OLD.matricula
           || ', cod_especialidad=' || :OLD.cod_especialidad
           || ', id_turno='         || :OLD.id_turno;
        v_detalle_new := NULL;
    END IF;

    INSERT INTO AUDITORIA_GUARDIA (
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
    ) VALUES (
        SYSTIMESTAMP,
        USER,
        v_operacion,
        NVL(:NEW.nro_guardia,      :OLD.nro_guardia),
        NVL(:NEW.fecha_hora,       :OLD.fecha_hora),
        NVL(:NEW.matricula,        :OLD.matricula),
        NVL(:NEW.cod_especialidad, :OLD.cod_especialidad),
        NVL(:NEW.id_turno,         :OLD.id_turno),
        v_detalle_old,
        v_detalle_new
    );
END;
/

