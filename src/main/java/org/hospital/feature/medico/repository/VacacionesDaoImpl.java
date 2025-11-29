package org.hospital.feature.medico.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.medico.domain.Vacaciones;

public class VacacionesDaoImpl implements VacacionesDao {
    private static final Logger logger = Logger.getLogger(VacacionesDaoImpl.class.getName());
    
    private static final String INSERT_SQL =
            "INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin) VALUES (?, ?, ?)";
    private static final String SELECT_BY_MATRICULA_SQL =
            "SELECT matricula, fecha_inicio, fecha_fin FROM VACACIONES WHERE matricula = ? ORDER BY fecha_inicio";
    private static final String SELECT_ALL_SQL =
            "SELECT matricula, fecha_inicio, fecha_fin FROM VACACIONES ORDER BY matricula, fecha_inicio";
    private static final String CHECK_ON_VACATION_SQL =
            "SELECT COUNT(*) FROM VACACIONES WHERE matricula = ? AND ? BETWEEN fecha_inicio AND fecha_fin";
    private static final String DELETE_SQL =
            "DELETE FROM VACACIONES WHERE matricula = ? AND fecha_inicio = ? AND fecha_fin = ?";
    private static final String DELETE_BY_MATRICULA_SQL =
            "DELETE FROM VACACIONES WHERE matricula = ?";

