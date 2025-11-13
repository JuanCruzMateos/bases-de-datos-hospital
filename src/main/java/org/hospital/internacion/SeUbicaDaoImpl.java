package org.hospital.internacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

/**
 * DAO implementation for SeUbica (Bed Assignment) relationship.
 * Uses manual transaction management (V1 pattern).
 */
public class SeUbicaDaoImpl implements SeUbicaDao {
    private static final Logger logger = Logger.getLogger(SeUbicaDaoImpl.class.getName());

    private static final String INSERT_SQL = 
        "INSERT INTO SE_UBICA (nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_BY_INTERNACION_SQL = 
        "SELECT nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion FROM SE_UBICA WHERE nro_internacion = ? ORDER BY fecha_hora_ingreso";
    
    private static final String SELECT_BY_CAMA_SQL = 
        "SELECT nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion FROM SE_UBICA WHERE nro_cama = ? AND nro_habitacion = ? ORDER BY fecha_hora_ingreso";
    
    private static final String SELECT_BY_HABITACION_SQL = 
        "SELECT nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion FROM SE_UBICA WHERE nro_habitacion = ? ORDER BY fecha_hora_ingreso";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT nro_internacion, fecha_hora_ingreso, nro_cama, nro_habitacion FROM SE_UBICA ORDER BY nro_internacion, fecha_hora_ingreso";
    
    private static final String DELETE_SQL = 
        "DELETE FROM SE_UBICA WHERE nro_internacion = ? AND fecha_hora_ingreso = ?";

    @Override
    public SeUbica create(SeUbica seUbica) throws DataAccessException {
        logger.info("Creating bed assignment for internacion: " + seUbica.getNroInternacion());
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
                stmt.setInt(1, seUbica.getNroInternacion());
                stmt.setTimestamp(2, Timestamp.valueOf(seUbica.getFechaHoraIngreso()));
                stmt.setInt(3, seUbica.getNroCama());
                stmt.setInt(4, seUbica.getNroHabitacion());
                stmt.executeUpdate();
            }
            
            connection.commit();
            logger.info("Successfully created bed assignment for internacion: " + seUbica.getNroInternacion());
            return seUbica;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for bed assignment creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create bed assignment: " + e.getMessage());
            throw new DataAccessException("Error creating bed assignment", e);
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
    public List<SeUbica> findByInternacion(int nroInternacion) throws DataAccessException {
        logger.fine("Finding bed assignments for internacion: " + nroInternacion);
        List<SeUbica> assignments = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_INTERNACION_SQL)) {
                stmt.setInt(1, nroInternacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        assignments.add(mapResultSetToSeUbica(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + assignments.size() + " bed assignments for internacion " + nroInternacion);
            return assignments;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find bed assignments by internacion: " + e.getMessage());
            throw new DataAccessException("Error finding bed assignments by internacion", e);
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
    public List<SeUbica> findByCama(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.fine("Finding bed assignments for cama: " + nroCama + " in room " + nroHabitacion);
        List<SeUbica> assignments = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_CAMA_SQL)) {
                stmt.setInt(1, nroCama);
                stmt.setInt(2, nroHabitacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        assignments.add(mapResultSetToSeUbica(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + assignments.size() + " bed assignments for cama " + nroCama);
            return assignments;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find bed assignments by cama: " + e.getMessage());
            throw new DataAccessException("Error finding bed assignments by cama", e);
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
    public List<SeUbica> findByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Finding bed assignments for habitacion: " + nroHabitacion);
        List<SeUbica> assignments = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_HABITACION_SQL)) {
                stmt.setInt(1, nroHabitacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        assignments.add(mapResultSetToSeUbica(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + assignments.size() + " bed assignments for habitacion " + nroHabitacion);
            return assignments;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find bed assignments by habitacion: " + e.getMessage());
            throw new DataAccessException("Error finding bed assignments by habitacion", e);
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
    public List<SeUbica> findAll() throws DataAccessException {
        logger.info("Finding all bed assignments");
        List<SeUbica> assignments = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    assignments.add(mapResultSetToSeUbica(rs));
                }
            }
            
            connection.commit();
            logger.fine("Found " + assignments.size() + " bed assignments");
            return assignments;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find all bed assignments: " + e.getMessage());
            throw new DataAccessException("Error retrieving all bed assignments", e);
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
    public boolean delete(int nroInternacion, String fechaHoraIngreso) throws DataAccessException {
        logger.info("Deleting bed assignment for internacion: " + nroInternacion);
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
                stmt.setInt(1, nroInternacion);
                stmt.setTimestamp(2, Timestamp.valueOf(fechaHoraIngreso));
                
                int rowsDeleted = stmt.executeUpdate();
                connection.commit();
                
                boolean deleted = rowsDeleted > 0;
                if (deleted) {
                    logger.info("Successfully deleted bed assignment for internacion: " + nroInternacion);
                } else {
                    logger.warning("No bed assignment found to delete for internacion: " + nroInternacion);
                }
                return deleted;
            }
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for bed assignment deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete bed assignment: " + e.getMessage());
            throw new DataAccessException("Error deleting bed assignment", e);
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

    private SeUbica mapResultSetToSeUbica(ResultSet rs) throws SQLException {
        SeUbica seUbica = new SeUbica();
        seUbica.setNroInternacion(rs.getInt("nro_internacion"));
        seUbica.setFechaHoraIngreso(rs.getTimestamp("fecha_hora_ingreso").toLocalDateTime());
        seUbica.setNroCama(rs.getInt("nro_cama"));
        seUbica.setNroHabitacion(rs.getInt("nro_habitacion"));
        return seUbica;
    }
}

