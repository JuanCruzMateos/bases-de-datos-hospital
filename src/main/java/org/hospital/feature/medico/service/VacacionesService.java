package org.hospital.feature.medico.service;

import org.hospital.feature.medico.domain.Vacaciones;
import org.hospital.feature.medico.repository.VacacionesDao;
import org.hospital.feature.medico.repository.VacacionesDaoImpl;

import java.time.LocalDate;
import java.util.List;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Vacaciones (Vacations) operations.
 * Provides business logic and coordinates with the DAO layer.
 * Uses transaction-based operations with proper validation.
 */
public class VacacionesService {
    private VacacionesDao vacacionesDao;
    
    public VacacionesService() {
        this.vacacionesDao = new VacacionesDaoImpl();
    }
    
    /**
     * Get all vacation periods from the database.
     * 
     * @return List of all vacations
     * @throws DataAccessException if there's an error accessing data
     */
    public List<Vacaciones> getAllVacaciones() throws DataAccessException {
        return vacacionesDao.findAll();
    }
    
    /**
     * Get all vacation periods for a specific medico.
     * 
     * @param matricula The medico's matricula
     * @return List of vacations for the medico
     * @throws DataAccessException if there's an error accessing data
     */
    public List<Vacaciones> getVacacionesByMatricula(long matricula) throws DataAccessException {
        return vacacionesDao.findByMatricula(matricula);
    }
    
    /**
     * Create a new vacation period with transaction logic.
     * Validates all business rules including date ranges, overlaps, and conflicts.
     * 
     * @param vacaciones The vacation to create
     * @throws DataAccessException if there's an error accessing data
     * @throws IllegalArgumentException if business rules are violated
     */
    public void createVacaciones(Vacaciones vacaciones) throws DataAccessException {
        if (vacaciones == null) {
            throw new IllegalArgumentException("Vacaciones cannot be null");
        }
        
        // Use transaction-based create which includes all validations
        vacacionesDao.createWithTransaction(vacaciones);
    }
    
    /**
     * Update an existing vacation period with transaction logic.
     * Deletes the old vacation and creates a new one with validation.
     * 
     * @param oldVacaciones The old vacation to be replaced
     * @param newVacaciones The new vacation data
     * @throws DataAccessException if there's an error accessing data
     * @throws IllegalArgumentException if business rules are violated
     */
    public void updateVacaciones(Vacaciones oldVacaciones, Vacaciones newVacaciones) throws DataAccessException {
        if (oldVacaciones == null || newVacaciones == null) {
            throw new IllegalArgumentException("Vacaciones cannot be null");
        }
        
        // Use transaction-based update which handles delete + create with validation
        vacacionesDao.updateWithTransaction(oldVacaciones, newVacaciones);
    }
    
    /**
     * Delete a specific vacation period.
     * 
     * @param matricula The medico's matricula
     * @param fechaInicio The start date of the vacation period
     * @param fechaFin The end date of the vacation period
     * @return true if the vacation was deleted, false if not found
     * @throws DataAccessException if there's an error accessing data
     */
    public boolean deleteVacaciones(long matricula, LocalDate fechaInicio, LocalDate fechaFin) throws DataAccessException {
        return vacacionesDao.delete(matricula, fechaInicio, fechaFin);
    }
    
    /**
     * Check if a medico is on vacation on a specific date.
     * 
     * @param matricula The medico's matricula
     * @param date The date to check
     * @return true if the medico is on vacation on that date
     * @throws DataAccessException if there's an error accessing data
     */
    public boolean isOnVacation(long matricula, LocalDate date) throws DataAccessException {
        return vacacionesDao.isOnVacation(matricula, date);
    }
}

