package org.hospital.feature.internacion.service;

import org.hospital.feature.internacion.domain.CamaDisponibleDetalle;
import org.hospital.feature.internacion.domain.CamaDisponibleResumen;
import org.hospital.feature.internacion.domain.Sector;
import org.hospital.feature.internacion.repository.CamaDisponibleDao;
import org.hospital.feature.internacion.repository.CamaDisponibleDaoImpl;
import org.hospital.feature.internacion.domain.*;
import org.hospital.feature.internacion.repository.*;

import java.util.List;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Camas Disponibles reports.
 */
public class CamaDisponibleService {
    private static final Logger logger = Logger.getLogger(CamaDisponibleService.class.getName());
    private final CamaDisponibleDao dao;

    public CamaDisponibleService(CamaDisponibleDao dao) {
        this.dao = dao;
    }

    public CamaDisponibleService() {
        this(new CamaDisponibleDaoImpl());
    }

    /**
     * Get summary of available beds grouped by sector.
     * 
     * @return List of available beds summary
     * @throws DataAccessException if database access fails
     */
    public List<CamaDisponibleResumen> getResumen() throws DataAccessException {
        logger.info("Service: Getting available beds summary");
        return dao.getResumen();
    }

    /**
     * Get detailed list of available beds for a specific sector.
     * 
     * @param idSector The sector ID to filter by
     * @return List of available beds with details
     * @throws DataAccessException if database access fails
     */
    public List<CamaDisponibleDetalle> getDetalle(int idSector) throws DataAccessException {
        logger.info("Service: Getting available beds detail for sector: " + idSector);
        
        if (idSector <= 0) {
            throw new IllegalArgumentException("Sector ID must be positive");
        }
        
        return dao.getDetalle(idSector);
    }
}

