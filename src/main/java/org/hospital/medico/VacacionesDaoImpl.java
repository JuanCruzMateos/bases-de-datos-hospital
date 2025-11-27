package org.hospital.medico;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

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
}

