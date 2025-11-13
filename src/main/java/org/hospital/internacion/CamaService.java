package org.hospital.internacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;

/**
 * Service layer for Cama (Bed) and SeUbica (Bed Assignment) business logic.
 */
public class CamaService {
    private static final Logger logger = Logger.getLogger(CamaService.class.getName());
    private final CamaDao camaDao;
    private final SeUbicaDao seUbicaDao;
    private final HabitacionDao habitacionDao;

    // Valid bed states
    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_OCUPADA = "OCUPADA";
    public static final String ESTADO_MANTENIMIENTO = "MANTENIMIENTO";

    public CamaService(CamaDao camaDao, SeUbicaDao seUbicaDao, HabitacionDao habitacionDao) {
        this.camaDao = camaDao;
        this.seUbicaDao = seUbicaDao;
        this.habitacionDao = habitacionDao;
    }

    public CamaService() {
        this(new CamaDaoImpl(), new SeUbicaDaoImpl(), new HabitacionDaoImpl());
    }

    /**
     * Create a new bed with business logic validation.
     */
    public Cama createCama(Cama cama) throws DataAccessException {
        logger.info("Service: Creating new cama for room: " + cama.getNroHabitacion());
        
        validateCamaBusinessRules(cama);
        
        // Verify habitacion exists
        if (!habitacionDao.findById(cama.getNroHabitacion()).isPresent()) {
            throw new IllegalArgumentException("Habitacion not found with ID: " + cama.getNroHabitacion());
        }
        
        // Check for duplicate bed number in the same room
        Optional<Cama> existing = camaDao.findByNroCamaAndHabitacion(cama.getNroCama(), cama.getNroHabitacion());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Bed " + cama.getNroCama() + " already exists in room " + cama.getNroHabitacion());
        }
        
        // Default to DISPONIBLE if not specified
        if (cama.getEstado() == null || cama.getEstado().trim().isEmpty()) {
            cama.setEstado(ESTADO_DISPONIBLE);
        }
        
