package org.hospital.medico;

import java.time.LocalDate;
import java.util.List;

import org.hospital.exception.DataAccessException;

public interface VacacionesDao {
    /**
     * Create a new vacation period for a medico.
     * 
     * @param vacaciones The vacation to create
     * @return The created vacation
     * @throws DataAccessException if there's an error accessing data
     */
    Vacaciones create(Vacaciones vacaciones) throws DataAccessException;
    
    /**
     * Find all vacation periods for a specific medico.
     * 
     * @param matricula The medico's matricula
     * @return List of all vacations for the medico
     * @throws DataAccessException if there's an error accessing data
     */
    List<Vacaciones> findByMatricula(long matricula) throws DataAccessException;
    
    /**
     * Find all vacation periods.
     * 
     * @return List of all vacations
     * @throws DataAccessException if there's an error accessing data
     */
    List<Vacaciones> findAll() throws DataAccessException;
    
    /**
     * Check if a medico is on vacation on a specific date.
     * 
     * @param matricula The medico's matricula
     * @param date The date to check
     * @return true if the medico is on vacation on that date
     * @throws DataAccessException if there's an error accessing data
     */
    boolean isOnVacation(long matricula, LocalDate date) throws DataAccessException;
    
    /**
     * Delete a specific vacation period.
     * 
     * @param matricula The medico's matricula
     * @param fechaInicio The start date of the vacation period
     * @param fechaFin The end date of the vacation period
     * @return true if the vacation was deleted, false if not found
     * @throws DataAccessException if there's an error accessing data
     */
    boolean delete(long matricula, LocalDate fechaInicio, LocalDate fechaFin) throws DataAccessException;
    
    /**
     * Delete all vacation periods for a specific medico.
     * 
     * @param matricula The medico's matricula
     * @return true if any vacations were deleted
     * @throws DataAccessException if there's an error accessing data
     */
    boolean deleteByMatricula(long matricula) throws DataAccessException;
    
    /**
     * Create a new vacation period using transaction logic with proper validation.
     * This method implements the same business rules as the SQL transaction:
     * - Validates date range (start <= end)
     * - Validates medico exists
     * - Checks for overlapping vacations
     * - Checks for guard duty conflicts
     * - Uses SERIALIZABLE isolation level
     * 
     * @param vacaciones The vacation to create
     * @return The created vacation
     * @throws DataAccessException if there's an error accessing data
     * @throws IllegalArgumentException if business rules are violated
     */
    Vacaciones createWithTransaction(Vacaciones vacaciones) throws DataAccessException;
    
    /**
     * Update an existing vacation period using transaction logic.
     * Deletes the old vacation and creates a new one with validation.
     * 
     * @param oldVacaciones The old vacation to be replaced
     * @param newVacaciones The new vacation data
     * @return The updated vacation
     * @throws DataAccessException if there's an error accessing data
     * @throws IllegalArgumentException if business rules are violated
     */
    Vacaciones updateWithTransaction(Vacaciones oldVacaciones, Vacaciones newVacaciones) throws DataAccessException;
}

