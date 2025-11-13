package org.hospital.internacion;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;

/**
 * Service layer for Habitacion business logic.
 */
public class HabitacionService {
    private static final Logger logger = Logger.getLogger(HabitacionService.class.getName());
    private final HabitacionDao habitacionDao;
    private final SectorDao sectorDao;

    public HabitacionService(HabitacionDao habitacionDao, SectorDao sectorDao) {
        this.habitacionDao = habitacionDao;
        this.sectorDao = sectorDao;
    }

    public HabitacionService() {
        this(new HabitacionDaoImpl(), new SectorDaoImpl());
    }

    /**
     * Create a new habitacion with business logic validation.
     */
    public Habitacion createHabitacion(Habitacion habitacion) throws DataAccessException {
        logger.info("Service: Creating new habitacion in sector: " + habitacion.getIdSector());
        
        validateHabitacionBusinessRules(habitacion);
        
        // Verify sector exists
        Optional<Sector> sector = sectorDao.findById(habitacion.getIdSector());
        if (!sector.isPresent()) {
            throw new IllegalArgumentException("Sector not found with ID: " + habitacion.getIdSector());
        }
        
        return habitacionDao.create(habitacion);
    }

    /**
     * Find a habitacion by ID.
     */
    public Optional<Habitacion> findHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Finding habitacion by ID: " + nroHabitacion);
        return habitacionDao.findById(nroHabitacion);
    }

    /**
     * Get all habitaciones.
     */
    public List<Habitacion> getAllHabitaciones() throws DataAccessException {
        logger.fine("Service: Retrieving all habitaciones");
        return habitacionDao.findAll();
    }

    /**
     * Get habitaciones by sector.
     */
    public List<Habitacion> getHabitacionesBySector(int idSector) throws DataAccessException {
        logger.fine("Service: Retrieving habitaciones for sector: " + idSector);
        return habitacionDao.findBySector(idSector);
    }

    /**
     * Update an existing habitacion.
     */
    public Habitacion updateHabitacion(Habitacion habitacion) throws DataAccessException {
        logger.info("Service: Updating habitacion with ID: " + habitacion.getNroHabitacion());
        
        validateHabitacionBusinessRules(habitacion);
        
        // Verify habitacion exists
        Optional<Habitacion> existing = habitacionDao.findById(habitacion.getNroHabitacion());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Habitacion not found with ID: " + habitacion.getNroHabitacion());
        }
        
        // Verify sector exists
        Optional<Sector> sector = sectorDao.findById(habitacion.getIdSector());
        if (!sector.isPresent()) {
            throw new IllegalArgumentException("Sector not found with ID: " + habitacion.getIdSector());
        }
        
        return habitacionDao.update(habitacion);
    }

    /**
     * Delete a habitacion.
     */
    public boolean deleteHabitacion(int nroHabitacion) throws DataAccessException {
        logger.info("Service: Deleting habitacion with ID: " + nroHabitacion);
        
        // Business logic: Check if habitacion has camas or active internaciones
        // This would be caught by FK constraint or could be checked explicitly
        
        return habitacionDao.delete(nroHabitacion);
    }

    /**
     * Validate business rules for a habitacion.
     */
    private void validateHabitacionBusinessRules(Habitacion habitacion) {
        if (habitacion == null) {
            throw new IllegalArgumentException("Habitacion cannot be null");
        }
        
        if (habitacion.getPiso() < 0) {
            throw new IllegalArgumentException("Piso cannot be negative");
        }
        
        if (habitacion.getPiso() > 50) {
            throw new IllegalArgumentException("Piso cannot exceed 50 floors");
        }
        
        if (habitacion.getOrientacion() == null || habitacion.getOrientacion().trim().isEmpty()) {
            throw new IllegalArgumentException("Orientacion cannot be empty");
        }
        
        // Validate orientacion is one of the cardinal directions or combinations
        String orientacion = habitacion.getOrientacion().toUpperCase();
        if (!orientacion.matches("^(N|S|E|O|W|NE|NO|NW|SE|SO|SW|NORTE|SUR|ESTE|OESTE)$")) {
            throw new IllegalArgumentException("Orientacion must be a valid direction (N, S, E, O, NE, NO, SE, SO, etc.)");
        }
        
        if (habitacion.getIdSector() <= 0) {
            throw new IllegalArgumentException("ID Sector must be positive");
        }
    }
}

