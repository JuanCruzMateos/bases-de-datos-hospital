/* Script de inicializacion de datos para el Hospital */

/*
    Oracle no soporta la sintaxis INSERT INTO TABLE(column1, column2, ...) VALUES (value1, value2, ...), (value1, value2, ...), ...
    Unicamente es sopotado a partir de la version 23c.

    La sintaxis propia de Oracle es con insert all y select 1 from dual.
    Dual es una tabla de 1 sola fila y 1 sola columna, perteneciente a sys que se utiliza para consultas que no requieran extraer datos de una tabla. (Orace no soporta SELECT sin FROM)
*/

ALTER SESSION SET CONTAINER = FREEPDB1;
-- Set the current schema to the hospital schema
ALTER SESSION SET CURRENT_SCHEMA = hospital;

-- ==============================================
-- 1. PERSONA (Base para Pacientes y Medicos)
-- ==============================================
INSERT ALL
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '32456789', 'Maria', 'Gonzalez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '28765432', 'Carlos', 'Rodriguez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '41234567', 'Ana', 'Martinez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '35678901', 'Jorge', 'Fernandez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '29876543', 'Laura', 'Lopez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '38901234', 'Roberto', 'Sanchez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '42567890', 'Patricia', 'Diaz', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '31234567', 'Diego', 'Torres', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '36789012', 'Silvia', 'Ramirez', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '40123456', 'Fernando', 'Flores', 'PACIENTE')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '25123456', 'Juan Carlos', 'Benitez', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '27234567', 'Gabriela', 'Morales', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '26345678', 'Ricardo', 'Castro', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '28456789', 'Monica', 'Pereyra', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '24567890', 'Alberto', 'Gutierrez', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '29234567', 'Cecilia', 'Romero', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '27890123', 'Pablo', 'Medina', 'MEDICO')
    INTO PERSONA (tipo_documento, nro_documento, nombre, apellido, tipo) VALUES ('DNI', '26789012', 'Veronica', 'Silva', 'MEDICO')
SELECT 1 FROM dual;

-- ==============================================
-- 2. PACIENTE
-- ==============================================
INSERT ALL
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '32456789', TO_DATE('1985-03-15', 'YYYY-MM-DD'), 'F')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '28765432', TO_DATE('1978-07-22', 'YYYY-MM-DD'), 'M')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '41234567', TO_DATE('1992-11-08', 'YYYY-MM-DD'), 'F')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '35678901', TO_DATE('1980-05-30', 'YYYY-MM-DD'), 'M')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '29876543', TO_DATE('1975-09-12', 'YYYY-MM-DD'), 'F')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '38901234', TO_DATE('1988-02-25', 'YYYY-MM-DD'), 'M')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '42567890', TO_DATE('1995-06-18', 'YYYY-MM-DD'), 'F')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '31234567', TO_DATE('1982-12-03', 'YYYY-MM-DD'), 'M')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '36789012', TO_DATE('1986-08-20', 'YYYY-MM-DD'), 'F')
    INTO PACIENTE (tipo_documento, nro_documento, fecha_nacimiento, sexo) VALUES ('DNI', '40123456', TO_DATE('1990-04-14', 'YYYY-MM-DD'), 'M')
SELECT 1 FROM dual;

-- ==============================================
-- 3. SECTOR
-- ==============================================
INSERT ALL
    INTO SECTOR (id_sector, descripcion) VALUES (1, 'Cardiologia')
    INTO SECTOR (id_sector, descripcion) VALUES (2, 'Traumatologia')
    INTO SECTOR (id_sector, descripcion) VALUES (3, 'Pediatria')
    INTO SECTOR (id_sector, descripcion) VALUES (4, 'Cirugia General')
    INTO SECTOR (id_sector, descripcion) VALUES (5, 'Terapia Intensiva')
    INTO SECTOR (id_sector, descripcion) VALUES (6, 'Neurologia')
    INTO SECTOR (id_sector, descripcion) VALUES (7, 'Oncologia')
SELECT 1 FROM dual;

