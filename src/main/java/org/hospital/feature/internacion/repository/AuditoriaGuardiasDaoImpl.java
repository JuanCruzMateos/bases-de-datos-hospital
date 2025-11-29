package org.hospital.feature.internacion.repository;

import org.hospital.feature.internacion.domain.AuditoriaGuardia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.config.DatabaseConfig;
import org.hospital.common.exception.DataAccessException;

/**
 * Implementation of AuditoriaGuardiasDao using JDBC.
 * Executes stored procedures for guard audit reports.
 */
public class AuditoriaGuardiasDaoImpl implements AuditoriaGuardiasDao {
    private static final Logger logger = Logger.getLogger(AuditoriaGuardiasDaoImpl.class.getName());

    @Override
    public List<AuditoriaGuardia> getAuditoriaGuardias(String usuario, Timestamp desde, Timestamp hasta) 
            throws DataAccessException {
        logger.fine("DAO: Calling sp_auditoria_guardias with filters - user: " + usuario + 
                   ", desde: " + desde + ", hasta: " + hasta);
        List<AuditoriaGuardia> resultados = new ArrayList<>();
        
        String sql = "{CALL sp_auditoria_guardias(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set IN parameters (all optional)
            if (usuario != null && !usuario.trim().isEmpty()) {
                stmt.setString(1, usuario.trim());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            
            if (desde != null) {
                stmt.setTimestamp(2, desde);
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            
            if (hasta != null) {
                stmt.setTimestamp(3, hasta);
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            // Register OUT parameter for cursor
            stmt.registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR);
            
            // Execute procedure
            stmt.execute();
            
            // Get result set from cursor
            try (ResultSet rs = (ResultSet) stmt.getObject(4)) {
                while (rs.next()) {
                    AuditoriaGuardia auditoria = new AuditoriaGuardia();
                    auditoria.setIdAuditoria(rs.getInt("id_auditoria"));
                    auditoria.setFechaHoraReg(rs.getTimestamp("fecha_hora_reg"));
                    auditoria.setUsuarioBd(rs.getString("usuario_bd"));
                    auditoria.setOperacion(rs.getString("operacion"));
                    
                    // Handle nullable integers
                    int nroGuardia = rs.getInt("nro_guardia");
                    auditoria.setNroGuardia(rs.wasNull() ? null : nroGuardia);
                    
                    auditoria.setFechaHoraGuard(rs.getTimestamp("fecha_hora_guard"));
                    
                    int matricula = rs.getInt("matricula");
                    auditoria.setMatricula(rs.wasNull() ? null : matricula);
                    
                    int codEspecialidad = rs.getInt("cod_especialidad");
                    auditoria.setCodEspecialidad(rs.wasNull() ? null : codEspecialidad);
                    
                    int idTurno = rs.getInt("id_turno");
                    auditoria.setIdTurno(rs.wasNull() ? null : idTurno);
                    
                    auditoria.setDetalleOld(rs.getString("detalle_old"));
                    auditoria.setDetalleNew(rs.getString("detalle_new"));
                    
                    resultados.add(auditoria);
                }
            }
            
            logger.fine("DAO: Retrieved " + resultados.size() + " audit records");
            return resultados;
            
        } catch (SQLException e) {
            logger.severe("Database error calling sp_auditoria_guardias: " + e.getMessage());
            throw new DataAccessException("Error getting guard audit records", e);
        }
    }
}

