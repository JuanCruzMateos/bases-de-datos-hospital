ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

--  En ORACLE Toda tabla que tenga una constraint PRIMARY KEY, tiene un índice UNICO B-tree (sin repeticiones):

--  Un índice B-tree es la estructura de índice “normal” de casi todos los motores relacionales (incluido Oracle):
--      Guarda los valores ordenados.
--      Internamente es un árbol balanceado (B-tree):
--      La búsqueda, inserción, etc. son O(log n).

-- Sobre las tablas involucradas en las transacciones de guardias y 
-- vacaciones definimos índices NO UNICOS sobre claves foráneas y 
-- columnas muy consultadas para optimizar las consultas típicas.

-- CAMA: filtra por estado (LIBRE/OCUPADA) al asignar o liberar camas
CREATE INDEX IDX_CAMA_ESTADO ON CAMA (estado);

-- CAMA: lista camas por habitacion
CREATE INDEX IDX_CAMA_NRO_HABITACION ON CAMA (nro_habitacion);

-- HABITACION: agrupa habitaciones por sector
CREATE INDEX IDX_HABITACION_ID_SECTOR ON HABITACION (id_sector);

-- SE_UBICA: ubica rapido una cama dentro de una habitacion
CREATE INDEX IDX_SE_UBICA_NRO_HABITACION_NRO_CAMA ON SE_UBICA (nro_habitacion, nro_cama);

-- INTERNACION: consultas por paciente (tipo y numero de documento)
CREATE INDEX IDX_INTERNACION_TDOC_NDOC ON INTERNACION (tipo_documento, nro_documento);

-- COMENTA_SOBRE: recupera comentarios por internacion
CREATE INDEX IDX_COMENTA_SOBRE_NRO_INTERNACION ON COMENTA_SOBRE (nro_internacion);

-- GUARDIA: busca guardias de un medico por fecha
CREATE INDEX IDX_GUARDIA_MATRICULA_FECHA ON GUARDIA (matricula, fecha_hora);

-- AUDITORIA_GUARDIA: consultas de auditoria por usuario y fecha
CREATE INDEX IDX_AUDITORIA_GUARDIA_USUARIO_FECHA ON AUDITORIA_GUARDIA (usuario_bd, fecha_hora_reg);

-- AUDITORIA_GUARDIA: navegar auditorias por guardia afectada
CREATE INDEX IDX_AUDITORIA_GUARDIA_NRO_GUARDIA ON AUDITORIA_GUARDIA (nro_guardia);

-- RECORRIDO: listar recorridos de una ronda
CREATE INDEX IDX_RECORRIDO_ID_RONDA ON RECORRIDO (id_ronda);

-- VISITA: obtener habitaciones asociadas a una ronda
CREATE INDEX IDX_VISITA_ID_RONDA ON VISITA (id_ronda);