-- ==============================================
-- 4. ESPECIALIDAD
-- ==============================================
INSERT ALL
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (101, 'Cardiologia Clinica', 1)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (102, 'Cirugia Cardiovascular', 1)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (201, 'Traumatologia y Ortopedia', 2)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (301, 'Pediatria General', 3)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (401, 'Cirugia General', 4)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (501, 'Terapia Intensiva', 5)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (601, 'Neurologia Clinica', 6)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (602, 'Neurocirugia', 6)
    INTO ESPECIALIDAD (cod_especialidad, descripcion, id_sector) VALUES (701, 'Oncologia Clinica', 7)
SELECT 1 FROM dual;

-- ==============================================
-- 5. MEDICO
-- ==============================================
INSERT ALL
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1001, '20-25123456-3', TO_DATE('2015-03-01', 'YYYY-MM-DD'), NULL, 6, 'DNI', '25123456')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1002, '27-27234567-4', TO_DATE('2016-07-15', 'YYYY-MM-DD'), NULL, 8, 'DNI', '27234567')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1003, '20-26345678-5', TO_DATE('2017-02-10', 'YYYY-MM-DD'), NULL, 6, 'DNI', '26345678')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1004, '27-28456789-6', TO_DATE('2018-05-20', 'YYYY-MM-DD'), NULL, 7, 'DNI', '28456789')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1005, '20-24567890-7', TO_DATE('2014-09-01', 'YYYY-MM-DD'), NULL, 5, 'DNI', '24567890')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1006, '27-29234567-8', TO_DATE('2019-01-15', 'YYYY-MM-DD'), NULL, 8, 'DNI', '29234567')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1007, '20-27890123-9', TO_DATE('2016-11-10', 'YYYY-MM-DD'), NULL, 6, 'DNI', '27890123')
    INTO MEDICO (matricula, cuil_cuit, fecha_ingreso, foto, max_cant_guardia, tipo_documento, nro_documento) VALUES (1008, '27-26789012-0', TO_DATE('2020-03-05', 'YYYY-MM-DD'), NULL, 7, 'DNI', '26789012')
SELECT 1 FROM dual;

-- ==============================================
-- 6. SE_ESPECIALIZA_EN
-- ==============================================
INSERT ALL
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1001, 101, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1001, 102, 0)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1002, 201, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1003, 301, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1004, 401, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1005, 501, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1006, 601, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1006, 602, 0)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1007, 701, 1)
    INTO SE_ESPECIALIZA_EN (matricula, cod_especialidad, hace_guardia) VALUES (1008, 101, 1)
SELECT 1 FROM dual;

-- ==============================================
-- 7. HABITACION
-- ==============================================
INSERT ALL
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (101, 1, 'NORTE', 1)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (102, 1, 'SUR', 1)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (103, 1, 'ESTE', 1)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (201, 2, 'NORTE', 2)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (202, 2, 'SUR', 2)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (203, 2, 'OESTE', 2)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (301, 3, 'ESTE', 3)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (302, 3, 'OESTE', 3)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (401, 4, 'NORTE', 4)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (402, 4, 'SUR', 4)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (501, 5, 'NORTE', 5)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (502, 5, 'SUR', 5)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (601, 6, 'ESTE', 6)
    INTO HABITACION (nro_habitacion, piso, orientacion, id_sector) VALUES (701, 7, 'OESTE', 7)
SELECT 1 FROM dual;

-- ==============================================
-- 8. CAMA
-- ==============================================
INSERT ALL
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 101, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 101, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 102, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 102, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 103, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 201, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 201, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 202, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 202, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 203, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 301, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 302, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 401, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 401, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 402, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 501, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 502, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 601, 'OCUPADA')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (2, 601, 'LIBRE')
    INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (1, 701, 'LIBRE')
SELECT 1 FROM dual;

-- ==============================================
-- 9. TURNO
-- ==============================================
INSERT ALL
    INTO TURNO (id_turno, horario) VALUES (1, 'Manana (07:00 - 13:00)')
    INTO TURNO (id_turno, horario) VALUES (2, 'Tarde (13:00 - 19:00)')
    INTO TURNO (id_turno, horario) VALUES (3, 'Noche (19:00 - 07:00)')
