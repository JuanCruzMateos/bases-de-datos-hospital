package org.hospital.feature.internacion.repository;

import org.hospital.feature.internacion.domain.CamaDisponibleDetalle;
import org.hospital.feature.internacion.domain.CamaDisponibleResumen;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;

/**
 * Implementation of CamaDisponibleDao using JDBC.
 * Executes stored procedures for available beds reports.
 */
public class CamaDisponibleDaoImpl implements CamaDisponibleDao {
    private static final Logger logger = Logger.getLogger(CamaDisponibleDaoImpl.class.getName());

    @Override
    public List<CamaDisponibleResumen> getResumen() throws DataAccessException {
        logger.fine("DAO: Calling sp_camas_disponibles_resumen");
        List<CamaDisponibleResumen> resultados = new ArrayList<>();
        
        String sql = "{CALL sp_camas_disponibles_resumen(?)}";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Register OUT parameter for cursor
            stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            
            // Execute procedure
            stmt.execute();
            
            // Get result set from cursor
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    CamaDisponibleResumen resumen = new CamaDisponibleResumen();
                    resumen.setIdSector(rs.getInt("id_sector"));
                    resumen.setDescripcion(rs.getString("descripcion"));
                    resumen.setCamasLibres(rs.getInt("camas_libres"));
                    resultados.add(resumen);
                }
            }
            
            logger.fine("DAO: Retrieved " + resultados.size() + " resumen records");
            return resultados;
            
        } catch (SQLException e) {
            logger.severe("Database error calling sp_camas_disponibles_resumen: " + e.getMessage());
            throw new DataAccessException("Error getting available beds summary", e);
        }
    }

    @Override
    public List<CamaDisponibleDetalle> getDetalle(int idSector) throws DataAccessException {
        logger.fine("DAO: Calling sp_camas_disponibles_detalle for sector: " + idSector);
        List<CamaDisponibleDetalle> resultados = new ArrayList<>();
        
        String sql = "{CALL sp_camas_disponibles_detalle(?, ?)}";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set IN parameter
            stmt.setInt(1, idSector);
            
            // Register OUT parameter for cursor
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            
            // Execute procedure
            stmt.execute();
            
            // Get result set from cursor
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                while (rs.next()) {
                    CamaDisponibleDetalle detalle = new CamaDisponibleDetalle();
                    detalle.setIdSector(rs.getInt("id_sector"));
                    detalle.setDescripcion(rs.getString("descripcion"));
                    detalle.setNroHabitacion(rs.getInt("nro_habitacion"));
                    detalle.setPiso(rs.getInt("piso"));
                    detalle.setOrientacion(rs.getString("orientacion"));
                    detalle.setNroCama(rs.getInt("nro_cama"));
                    detalle.setEstado(rs.getString("estado"));
                    resultados.add(detalle);
                }
            }
            
            logger.fine("DAO: Retrieved " + resultados.size() + " detalle records");
            return resultados;
            
        } catch (SQLException e) {
            logger.severe("Database error calling sp_camas_disponibles_detalle: " + e.getMessage());
            throw new DataAccessException("Error getting available beds detail", e);
        }
    }
}

