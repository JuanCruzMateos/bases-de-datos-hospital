
-- Este es el archivo maestro que carga todos los procedimientos almacenados y scripts necesarios
-- para reconstruir la base de datos del esquema 'hospital'.
-- docker exec -it oracle-hospital bash 
-- sqlplus hospital/hospital123@FREEPDB1                   Alternativa --sqlplus sys/admin123 as sysdba
-- @/opt/oracle/scripts/startup/10-rebuild-hospital.sql


ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = hospital;

@/opt/oracle/scripts/startup/01-drop-tables.sql
@/opt/oracle/scripts/startup/02-create-tables-pk.sql
@/opt/oracle/scripts/startup/03-define-fk-constrains.sql
@/opt/oracle/scripts/startup/04-init-db.sql
@/opt/oracle/scripts/startup/05-triggers.sql

@/opt/oracle/scripts/procedures/sp_internaciones.sql
@/opt/oracle/scripts/procedures/sp_camas_disponibles.sql
@/opt/oracle/scripts/procedures/sp_comentarios_visitas.sql
@/opt/oracle/scripts/procedures/sp_auditoria_guardias.sql
