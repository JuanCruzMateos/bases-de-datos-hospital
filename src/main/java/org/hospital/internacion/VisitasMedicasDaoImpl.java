package org.hospital.internacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.config.DatabaseConfig;
import org.hospital.exception.DataAccessException;

/**
 * Implementation of VisitasMedicasDao using JDBC.
 * Executes stored procedures for medical visits reports.
 */
public class VisitasMedicasDaoImpl implements VisitasMedicasDao {
    private static final Logger logger = Logger.getLogger(VisitasMedicasDaoImpl.class.getName());

    @Override
    public List<InternacionPaciente> getInternacionesPaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.fine("DAO: Calling sp_internaciones_paciente for patient: " + tipoDocumento + " " + nroDocumento);
        List<InternacionPaciente> resultados = new ArrayList<>();
        
        String sql = "{CALL sp_internaciones_paciente(?, ?, ?)}";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set IN parameters
            stmt.setString(1, tipoDocumento);
            stmt.setString(2, nroDocumento);
            
            // Register OUT parameter for cursor
            stmt.registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR);
            
            // Execute procedure
            stmt.execute();
            
            // Get result set from cursor
            try (ResultSet rs = (ResultSet) stmt.getObject(3)) {
                while (rs.next()) {
                    InternacionPaciente internacion = new InternacionPaciente();
                    internacion.setNroInternacion(rs.getInt("nro_internacion"));
                    internacion.setFechaInicio(rs.getDate("fecha_inicio"));
                    Date fechaFin = rs.getDate("fecha_fin");
                    internacion.setFechaFin(fechaFin);
                    // Calculate estado based on fecha_fin
                    internacion.setEstado(fechaFin == null ? "EN CURSO" : "FINALIZADA");
                    resultados.add(internacion);
                }
            }
            
            logger.fine("DAO: Retrieved " + resultados.size() + " internations");
            return resultados;
            
        } catch (SQLException e) {
            logger.severe("Database error calling sp_internaciones_paciente: " + e.getMessage());
            throw new DataAccessException("Error getting patient internations", e);
        }
    }

    @Override
    public List<ComentarioVisita> getComentariosVisitas(int nroInternacion) 
            throws DataAccessException {
        logger.fine("DAO: Calling sp_comentarios_visitas for internation: " + nroInternacion);
        List<ComentarioVisita> resultados = new ArrayList<>();
        
        String sql = "{CALL sp_comentarios_visitas(?, ?)}";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set IN parameter
            stmt.setInt(1, nroInternacion);
            
            // Register OUT parameter for cursor
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            
            // Execute procedure
            stmt.execute();
            
            // Get result set from cursor
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                while (rs.next()) {
                    ComentarioVisita comentario = new ComentarioVisita();
                    comentario.setNroInternacion(rs.getInt("nro_internacion"));
                    comentario.setPaciente(rs.getString("paciente"));
                    comentario.setMedico(rs.getString("medico"));
                    comentario.setFechaRecorrido(rs.getDate("fecha_recorrido"));
                    comentario.setHoraInicio(rs.getTime("hora_inicio"));
                    comentario.setHoraFin(rs.getTime("hora_fin"));
                    comentario.setComentario(rs.getString("comentario"));
                    resultados.add(comentario);
                }
            }
            
            logger.fine("DAO: Retrieved " + resultados.size() + " comments");
            return resultados;
            
        } catch (SQLException e) {
            logger.severe("Database error calling sp_comentarios_visitas: " + e.getMessage());
            throw new DataAccessException("Error getting visit comments", e);
        }
    }
}

