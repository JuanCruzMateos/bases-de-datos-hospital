package org.hospital.feature.internacion.service;

import org.hospital.feature.internacion.domain.AuditoriaGuardia;
import org.hospital.feature.internacion.repository.AuditoriaGuardiasDao;
import org.hospital.feature.internacion.repository.AuditoriaGuardiasDaoImpl;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Auditoria Guardias reports.
 */
public class AuditoriaGuardiasService {
    private static final Logger logger = Logger.getLogger(AuditoriaGuardiasService.class.getName());
    private final AuditoriaGuardiasDao dao;

    public AuditoriaGuardiasService(AuditoriaGuardiasDao dao) {
        this.dao = dao;
    }

    public AuditoriaGuardiasService() {
        this(new AuditoriaGuardiasDaoImpl());
    }

    /**
     * Get audit records for guard assignments with optional filters.
     * 
     * @param usuario Database user (null or empty for all users)
     * @param desde Start date/time (null for no start filter)
     * @param hasta End date/time (null for no end filter)
     * @return List of audit records
     * @throws DataAccessException if database access fails
     */
    public List<AuditoriaGuardia> getAuditoriaGuardias(String usuario, Timestamp desde, Timestamp hasta) 
            throws DataAccessException {
        logger.info("Service: Getting guard audit records");
        
        // Validate date range if both are provided
        if (desde != null && hasta != null && desde.after(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        
        return dao.getAuditoriaGuardias(usuario, desde, hasta);
    }
}