    @Override
    public Vacaciones create(Vacaciones vacaciones) throws DataAccessException {
        logger.info("Creating vacaciones for medico: " + vacaciones.getMatricula());
        validateVacaciones(vacaciones);
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
                statement.setLong(1, vacaciones.getMatricula());
                statement.setDate(2, Date.valueOf(vacaciones.getFechaInicio()));
                statement.setDate(3, Date.valueOf(vacaciones.getFechaFin()));
                statement.executeUpdate();
            }
            logger.info("Successfully created vacaciones for medico: " + vacaciones.getMatricula());
            return vacaciones;
        } catch (SQLException e) {
            logger.severe("Failed to create vacaciones: " + e.getMessage());
            throw new DataAccessException("Error creating vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<Vacaciones> findByMatricula(long matricula) throws DataAccessException {
        logger.fine("Finding vacaciones for medico: " + matricula);
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_MATRICULA_SQL)) {
                statement.setLong(1, matricula);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Vacaciones> vacacionesList = new ArrayList<>();
                    while (resultSet.next()) {
                        vacacionesList.add(mapToVacaciones(resultSet));
                    }
                    logger.fine("Found " + vacacionesList.size() + " vacation periods for medico " + matricula);
                    return vacacionesList;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find vacaciones: " + e.getMessage());
            throw new DataAccessException("Error finding vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public List<Vacaciones> findAll() throws DataAccessException {
        logger.fine("Finding all vacaciones");
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Vacaciones> vacacionesList = new ArrayList<>();
                while (resultSet.next()) {
                    vacacionesList.add(mapToVacaciones(resultSet));
                }
                logger.fine("Found " + vacacionesList.size() + " vacation periods");
                return vacacionesList;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find all vacaciones: " + e.getMessage());
            throw new DataAccessException("Error finding all vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean isOnVacation(long matricula, LocalDate date) throws DataAccessException {
        logger.fine("Checking if medico " + matricula + " is on vacation on " + date);
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(CHECK_ON_VACATION_SQL)) {
                statement.setLong(1, matricula);
                statement.setDate(2, Date.valueOf(date));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        boolean onVacation = resultSet.getInt(1) > 0;
                        logger.fine("Medico " + matricula + " is " + (onVacation ? "" : "not ") + "on vacation on " + date);
                        return onVacation;
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to check vacation status: " + e.getMessage());
            throw new DataAccessException("Error checking vacation status", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean delete(long matricula, LocalDate fechaInicio, LocalDate fechaFin) throws DataAccessException {
        logger.info("Deleting vacaciones for medico " + matricula + " from " + fechaInicio + " to " + fechaFin);
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setLong(1, matricula);
                statement.setDate(2, Date.valueOf(fechaInicio));
                statement.setDate(3, Date.valueOf(fechaFin));
                int rowsDeleted = statement.executeUpdate();
                boolean deleted = rowsDeleted > 0;
                if (deleted) {
                    logger.info("Successfully deleted vacaciones");
                } else {
                    logger.warning("Vacaciones not found for deletion");
                }
                return deleted;
            }
        } catch (SQLException e) {
            logger.severe("Failed to delete vacaciones: " + e.getMessage());
            throw new DataAccessException("Error deleting vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public boolean deleteByMatricula(long matricula) throws DataAccessException {
        logger.info("Deleting all vacaciones for medico: " + matricula);
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_MATRICULA_SQL)) {
                statement.setLong(1, matricula);
                int rowsDeleted = statement.executeUpdate();
                boolean deleted = rowsDeleted > 0;
                if (deleted) {
                    logger.info("Successfully deleted " + rowsDeleted + " vacation periods");
                } else {
                    logger.fine("No vacation periods found for medico " + matricula);
                }
                return deleted;
            }
        } catch (SQLException e) {
            logger.severe("Failed to delete vacaciones by matricula: " + e.getMessage());
            throw new DataAccessException("Error deleting vacaciones by matricula", e);
        } finally {
            closeConnection(connection);
        }
    }

    private Vacaciones mapToVacaciones(ResultSet resultSet) throws SQLException {
        return new Vacaciones(
                resultSet.getLong("matricula"),
                resultSet.getDate("fecha_inicio").toLocalDate(),
                resultSet.getDate("fecha_fin").toLocalDate()
        );
    }

    private void validateVacaciones(Vacaciones vacaciones) {
        if (vacaciones == null) {
            throw new IllegalArgumentException("Vacaciones must not be null");
        }
        if (vacaciones.getFechaInicio() == null) {
            throw new IllegalArgumentException("Fecha inicio must not be null");
        }
        if (vacaciones.getFechaFin() == null) {
            throw new IllegalArgumentException("Fecha fin must not be null");
        }
        if (vacaciones.getFechaInicio().isAfter(vacaciones.getFechaFin())) {
            throw new IllegalArgumentException("Fecha inicio must be before or equal to fecha fin");
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warning("Failed to close connection: " + e.getMessage());
            }
        }
    }
    
    @Override
    public Vacaciones createWithTransaction(Vacaciones vacaciones) throws DataAccessException {
        logger.info("Creating vacaciones with transaction for medico: " + vacaciones.getMatricula());
        validateVacaciones(vacaciones);
        
        // PL/SQL block that mirrors the logic in db_scripts/transactions/vacaciones.sql
        String plsqlBlock = 
            "DECLARE\n" +
            "    v_matricula         NUMBER := ?;\n" +
            "    v_fecha_inicio      DATE := ?;\n" +
            "    v_fecha_fin         DATE := ?;\n" +
            "    \n" +
            "    v_guardias_conflicto NUMBER;\n" +
            "    v_vacaciones_solapadas NUMBER;\n" +
            "    v_medico_existe      NUMBER;\n" +
            "BEGIN\n" +
            "    SAVEPOINT inicio_transaccion;\n" +
            "    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;\n" +
            "    \n" +
            "    -- Lock GUARDIA : el resto puede leer pero no modificar\n" +
            "    LOCK TABLE GUARDIA IN SHARE ROW EXCLUSIVE MODE;\n" +
            "\n" +
            "    -- 1. Validate que el rango de fechas es valido\n" +
            "    IF v_fecha_inicio > v_fecha_fin THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20099,\n" +
            "            'Error: La fecha de inicio (' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || \n" +
            "            ') debe ser anterior o igual a la fecha de fin (' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    IF v_fecha_inicio IS NULL OR v_fecha_fin IS NULL THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20098,\n" +
            "            'Error: Las fechas de inicio y fin no pueden ser nulas.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "\n" +
            "    -- 2. Validate que el medico existe\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_medico_existe\n" +
            "    FROM MEDICO\n" +
            "    WHERE matricula = v_matricula;\n" +
            "    \n" +
            "    IF v_medico_existe = 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20100,\n" +
            "            'Error: El medico con matricula ' || v_matricula || ' no existe.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 3. Check para prevenir que un medico tenga vacaciones que se solapen\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_vacaciones_solapadas\n" +
            "    FROM VACACIONES\n" +
            "    WHERE matricula = v_matricula\n" +
            "        AND fecha_inicio < v_fecha_fin\n" +
            "        AND fecha_fin > v_fecha_inicio;\n" +
            "    \n" +
            "    IF v_vacaciones_solapadas > 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20101,\n" +
            "            'Error: El medico ya tiene vacaciones que se solapan con el periodo solicitado ' ||\n" +
            "            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 4. Check para prevenir que un medico esté en guardia durante su periodo de vacaciones\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_guardias_conflicto\n" +
            "    FROM GUARDIA\n" +
            "    WHERE matricula = v_matricula\n" +
            "        AND TRUNC(fecha_hora) BETWEEN v_fecha_inicio AND v_fecha_fin;\n" +
            "    \n" +
            "    IF v_guardias_conflicto > 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20102,\n" +
            "            'Error: El medico tiene ' || v_guardias_conflicto || ' guardia(s) programada(s) durante el periodo de vacaciones ' ||\n" +
            "            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || '). ' ||\n" +
            "            'Debe reasignar o cancelar las guardias antes de solicitar vacaciones.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 5. Insert the vacation\n" +
            "    INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin)\n" +
            "    VALUES (v_matricula, v_fecha_inicio, v_fecha_fin);\n" +
            "    \n" +
            "    COMMIT;\n" +
            "EXCEPTION\n" +
            "    WHEN OTHERS THEN\n" +
            "        ROLLBACK TO inicio_transaccion;\n" +
            "        RAISE;\n" +
            "END;";
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(plsqlBlock)) {
                stmt.setLong(1, vacaciones.getMatricula());
                stmt.setDate(2, Date.valueOf(vacaciones.getFechaInicio()));
                stmt.setDate(3, Date.valueOf(vacaciones.getFechaFin()));
                stmt.execute();
            }
            
            logger.info("Successfully created vacaciones with transaction for medico: " + vacaciones.getMatricula());
            return vacaciones;
            
        } catch (SQLException e) {
            // Check for serialization conflict (ORA-08177)
            if (e.getErrorCode() == 8177) {
                throw new DataAccessException(
                    "Conflicto de serializacion detectado. Otra transaccion modifico los datos concurrentemente. " +
                    "Se recomienda reintentar la operacion.", e
                );
            }
            
            logger.severe("Failed to create vacaciones with transaction: " + e.getMessage());
            throw new DataAccessException("Error creating vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    @Override
    public Vacaciones updateWithTransaction(Vacaciones oldVacaciones, Vacaciones newVacaciones) throws DataAccessException {
        logger.info("Updating vacaciones with transaction for medico: " + newVacaciones.getMatricula());
        validateVacaciones(newVacaciones);
        
        // PL/SQL block for update: delete old + insert new with validation
        String plsqlBlock = 
            "DECLARE\n" +
            "    v_old_matricula     NUMBER := ?;\n" +
            "    v_old_fecha_inicio  DATE := ?;\n" +
            "    v_old_fecha_fin     DATE := ?;\n" +
            "    v_matricula         NUMBER := ?;\n" +
            "    v_fecha_inicio      DATE := ?;\n" +
            "    v_fecha_fin         DATE := ?;\n" +
            "    \n" +
            "    v_guardias_conflicto NUMBER;\n" +
            "    v_vacaciones_solapadas NUMBER;\n" +
            "    v_medico_existe      NUMBER;\n" +
            "    v_deleted            NUMBER;\n" +
            "BEGIN\n" +
            "    SAVEPOINT inicio_transaccion;\n" +
            "    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;\n" +
            "    \n" +
            "    -- Lock GUARDIA : el resto puede leer pero no modificar\n" +
            "    LOCK TABLE GUARDIA IN SHARE ROW EXCLUSIVE MODE;\n" +
            "\n" +
            "    -- 1. Delete old vacation\n" +
            "    DELETE FROM VACACIONES \n" +
            "    WHERE matricula = v_old_matricula \n" +
            "        AND fecha_inicio = v_old_fecha_inicio \n" +
            "        AND fecha_fin = v_old_fecha_fin;\n" +
            "    \n" +
            "    v_deleted := SQL%ROWCOUNT;\n" +
            "    IF v_deleted = 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(-20103, 'Error: La vacacion original no existe.');\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 2. Validate que el rango de fechas es valido\n" +
            "    IF v_fecha_inicio > v_fecha_fin THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20099,\n" +
            "            'Error: La fecha de inicio (' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || \n" +
            "            ') debe ser anterior o igual a la fecha de fin (' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    IF v_fecha_inicio IS NULL OR v_fecha_fin IS NULL THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20098,\n" +
            "            'Error: Las fechas de inicio y fin no pueden ser nulas.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "\n" +
            "    -- 3. Validate que el medico existe\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_medico_existe\n" +
            "    FROM MEDICO\n" +
            "    WHERE matricula = v_matricula;\n" +
            "    \n" +
            "    IF v_medico_existe = 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20100,\n" +
            "            'Error: El medico con matricula ' || v_matricula || ' no existe.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 4. Check para prevenir que un medico tenga vacaciones que se solapen\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_vacaciones_solapadas\n" +
            "    FROM VACACIONES\n" +
            "    WHERE matricula = v_matricula\n" +
            "        AND fecha_inicio < v_fecha_fin\n" +
            "        AND fecha_fin > v_fecha_inicio;\n" +
            "    \n" +
            "    IF v_vacaciones_solapadas > 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20101,\n" +
            "            'Error: El medico ya tiene vacaciones que se solapan con el periodo solicitado ' ||\n" +
            "            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || ').'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 5. Check para prevenir que un medico esté en guardia durante su periodo de vacaciones\n" +
            "    SELECT COUNT(*)\n" +
            "    INTO v_guardias_conflicto\n" +
            "    FROM GUARDIA\n" +
            "    WHERE matricula = v_matricula\n" +
            "        AND TRUNC(fecha_hora) BETWEEN v_fecha_inicio AND v_fecha_fin;\n" +
            "    \n" +
            "    IF v_guardias_conflicto > 0 THEN\n" +
            "        RAISE_APPLICATION_ERROR(\n" +
            "            -20102,\n" +
            "            'Error: El medico tiene ' || v_guardias_conflicto || ' guardia(s) programada(s) durante el periodo de vacaciones ' ||\n" +
            "            '(' || TO_CHAR(v_fecha_inicio, 'YYYY-MM-DD') || ' - ' || TO_CHAR(v_fecha_fin, 'YYYY-MM-DD') || '). ' ||\n" +
            "            'Debe reasignar o cancelar las guardias antes de solicitar vacaciones.'\n" +
            "        );\n" +
            "    END IF;\n" +
            "    \n" +
            "    -- 6. Insert the new vacation\n" +
            "    INSERT INTO VACACIONES (matricula, fecha_inicio, fecha_fin)\n" +
            "    VALUES (v_matricula, v_fecha_inicio, v_fecha_fin);\n" +
            "    \n" +
            "    COMMIT;\n" +
            "EXCEPTION\n" +
            "    WHEN OTHERS THEN\n" +
            "        ROLLBACK TO inicio_transaccion;\n" +
            "        RAISE;\n" +
            "END;";
        
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(plsqlBlock)) {
                // Old vacation parameters
                stmt.setLong(1, oldVacaciones.getMatricula());
                stmt.setDate(2, Date.valueOf(oldVacaciones.getFechaInicio()));
                stmt.setDate(3, Date.valueOf(oldVacaciones.getFechaFin()));
                // New vacation parameters
                stmt.setLong(4, newVacaciones.getMatricula());
                stmt.setDate(5, Date.valueOf(newVacaciones.getFechaInicio()));
                stmt.setDate(6, Date.valueOf(newVacaciones.getFechaFin()));
                stmt.execute();
            }
            
            logger.info("Successfully updated vacaciones with transaction");
            return newVacaciones;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 8177) {
                throw new DataAccessException(
                    "Conflicto de serializacion detectado. Se recomienda reintentar la operacion.", e
                );
            }
            
            logger.severe("Failed to update vacaciones with transaction: " + e.getMessage());
            throw new DataAccessException("Error updating vacaciones", e);
        } finally {
            closeConnection(connection);
        }
    }
}