        return camaDao.create(cama);
    }

    /**
     * Find a specific bed.
     */
    public Optional<Cama> findCama(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Finding cama " + nroCama + " in room " + nroHabitacion);
        return camaDao.findByNroCamaAndHabitacion(nroCama, nroHabitacion);
    }

    /**
     * Get all beds.
     */
    public List<Cama> getAllCamas() throws DataAccessException {
        logger.fine("Service: Retrieving all camas");
        return camaDao.findAll();
    }

    /**
     * Get all beds in a specific room.
     */
    public List<Cama> getCamasByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Retrieving camas for habitacion: " + nroHabitacion);
        return camaDao.findByHabitacion(nroHabitacion);
    }

    /**
     * Get all available beds in a specific room.
     */
    public List<Cama> getAvailableCamasByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Retrieving available camas for habitacion: " + nroHabitacion);
        return camaDao.findAvailableByHabitacion(nroHabitacion);
    }

    /**
     * Get all beds by state.
     */
    public List<Cama> getCamasByEstado(String estado) throws DataAccessException {
        logger.fine("Service: Retrieving camas with estado: " + estado);
        return camaDao.findByEstado(estado);
    }

    /**
     * Update a bed's state.
     */
    public Cama updateCama(Cama cama) throws DataAccessException {
        logger.info("Service: Updating cama " + cama.getNroCama() + " in room " + cama.getNroHabitacion());
        
        validateCamaBusinessRules(cama);
        
        // Verify cama exists
        Optional<Cama> existing = camaDao.findByNroCamaAndHabitacion(cama.getNroCama(), cama.getNroHabitacion());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Cama not found: " + cama.getNroCama() + " in room " + cama.getNroHabitacion());
        }
        
        return camaDao.update(cama);
    }

    /**
     * Delete a bed.
     */
    public boolean deleteCama(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.info("Service: Deleting cama " + nroCama + " from room " + nroHabitacion);
        
        // Business rule: Cannot delete bed if it has assignments
        List<SeUbica> assignments = seUbicaDao.findByCama(nroCama, nroHabitacion);
        if (!assignments.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete cama with existing bed assignments. Remove assignments first.");
        }
        
        return camaDao.delete(nroCama, nroHabitacion);
    }

    /**
     * Assign a bed to an internacion.
     */
    public SeUbica assignBedToInternacion(int nroInternacion, int nroCama, int nroHabitacion) throws DataAccessException {
        logger.info("Service: Assigning bed " + nroCama + " to internacion " + nroInternacion);
        
        // Verify bed exists
        Optional<Cama> camaOpt = camaDao.findByNroCamaAndHabitacion(nroCama, nroHabitacion);
        if (!camaOpt.isPresent()) {
            throw new IllegalArgumentException("Bed not found: " + nroCama + " in room " + nroHabitacion);
        }
        
        Cama cama = camaOpt.get();
        
        // Business rule: Bed must be available
        if (!ESTADO_DISPONIBLE.equals(cama.getEstado())) {
            throw new IllegalArgumentException("Bed " + nroCama + " is not available (current state: " + cama.getEstado() + ")");
        }
        
        // Create the assignment
        SeUbica seUbica = new SeUbica();
        seUbica.setNroInternacion(nroInternacion);
        seUbica.setFechaHoraIngreso(LocalDateTime.now());
        seUbica.setNroCama(nroCama);
        seUbica.setNroHabitacion(nroHabitacion);
        
        SeUbica created = seUbicaDao.create(seUbica);
        
        // Update bed state to OCUPADA
        cama.setEstado(ESTADO_OCUPADA);
        camaDao.update(cama);
        
        logger.info("Successfully assigned bed " + nroCama + " to internacion " + nroInternacion);
        return created;
    }

    /**
     * Release a bed (mark as available).
     */
    public void releaseBed(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.info("Service: Releasing bed " + nroCama + " in room " + nroHabitacion);
        
        Optional<Cama> camaOpt = camaDao.findByNroCamaAndHabitacion(nroCama, nroHabitacion);
        if (!camaOpt.isPresent()) {
            throw new IllegalArgumentException("Bed not found: " + nroCama + " in room " + nroHabitacion);
        }
        
        Cama cama = camaOpt.get();
        cama.setEstado(ESTADO_DISPONIBLE);
        camaDao.update(cama);
        
        logger.info("Successfully released bed " + nroCama);
    }

    /**
     * Get all bed assignments for an internacion.
     */
    public List<SeUbica> getBedAssignmentsByInternacion(int nroInternacion) throws DataAccessException {
        logger.fine("Service: Retrieving bed assignments for internacion: " + nroInternacion);
        return seUbicaDao.findByInternacion(nroInternacion);
    }

    /**
     * Get all bed assignments for a specific bed.
     */
    public List<SeUbica> getBedAssignmentsByCama(int nroCama, int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Retrieving bed assignments for cama: " + nroCama);
        return seUbicaDao.findByCama(nroCama, nroHabitacion);
    }

    /**
     * Get all bed assignments for a room.
     */
    public List<SeUbica> getBedAssignmentsByHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Retrieving bed assignments for habitacion: " + nroHabitacion);
        return seUbicaDao.findByHabitacion(nroHabitacion);
    }

    /**
     * Validate business rules for a bed.
     */
    private void validateCamaBusinessRules(Cama cama) {
        if (cama == null) {
            throw new IllegalArgumentException("Cama cannot be null");
        }
        
        if (cama.getNroCama() <= 0) {
            throw new IllegalArgumentException("Nro cama must be positive");
        }
        
        if (cama.getNroHabitacion() <= 0) {
            throw new IllegalArgumentException("Nro habitacion must be positive");
        }
        
        if (cama.getEstado() == null || cama.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("Estado cannot be empty");
        }
        
        // Validate estado is one of the valid states
        String estado = cama.getEstado().toUpperCase();
        if (!ESTADO_DISPONIBLE.equals(estado) && !ESTADO_OCUPADA.equals(estado) && !ESTADO_MANTENIMIENTO.equals(estado)) {
            throw new IllegalArgumentException("Invalid estado. Must be one of: DISPONIBLE, OCUPADA, MANTENIMIENTO");
        }
        
        // Normalize estado to uppercase
        cama.setEstado(estado);
    }
}

