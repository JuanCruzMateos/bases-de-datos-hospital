package org.hospital.feature.internacion.service;

import org.hospital.feature.internacion.domain.Sector;
import org.hospital.feature.internacion.repository.SectorDao;
import org.hospital.feature.internacion.repository.SectorDaoImpl;
import org.hospital.feature.internacion.domain.*;
import org.hospital.feature.internacion.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Sector business logic.
 */
public class SectorService {
    private static final Logger logger = Logger.getLogger(SectorService.class.getName());
    private final SectorDao sectorDao;

    public SectorService(SectorDao sectorDao) {
        this.sectorDao = sectorDao;
    }

    public SectorService() {
        this(new SectorDaoImpl());
    }

    /**
     * Create a new sector with business logic validation.
     */
    public Sector createSector(Sector sector) throws DataAccessException {
        logger.info("Service: Creating new sector: " + sector.getDescripcion());
        
        validateSectorBusinessRules(sector);
        
        return sectorDao.create(sector);
    }

    /**
     * Find a sector by ID.
     */
    public Optional<Sector> findSector(int idSector) throws DataAccessException {
        logger.fine("Service: Finding sector by ID: " + idSector);
        return sectorDao.findById(idSector);
    }

    /**
     * Get all sectors.
     */
    public List<Sector> getAllSectores() throws DataAccessException {
        logger.fine("Service: Retrieving all sectores");
        return sectorDao.findAll();
    }

    /**
     * Update an existing sector.
     */
    public Sector updateSector(Sector sector) throws DataAccessException {
        logger.info("Service: Updating sector with ID: " + sector.getIdSector());
        
        validateSectorBusinessRules(sector);
        
        // Verify sector exists
        Optional<Sector> existing = sectorDao.findById(sector.getIdSector());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Sector not found with ID: " + sector.getIdSector());
        }
        
        return sectorDao.update(sector);
    }

    /**
     * Delete a sector.
     */
    public boolean deleteSector(int idSector) throws DataAccessException {
        logger.info("Service: Deleting sector with ID: " + idSector);
        
        // Business logic: Check if sector has habitaciones
        // This would require additional query or will be caught by FK constraint
        
        return sectorDao.delete(idSector);
    }

    /**
     * Validate business rules for a sector.
     */
    private void validateSectorBusinessRules(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException("Sector cannot be null");
        }
        
        if (sector.getDescripcion() == null || sector.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("Descripcion cannot be empty");
        }
        
        if (sector.getDescripcion().length() > 100) {
            throw new IllegalArgumentException("Descripcion cannot exceed 100 characters");
        }
    }
}