SELECT 1 FROM dual;

-- ==============================================
-- 10. ATIENDE (Especialidades que atienden por turno)
-- ==============================================
INSERT ALL
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (101, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (101, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (101, 3)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (201, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (201, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (201, 3)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (301, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (301, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (301, 3)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (401, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (401, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (501, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (501, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (501, 3)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (601, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (601, 2)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (701, 1)
    INTO ATIENDE (cod_especialidad, id_turno) VALUES (701, 2)
SELECT 1 FROM dual;

-- ==============================================
-- 11. RONDA
-- ==============================================
INSERT ALL
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (1, 'LUNES', 'Manana')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (2, 'LUNES', 'Tarde')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (3, 'MARTES', 'Manana')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (4, 'MARTES', 'Tarde')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (5, 'MIERCOLES', 'Manana')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (6, 'MIERCOLES', 'Tarde')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (7, 'JUEVES', 'Manana')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (8, 'JUEVES', 'Tarde')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (9, 'VIERNES', 'Manana')
    INTO RONDA (id_ronda, dia_semana, turno) VALUES (10, 'VIERNES', 'Tarde')
SELECT 1 FROM dual;

-- ==============================================
-- 12. VISITA (Rondas visitan habitaciones)
-- ==============================================
INSERT ALL
    INTO VISITA (id_ronda, nro_habitacion) VALUES (1, 101)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (1, 102)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (1, 103)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (1, 201)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (1, 202)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (2, 301)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (2, 302)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (2, 401)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (2, 402)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (3, 501)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (3, 502)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (3, 601)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (4, 701)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (4, 101)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (4, 102)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (5, 201)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (5, 202)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (5, 203)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (6, 301)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (6, 302)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (7, 401)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (7, 402)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (8, 501)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (8, 502)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (9, 601)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (9, 701)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (10, 101)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (10, 102)
    INTO VISITA (id_ronda, nro_habitacion) VALUES (10, 103)
SELECT 1 FROM dual;

-- ==============================================
-- 13. INTERNACION
-- ==============================================
INSERT ALL
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (1, TO_DATE('2025-11-01', 'YYYY-MM-DD'), NULL, 'DNI', '32456789', 1001)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (2, TO_DATE('2025-11-03', 'YYYY-MM-DD'), NULL, 'DNI', '28765432', 1002)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (3, TO_DATE('2025-11-05', 'YYYY-MM-DD'), NULL, 'DNI', '35678901', 1002)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (4, TO_DATE('2025-11-04', 'YYYY-MM-DD'), NULL, 'DNI', '42567890', 1004)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (5, TO_DATE('2025-11-06', 'YYYY-MM-DD'), NULL, 'DNI', '29876543', 1005)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (6, TO_DATE('2025-11-07', 'YYYY-MM-DD'), NULL, 'DNI', '36789012', 1006)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (7, TO_DATE('2025-10-15', 'YYYY-MM-DD'), TO_DATE('2025-10-25', 'YYYY-MM-DD'), 'DNI', '41234567', 1003)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (8, TO_DATE('2025-10-10', 'YYYY-MM-DD'), TO_DATE('2025-10-20', 'YYYY-MM-DD'), 'DNI', '38901234', 1001)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (9, TO_DATE('2025-10-01', 'YYYY-MM-DD'), TO_DATE('2025-10-12', 'YYYY-MM-DD'), 'DNI', '31234567', 1007)
    INTO INTERNACION (nro_internacion, fecha_inicio, fecha_fin, tipo_documento, nro_documento, matricula) VALUES (10, TO_DATE('2025-09-20', 'YYYY-MM-DD'), TO_DATE('2025-10-05', 'YYYY-MM-DD'), 'DNI', '40123456', 1008)
SELECT 1 FROM dual;

-- ==============================================
-- 14. SE_UBICA (Pacientes ubicados en camas)
-- ==============================================
INSERT ALL
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (1, TO_TIMESTAMP('2025-11-01 08:30:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 101)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (2, TO_TIMESTAMP('2025-11-03 14:15:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 201)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (3, TO_TIMESTAMP('2025-11-05 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), 2, 201)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (4, TO_TIMESTAMP('2025-11-04 10:45:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 401)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (5, TO_TIMESTAMP('2025-11-06 16:20:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 501)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (6, TO_TIMESTAMP('2025-11-07 11:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 601)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (7, TO_TIMESTAMP('2025-10-15 13:30:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 302)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (8, TO_TIMESTAMP('2025-10-10 09:15:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 102)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (9, TO_TIMESTAMP('2025-10-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 203)
    INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (10, TO_TIMESTAMP('2025-09-20 15:45:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 102)
SELECT 1 FROM dual;

-- ==============================================
-- 15. RECORRIDO (Medicos realizan rondas)
-- ==============================================
INSERT ALL
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (1, TO_DATE('2025-11-04', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-04 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-04 10:30:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 1001)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (2, TO_DATE('2025-11-04', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-04 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-04 16:45:00', 'YYYY-MM-DD HH24:MI:SS'), 2, 1003)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (3, TO_DATE('2025-11-05', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-05 08:15:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-05 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), 3, 1005)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (4, TO_DATE('2025-11-05', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-05 14:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-05 16:15:00', 'YYYY-MM-DD HH24:MI:SS'), 4, 1007)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (5, TO_DATE('2025-11-06', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-06 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-06 10:45:00', 'YYYY-MM-DD HH24:MI:SS'), 5, 1002)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (6, TO_DATE('2025-11-06', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-06 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-06 15:30:00', 'YYYY-MM-DD HH24:MI:SS'), 6, 1003)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (7, TO_DATE('2025-11-07', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-07 08:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-07 10:15:00', 'YYYY-MM-DD HH24:MI:SS'), 7, 1004)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (8, TO_DATE('2025-11-07', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-07 14:15:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-07 16:00:00', 'YYYY-MM-DD HH24:MI:SS'), 8, 1005)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (9, TO_DATE('2025-11-08', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-08 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-08 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), 9, 1006)
    INTO RECORRIDO (id_recorrido, fecha_recorrido, hora_inicio, hora_fin, id_ronda, matricula) VALUES (10, TO_DATE('2025-11-08', 'YYYY-MM-DD'), TO_TIMESTAMP('2025-11-08 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2025-11-08 16:30:00', 'YYYY-MM-DD HH24:MI:SS'), 10, 1001)
SELECT 1 FROM dual;

-- ==============================================
-- 16. COMENTA_SOBRE (Comentarios de recorridos)
-- ==============================================
INSERT ALL
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (1, 1, 'Paciente estable. Presion arterial controlada. Continuar con medicacion actual. Evolucion favorable.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (3, 5, 'Paciente en terapia intensiva. Signos vitales monitoreados constantemente. Requiere observacion continua durante las proximas 48 horas.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (5, 2, 'Fractura en proceso de recuperacion. Dolor controlado con analgesicos. Iniciar fisioterapia en 3 dias.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (5, 3, 'Rehabilitacion post-quirurgica en curso. Paciente responde bien al tratamiento. Considerar alta en 5 dias si continua evolucion positiva.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (7, 4, 'Post-operatorio normal. Herida quirurgica limpia, sin signos de infeccion. Paciente tolera alimentacion oral. Pronostico favorable.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (8, 5, 'Mejoria significativa. Parametros vitales dentro de rangos normales. Evaluar posibilidad de traslado a sala general.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (9, 6, 'Paciente neurologico estable. Estudios complementarios programados para manana. Mantener tratamiento neuroprotector actual.')
    INTO COMENTA_SOBRE (id_recorrido, nro_internacion, comentario) VALUES (10, 1, 'Control cardiologico semanal. Electrocardiograma sin alteraciones. Paciente reporta sentirse mejor. Ajustar dosis de medicacion anticoagulante.')
SELECT 1 FROM dual;

-- ==============================================
-- 17. GUARDIA (Medicos realizan guardias)
-- ==============================================
INSERT ALL
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (1, TO_TIMESTAMP('2025-11-01 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1001, 101, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (2, TO_TIMESTAMP('2025-11-01 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1008, 101, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (3, TO_TIMESTAMP('2025-11-01 19:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1001, 101, 3)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (4, TO_TIMESTAMP('2025-11-02 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1002, 201, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (5, TO_TIMESTAMP('2025-11-02 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1002, 201, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (6, TO_TIMESTAMP('2025-11-02 19:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1002, 201, 3)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (7, TO_TIMESTAMP('2025-11-03 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1003, 301, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (8, TO_TIMESTAMP('2025-11-03 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1003, 301, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (9, TO_TIMESTAMP('2025-11-03 19:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1003, 301, 3)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (10, TO_TIMESTAMP('2025-11-04 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1004, 401, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (11, TO_TIMESTAMP('2025-11-04 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1004, 401, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (12, TO_TIMESTAMP('2025-11-05 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1005, 501, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (13, TO_TIMESTAMP('2025-11-05 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1005, 501, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (14, TO_TIMESTAMP('2025-11-05 19:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1005, 501, 3)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (15, TO_TIMESTAMP('2025-11-06 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1006, 601, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (16, TO_TIMESTAMP('2025-11-06 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1006, 601, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (17, TO_TIMESTAMP('2025-11-07 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1007, 701, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (18, TO_TIMESTAMP('2025-11-07 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1007, 701, 2)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (19, TO_TIMESTAMP('2025-11-08 07:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1008, 101, 1)
    INTO GUARDIA (nro_guardia, fecha_hora, matricula, cod_especialidad, id_turno) VALUES (20, TO_TIMESTAMP('2025-11-08 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), 1001, 101, 2)
SELECT 1 FROM dual;

-- ==============================================
-- 18. VACACIONES (Vacaciones de los medicos)
-- ==============================================
INSERT ALL
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1001, TO_DATE('2025-01-01', 'YYYY-MM-DD'), TO_DATE('2025-01-31', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1002, TO_DATE('2025-02-01', 'YYYY-MM-DD'), TO_DATE('2025-02-28', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1003, TO_DATE('2025-03-01', 'YYYY-MM-DD'), TO_DATE('2025-03-31', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1004, TO_DATE('2025-04-01', 'YYYY-MM-DD'), TO_DATE('2025-04-30', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1005, TO_DATE('2025-05-01', 'YYYY-MM-DD'), TO_DATE('2025-05-31', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1006, TO_DATE('2025-06-01', 'YYYY-MM-DD'), TO_DATE('2025-06-30', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1007, TO_DATE('2025-07-01', 'YYYY-MM-DD'), TO_DATE('2025-07-31', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1008, TO_DATE('2025-08-01', 'YYYY-MM-DD'), TO_DATE('2025-08-31', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1001, TO_DATE('2025-01-15', 'YYYY-MM-DD'), TO_DATE('2025-02-14', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1002, TO_DATE('2025-02-10', 'YYYY-MM-DD'), TO_DATE('2025-03-10', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1003, TO_DATE('2025-03-05', 'YYYY-MM-DD'), TO_DATE('2025-04-04', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1004, TO_DATE('2025-04-20', 'YYYY-MM-DD'), TO_DATE('2025-05-19', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1005, TO_DATE('2025-05-15', 'YYYY-MM-DD'), TO_DATE('2025-06-14', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1006, TO_DATE('2025-06-10', 'YYYY-MM-DD'), TO_DATE('2025-07-09', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1007, TO_DATE('2025-07-25', 'YYYY-MM-DD'), TO_DATE('2025-08-24', 'YYYY-MM-DD'))
    INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (1008, TO_DATE('2025-08-20', 'YYYY-MM-DD'), TO_DATE('2025-09-19', 'YYYY-MM-DD'))
SELECT 1 FROM dual;

-- ==============================================
-- COMMIT de todos los cambios
-- ==============================================
COMMIT;
