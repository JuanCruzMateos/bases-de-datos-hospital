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

public class SectorDaoImpl implements SectorDao {
    private static final Logger logger = Logger.getLogger(SectorDaoImpl.class.getName());
    private static final String INSERT_SQL = 
            "INSERT INTO SECTOR (descripcion) VALUES (?)";
    private static final String SELECT_BY_ID_SQL = 
            "SELECT id_sector, descripcion FROM SECTOR WHERE id_sector = ?";
    private static final String SELECT_ALL_SQL = 
            "SELECT id_sector, descripcion FROM SECTOR ORDER BY descripcion";
    private static final String UPDATE_SQL = 
            "UPDATE SECTOR SET descripcion = ? WHERE id_sector = ?";
    private static final String DELETE_SQL = 
            "DELETE FROM SECTOR WHERE id_sector = ?";

    @Override
    public Sector create(Sector sector) throws DataAccessException {
        logger.info("Creating sector: " + sector.getDescripcion());
        validateSector(sector);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, 
                    new String[]{"id_sector"})) {
                statement.setString(1, sector.getDescripcion());
                statement.executeUpdate();
                
                // Get generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sector.setIdSector(generatedKeys.getInt(1));
                    }
                }
            }
            
            connection.commit();
            logger.info("Successfully created sector: " + sector.getIdSector());
            return sector;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for sector creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create sector: " + e.getMessage());
            throw new DataAccessException("Error creating sector", e);
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
    public Optional<Sector> findById(int idSector) throws DataAccessException {
        logger.fine("Finding sector by id: " + idSector);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                statement.setInt(1, idSector);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapToSector(resultSet));
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to find sector: " + e.getMessage());
            throw new DataAccessException("Error finding sector", e);
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
    public List<Sector> findAll() throws DataAccessException {
        logger.fine("Finding all sectores");
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Sector> sectores = new ArrayList<>();
                while (resultSet.next()) {
                    sectores.add(mapToSector(resultSet));
                }
                logger.fine("Found " + sectores.size() + " sectores");
                return sectores;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find sectores: " + e.getMessage());
            throw new DataAccessException("Error finding sectores", e);
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
    public Sector update(Sector sector) throws DataAccessException {
        logger.info("Updating sector: " + sector.getIdSector());
        validateSector(sector);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
                statement.setString(1, sector.getDescripcion());
                statement.setInt(2, sector.getIdSector());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Sector not found with id: " + sector.getIdSector());
                }
            }
            
            connection.commit();
            logger.info("Successfully updated sector: " + sector.getIdSector());
            return sector;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for sector update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update sector: " + e.getMessage());
            throw new DataAccessException("Error updating sector", e);
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
    public boolean delete(int idSector) throws DataAccessException {
        logger.info("Deleting sector: " + idSector);
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
                statement.setInt(1, idSector);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    logger.info("Successfully deleted sector: " + idSector);
                    return true;
                } else {
                    connection.rollback();
                    logger.warning("Sector not found for deletion: " + idSector);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for sector deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete sector: " + e.getMessage());
            throw new DataAccessException("Error deleting sector", e);
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

    private Sector mapToSector(ResultSet resultSet) throws SQLException {
        return new Sector(
                resultSet.getInt("id_sector"),
                resultSet.getString("descripcion")
        );
    }

    private void validateSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException("sector must not be null");
        }
        if (sector.getDescripcion() == null || sector.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("descripcion must not be null or empty");
        }
    }
}
