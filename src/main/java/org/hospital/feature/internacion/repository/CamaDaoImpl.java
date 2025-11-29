package org.hospital.feature.internacion.repository;

import org.hospital.feature.internacion.domain.Cama;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.sql.CallableStatement;
import java.sql.Types;

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;

/**
 * DAO implementation for Cama (Bed) entity.
 * Uses manual transaction management (V1 pattern).
 */
public class CamaDaoImpl implements CamaDao {
    private static final Logger logger = Logger.getLogger(CamaDaoImpl.class.getName());

    private static final String INSERT_SQL = 
        "INSERT INTO CAMA (nro_cama, nro_habitacion, estado) VALUES (?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT nro_cama, nro_habitacion, estado FROM CAMA WHERE nro_cama = ? AND nro_habitacion = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT nro_cama, nro_habitacion, estado FROM CAMA ORDER BY nro_habitacion, nro_cama";
    
    private static final String SELECT_BY_HABITACION_SQL = 
        "SELECT nro_cama, nro_habitacion, estado FROM CAMA WHERE nro_habitacion = ? ORDER BY nro_cama";
    
    private static final String SELECT_BY_ESTADO_SQL = 
        "SELECT nro_cama, nro_habitacion, estado FROM CAMA WHERE estado = ? ORDER BY nro_habitacion, nro_cama";
    
    private static final String SELECT_AVAILABLE_BY_HABITACION_SQL = 
        "SELECT nro_cama, nro_habitacion, estado FROM CAMA WHERE nro_habitacion = ? AND estado = 'DISPONIBLE' ORDER BY nro_cama";
    
    private static final String UPDATE_SQL = 
        "UPDATE CAMA SET estado = ? WHERE nro_cama = ? AND nro_habitacion = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM CAMA WHERE nro_cama = ? AND nro_habitacion = ?";

    @Override
    public Cama create(Cama cama) throws DataAccessException {
        logger.info("Creating cama: " + cama.getNroCama() + " in room " + cama.getNroHabitacion());
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
                stmt.setInt(1, cama.getNroCama());
                stmt.setInt(2, cama.getNroHabitacion());
                stmt.setString(3, cama.getEstado());
                stmt.executeUpdate();
            }
            
