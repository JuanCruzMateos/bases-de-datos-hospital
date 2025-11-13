package org.hospital.medico;

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

public class EspecialidadDaoImpl implements EspecialidadDao {
    private static final Logger logger = Logger.getLogger(EspecialidadDaoImpl.class.getName());
    
    private static final String INSERT_SQL = 
            "INSERT INTO ESPECIALIDAD (descripcion, id_sector) VALUES (?, ?)";
    private static final String SELECT_BY_ID_SQL = 
            "SELECT cod_especialidad, descripcion, id_sector FROM ESPECIALIDAD WHERE cod_especialidad = ?";
    private static final String SELECT_ALL_SQL = 
            "SELECT cod_especialidad, descripcion, id_sector FROM ESPECIALIDAD ORDER BY descripcion";
    private static final String UPDATE_SQL = 
            "UPDATE ESPECIALIDAD SET descripcion = ?, id_sector = ? WHERE cod_especialidad = ?";
    private static final String DELETE_SQL = 
            "DELETE FROM ESPECIALIDAD WHERE cod_especialidad = ?";

    @Override
    public Especialidad create(Especialidad especialidad) throws DataAccessException {
        logger.info("Creating especialidad: " + especialidad.getDescripcion());
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, 
                    new String[]{"cod_especialidad"})) {
                statement.setString(1, especialidad.getDescripcion());
                statement.setInt(2, especialidad.getIdSector());
                statement.executeUpdate();
                
                // Get generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        especialidad.setCodEspecialidad(generatedKeys.getInt(1));
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully created especialidad: " + especialidad.getCodEspecialidad());
            return especialidad;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for especialidad creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create especialidad: " + e.getMessage());
            throw new DataAccessException("Error creating especialidad", e);
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
    public Optional<Especialidad> findByCodEspecialidad(int codEspecialidad) throws DataAccessException {
        logger.fine("Finding especialidad by code: " + codEspecialidad);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setInt(1, codEspecialidad);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToEspecialidad(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find especialidad: " + e.getMessage());
            throw new DataAccessException("Error finding especialidad", e);
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
    public List<Especialidad> findAll() throws DataAccessException {
        logger.fine("Finding all especialidades");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Especialidad> especialidades = new ArrayList<>();
                while (resultSet.next()) {
                    especialidades.add(mapToEspecialidad(resultSet));
                }
                logger.fine("Found " + especialidades.size() + " especialidades");
                return especialidades;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find especialidades: " + e.getMessage());
            throw new DataAccessException("Error finding especialidades", e);
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
    public Especialidad update(Especialidad especialidad) throws DataAccessException {
        logger.info("Updating especialidad: " + especialidad.getCodEspecialidad());
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setString(1, especialidad.getDescripcion());
                statement.setInt(2, especialidad.getIdSector());
                statement.setInt(3, especialidad.getCodEspecialidad());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Especialidad not found with code: " + 
                            especialidad.getCodEspecialidad());
                }
            }
            
            connection.commit();
            logger.info("Successfully updated especialidad: " + especialidad.getCodEspecialidad());
            return especialidad;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for especialidad update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update especialidad: " + e.getMessage());
            throw new DataAccessException("Error updating especialidad", e);
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
    public boolean delete(int codEspecialidad) throws DataAccessException {
        logger.info("Deleting especialidad: " + codEspecialidad);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setInt(1, codEspecialidad);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    logger.info("Successfully deleted especialidad: " + codEspecialidad);
                    return true;
                } else {
                    connection.rollback();
                    logger.warning("Especialidad not found for deletion: " + codEspecialidad);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for especialidad deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete especialidad: " + e.getMessage());
            throw new DataAccessException("Error deleting especialidad", e);
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

    private Especialidad mapToEspecialidad(ResultSet resultSet) throws SQLException {
        return new Especialidad(
                resultSet.getInt("cod_especialidad"),
                resultSet.getString("descripcion"),
                resultSet.getInt("id_sector")
        );
    }
}
