package org.hospital.internacion;

import java.sql.Timestamp;
import java.util.List;
import org.hospital.exception.DataAccessException;

/**
 * DAO interface for Auditoria Guardias stored procedures.
 */
public interface AuditoriaGuardiasDao {
    
    /**
     * Get audit records for guard assignments.
     * Calls sp_auditoria_guardias with optional filters.
     * 
     * @param usuario Database user (null for all users)
     * @param desde Start date/time (null for no start filter)
     * @param hasta End date/time (null for no end filter)
     * @return List of audit records
     * @throws DataAccessException if database access fails
     */
    List<AuditoriaGuardia> getAuditoriaGuardias(String usuario, Timestamp desde, Timestamp hasta) 
            throws DataAccessException;
}

