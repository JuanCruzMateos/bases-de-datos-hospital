-- Switch to the default Pluggable Database (PDB)
ALTER SESSION SET CONTAINER = FREEPDB1;

-- Create the user
CREATE USER hospital IDENTIFIED BY hospital123;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO hospital;
GRANT UNLIMITED TABLESPACE TO hospital;

-- Optional: Grant privileges to create views, procedures, etc.
GRANT CREATE VIEW, CREATE PROCEDURE, CREATE SEQUENCE TO hospital;

EXIT;