            connection.commit();
            logger.info("Successfully created cama: " + cama.getNroCama());
            return cama;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for cama creation");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to create cama: " + e.getMessage());
            throw new DataAccessException("Error creating cama", e);
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
    public Optional<Cama> findByNroCamaAndHabitacion(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.fine("Finding cama by nro_cama: " + nroCama + " and nro_habitacion: " + nroHabitacion);
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
                stmt.setInt(1, nroCama);
                stmt.setInt(2, nroHabitacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Cama cama = mapResultSetToCama(rs);
                        connection.commit();
                        return Optional.of(cama);
                    }
                }
            }
            
            connection.commit();
            return Optional.empty();
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find cama: " + e.getMessage());
            throw new DataAccessException("Error finding cama", e);
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
    public List<Cama> findAll() throws DataAccessException {
        logger.info("Finding all camas");
        List<Cama> camas = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    camas.add(mapResultSetToCama(rs));
                }
            }
            
            connection.commit();
            logger.fine("Found " + camas.size() + " camas");
            return camas;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find all camas: " + e.getMessage());
            throw new DataAccessException("Error retrieving all camas", e);
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
    public List<Cama> findByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Finding camas by habitacion: " + nroHabitacion);
        List<Cama> camas = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_HABITACION_SQL)) {
                stmt.setInt(1, nroHabitacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        camas.add(mapResultSetToCama(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + camas.size() + " camas in habitacion " + nroHabitacion);
            return camas;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find camas by habitacion: " + e.getMessage());
            throw new DataAccessException("Error finding camas by habitacion", e);
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
    public List<Cama> findByEstado(String estado) throws DataAccessException {
        logger.fine("Finding camas by estado: " + estado);
        List<Cama> camas = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ESTADO_SQL)) {
                stmt.setString(1, estado);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        camas.add(mapResultSetToCama(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + camas.size() + " camas with estado " + estado);
            return camas;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find camas by estado: " + e.getMessage());
            throw new DataAccessException("Error finding camas by estado", e);
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
    public List<Cama> findAvailableByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Finding available camas in habitacion: " + nroHabitacion);
        List<Cama> camas = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_AVAILABLE_BY_HABITACION_SQL)) {
                stmt.setInt(1, nroHabitacion);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        camas.add(mapResultSetToCama(rs));
                    }
                }
            }
            
            connection.commit();
            logger.fine("Found " + camas.size() + " available camas in habitacion " + nroHabitacion);
            return camas;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find available camas: " + e.getMessage());
            throw new DataAccessException("Error finding available camas", e);
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
    public Cama update(Cama cama) throws DataAccessException {
        logger.info("Updating cama: " + cama.getNroCama() + " in room " + cama.getNroHabitacion());
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
                stmt.setString(1, cama.getEstado());
                stmt.setInt(2, cama.getNroCama());
                stmt.setInt(3, cama.getNroHabitacion());
                
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Cama not found for update");
                }
            }
            
            connection.commit();
            logger.info("Successfully updated cama: " + cama.getNroCama());
            return cama;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for cama update");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to update cama: " + e.getMessage());
            throw new DataAccessException("Error updating cama", e);
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
    public boolean delete(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.info("Deleting cama: " + nroCama + " from room " + nroHabitacion);
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
                stmt.setInt(1, nroCama);
                stmt.setInt(2, nroHabitacion);
                
                int rowsDeleted = stmt.executeUpdate();
                connection.commit();
                
                boolean deleted = rowsDeleted > 0;
                if (deleted) {
                    logger.info("Successfully deleted cama: " + nroCama);
                } else {
                    logger.warning("No cama found to delete with nro_cama: " + nroCama);
                }
                return deleted;
            }
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for cama deletion");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to delete cama: " + e.getMessage());
            throw new DataAccessException("Error deleting cama", e);
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
    public void agregarCama(int nroHabitacion, int nroCama) throws DataAccessException {
        logger.info("DAO: Adding cama " + nroCama + " to habitacion " + nroHabitacion);
        Connection connection = null;
        String sql = "{ call sp_agregar_cama(?, ?) }";

        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (CallableStatement stmt = connection.prepareCall(sql)) {
                // 1) Habitacion (IN)
                stmt.setInt(1, nroHabitacion);

                // 2) Cama (IN OUT)
                //    Si nroCama <= 0 lo tratamos como "campo vacío"
                if (nroCama <= 0) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, nroCama);
                }

                // Registramos OUT para leer el número final asignado
                stmt.registerOutParameter(2, Types.INTEGER);

                // Ejecutamos la SP
                stmt.execute();

                // Obtenemos el número de cama definitivo (sea el ingresado o el auto)
                int camaAsignada = stmt.getInt(2);
                nroCama = camaAsignada;
            }

            connection.commit();
            logger.info("Successfully added cama " + nroCama + " to habitacion " + nroHabitacion);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for agregarCama");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to call sp_agregar_cama: " + e.getMessage());
            throw new DataAccessException("Error calling sp_agregar_cama", e);
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
    public void eliminarODesactivarCama(int nroHabitacion, int nroCama) throws DataAccessException {
        logger.info("DAO: Deleting/Disabling cama " + nroCama + " from habitacion " + nroHabitacion);
        Connection connection = null;
        String sql = "{ call sp_eliminar_o_desactivar_cama(?, ?) }";

        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            try (CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.setInt(1, nroHabitacion);
                stmt.setInt(2, nroCama);
                stmt.execute();
            }

            connection.commit();
            logger.info("Successfully executed sp_eliminar_o_desactivar_cama for cama " 
                        + nroCama + " in habitacion " + nroHabitacion);

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for eliminarODesactivarCama");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to call sp_eliminar_o_desactivar_cama: " + e.getMessage());
            throw new DataAccessException("Error calling sp_eliminar_o_desactivar_cama", e);
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

    private Cama mapResultSetToCama(ResultSet rs) throws SQLException {
        Cama cama = new Cama();
        cama.setNroCama(rs.getInt("nro_cama"));
        cama.setNroHabitacion(rs.getInt("nro_habitacion"));
        cama.setEstado(rs.getString("estado"));
        return cama;
    }
}

