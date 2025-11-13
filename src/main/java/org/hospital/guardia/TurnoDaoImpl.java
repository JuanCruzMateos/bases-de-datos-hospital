package org.hospital.guardia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

/**
 * DAO implementation for Turno entity.
 * Provides read-only access to turno data.
 */
public class TurnoDaoImpl implements TurnoDao {
    private static final Logger logger = Logger.getLogger(TurnoDaoImpl.class.getName());

    private static final String SELECT_ALL_SQL = 
        "SELECT id_turno, horario FROM TURNO ORDER BY id_turno";

    @Override
    public List<Turno> findAll() throws DataAccessException {
        logger.info("Finding all turnos");
        List<Turno> turnos = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    turnos.add(mapResultSetToTurno(rs));
                }
            }
            
            connection.commit();
            logger.fine("Found " + turnos.size() + " turnos");
            return turnos;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warning("Transaction rolled back for findAll turnos");
                } catch (SQLException ex) {
                    logger.severe("Failed to rollback transaction: " + ex.getMessage());
                }
            }
            logger.severe("Failed to find all turnos: " + e.getMessage());
            throw new DataAccessException("Error retrieving all turnos", e);
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

    private Turno mapResultSetToTurno(ResultSet rs) throws SQLException {
        Turno turno = new Turno();
        turno.setIdTurno(rs.getInt("id_turno"));
        turno.setHorario(rs.getString("horario"));
        return turno;
    }
}

