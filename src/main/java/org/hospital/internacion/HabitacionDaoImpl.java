package org.hospital.internacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

public class HabitacionDaoImpl implements HabitacionDao {
    private static final Logger logger = Logger.getLogger(HabitacionDaoImpl.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO HABITACION (piso, orientacion, id_sector) VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT nro_habitacion, piso, orientacion, id_sector " +
            "FROM HABITACION " +
            "WHERE nro_habitacion = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT nro_habitacion, piso, orientacion, id_sector " +
            "FROM HABITACION " +
            "ORDER BY piso, nro_habitacion";

    private static final String SELECT_BY_SECTOR_SQL =
            "SELECT nro_habitacion, piso, orientacion, id_sector " +
            "FROM HABITACION " +
            "WHERE id_sector = ? " +
            "ORDER BY piso, nro_habitacion";

    private static final String UPDATE_SQL =
            "UPDATE HABITACION " +
            "SET piso = ?, orientacion = ?, id_sector = ? " +
            "WHERE nro_habitacion = ?";

    private static final String DELETE_SQL =
            "DELETE FROM HABITACION WHERE nro_habitacion = ?";


    @Override
    public Habitacion create(Habitacion habitacion) throws DataAccessException {
        logger.info("Creating habitacion: piso=" + habitacion.getPiso() + ", sector=" + habitacion.getIdSector());
        validateHabitacion(habitacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, 
                    new String[]{"nro_habitacion"})) {
                statement.setInt(1, habitacion.getPiso());
                statement.setString(2, habitacion.getOrientacion());
                statement.setInt(3, habitacion.getIdSector());
                statement.executeUpdate();
                
                // Get generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        habitacion.setNroHabitacion(generatedKeys.getInt(1));
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully created habitacion: " + habitacion.getNroHabitacion());
            return habitacion;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for habitacion creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create habitacion: " + e.getMessage());
            throw new DataAccessException("Error creating habitacion", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<Habitacion> findById(int nroHabitacion) throws DataAccessException {
        logger.fine("Finding habitacion by id: " + nroHabitacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setInt(1, nroHabitacion);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToHabitacion(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find habitacion: " + e.getMessage());
            throw new DataAccessException("Error finding habitacion", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Habitacion> findAll() throws DataAccessException {
        logger.fine("Finding all habitaciones");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Habitacion> habitaciones = new ArrayList<>();
                while (resultSet.next()) {
                    habitaciones.add(mapToHabitacion(resultSet));
                }
                logger.fine("Found " + habitaciones.size() + " habitaciones");
                return habitaciones;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find habitaciones: " + e.getMessage());
            throw new DataAccessException("Error finding habitaciones", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Habitacion> findBySector(int idSector) throws DataAccessException {
        logger.fine("Finding habitaciones by sector: " + idSector);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_SECTOR_SQL)) {
                statement.setInt(1, idSector);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Habitacion> habitaciones = new ArrayList<>();
                    while (resultSet.next()) {
                        habitaciones.add(mapToHabitacion(resultSet));
                    }
                    return habitaciones;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find habitaciones by sector: " + e.getMessage());
            throw new DataAccessException("Error finding habitaciones by sector", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Habitacion update(Habitacion habitacion) throws DataAccessException {
        logger.info("Updating habitacion: " + habitacion.getNroHabitacion());
        validateHabitacion(habitacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setInt(1, habitacion.getPiso());
                statement.setString(2, habitacion.getOrientacion());
                statement.setInt(3, habitacion.getIdSector());
                statement.setInt(4, habitacion.getNroHabitacion());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Habitacion not found with nro: " + habitacion.getNroHabitacion());
                }
            }
            
            connection.commit();
            logger.info("Successfully updated habitacion: " + habitacion.getNroHabitacion());
            return habitacion;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for habitacion update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update habitacion: " + e.getMessage());
            throw new DataAccessException("Error updating habitacion", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean delete(int nroHabitacion) throws DataAccessException {
        logger.info("Deleting habitacion: " + nroHabitacion);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setInt(1, nroHabitacion);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    logger.info("Successfully deleted habitacion: " + nroHabitacion);
                    return true;
                } else {
                    connection.rollback();
                    logger.warning("Habitacion not found for deletion: " + nroHabitacion);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for habitacion deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete habitacion: " + e.getMessage());
            throw new DataAccessException("Error deleting habitacion", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warning("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    private Habitacion mapToHabitacion(ResultSet resultSet) throws SQLException {
        return new Habitacion(
                resultSet.getInt("nro_habitacion"),
                resultSet.getInt("piso"),
                resultSet.getString("orientacion"),
                resultSet.getInt("id_sector")
        );
    }

    private void validateHabitacion(Habitacion habitacion) {
        if (habitacion == null) {
            throw new IllegalArgumentException("habitacion must not be null");
        }
        if (habitacion.getPiso() < 0) {
            throw new IllegalArgumentException("piso must be >= 0");
        }
        if (habitacion.getOrientacion() == null || habitacion.getOrientacion().trim().isEmpty()) {
            throw new IllegalArgumentException("orientacion must not be null or empty");
        }
    }
}
