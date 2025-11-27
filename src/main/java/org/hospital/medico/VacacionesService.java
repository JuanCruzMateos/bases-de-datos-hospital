package org.hospital.medico;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;

/**
 * Service layer for Vacaciones business logic.
 * Implements transaction-based operations with proper validation.
 */
public class VacacionesService {
    private static final Logger logger = Logger.getLogger(VacacionesService.class.getName());
    private final VacacionesDao vacacionesDao;

    public VacacionesService(VacacionesDao vacacionesDao) {
        this.vacacionesDao = vacacionesDao;
    }

    public VacacionesService() {
        this(new VacacionesDaoImpl());
    }

    /**
     * Create a new vacation period with transaction logic and validation.
     * Uses SERIALIZABLE isolation level as per SQL transaction requirements.
     */
    public Vacaciones createVacaciones(Vacaciones vacaciones) throws DataAccessException {
        logger.info("Service: Creating new vacaciones for medico: " + vacaciones.getMatricula());
        validateVacaciones(vacaciones);
        return vacacionesDao.createWithTransaction(vacaciones);
    }

    /**
     * Update an existing vacation period with transaction logic.
     */
    public Vacaciones updateVacaciones(Vacaciones oldVacaciones, Vacaciones newVacaciones) throws DataAccessException {
        logger.info("Service: Updating vacaciones for medico: " + newVacaciones.getMatricula());
        validateVacaciones(newVacaciones);
        return vacacionesDao.updateWithTransaction(oldVacaciones, newVacaciones);
    }

    /**
     * Get all vacations for a specific medico.
     */
    public List<Vacaciones> getVacacionesByMatricula(long matricula) throws DataAccessException {
        logger.fine("Service: Retrieving vacaciones for medico: " + matricula);
        return vacacionesDao.findByMatricula(matricula);
    }

    /**
     * Get all vacations in the system.
     */
    public List<Vacaciones> getAllVacaciones() throws DataAccessException {
        logger.fine("Service: Retrieving all vacaciones");
        return vacacionesDao.findAll();
    }

    /**
     * Delete a specific vacation period.
     */
    public boolean deleteVacaciones(long matricula, LocalDate fechaInicio, LocalDate fechaFin) throws DataAccessException {
        logger.info("Service: Deleting vacaciones for medico " + matricula + 
                    " from " + fechaInicio + " to " + fechaFin);
        return vacacionesDao.delete(matricula, fechaInicio, fechaFin);
    }

    /**
     * Check if a medico is on vacation on a specific date.
     */
    public boolean isMedicoOnVacation(long matricula, LocalDate date) throws DataAccessException {
        logger.fine("Service: Checking if medico " + matricula + " is on vacation on " + date);
        return vacacionesDao.isOnVacation(matricula, date);
    }

    /**
     * Validate business rules for vacaciones.
     */
    private void validateVacaciones(Vacaciones vacaciones) {
        if (vacaciones == null) {
            throw new IllegalArgumentException("Vacaciones cannot be null");
        }
        
        if (vacaciones.getFechaInicio() == null) {
            throw new IllegalArgumentException("Fecha inicio must not be null");
        }
        
        if (vacaciones.getFechaFin() == null) {
            throw new IllegalArgumentException("Fecha fin must not be null");
        }
        
        if (vacaciones.getFechaInicio().isAfter(vacaciones.getFechaFin())) {
            throw new IllegalArgumentException("Fecha inicio must be before or equal to fecha fin");
        }
        
        if (vacaciones.getMatricula() <= 0) {
            throw new IllegalArgumentException("Matricula must be positive");
        }
    }
}

