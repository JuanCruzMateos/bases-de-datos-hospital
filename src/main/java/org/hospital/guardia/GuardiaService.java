package org.hospital.guardia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;
import org.hospital.medico.MedicoDao;
import org.hospital.medico.MedicoDaoImpl;

/**
 * Service layer for Guardia business logic.
 */
public class GuardiaService {
    private static final Logger logger = Logger.getLogger(GuardiaService.class.getName());
    private final GuardiaDao guardiaDao;
    private final MedicoDao medicoDao;

    public GuardiaService(GuardiaDao guardiaDao, MedicoDao medicoDao) {
        this.guardiaDao = guardiaDao;
        this.medicoDao = medicoDao;
    }

    public GuardiaService() {
        this(new GuardiaDaoImpl(), new MedicoDaoImpl());
    }

    /**
     * Create a new guardia with business logic validation.
     */
    public Guardia createGuardia(Guardia guardia) throws DataAccessException {
        logger.info("Service: Creating new guardia for medico: " + guardia.getMatricula());
        
        validateGuardiaBusinessRules(guardia);
        
        // Verify medico exists
        if (!medicoDao.findByMatricula(guardia.getMatricula()).isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + guardia.getMatricula());
        }
        
        // Business rule: Check if medico doesn't exceed max guardias per month
        // This would require counting guardias in the month
        // For now, we'll allow creation (could be enhanced)
        
        return guardiaDao.create(guardia);
    }

    /**
     * Find a guardia by ID.
     */
    public Optional<Guardia> findGuardia(int nroGuardia) throws DataAccessException {
        logger.fine("Service: Finding guardia by ID: " + nroGuardia);
        return guardiaDao.findById(nroGuardia);
    }

    /**
     * Get all guardias.
     */
    public List<Guardia> getAllGuardias() throws DataAccessException {
        logger.fine("Service: Retrieving all guardias");
        return guardiaDao.findAll();
    }

    /**
     * Get guardias by medico.
     */
    public List<Guardia> getGuardiasByMedico(long matricula) throws DataAccessException {
        logger.fine("Service: Retrieving guardias for medico: " + matricula);
        return guardiaDao.findByMedico(matricula);
    }

    /**
     * Get guardias by especialidad.
     */
    public List<Guardia> getGuardiasByEspecialidad(int codEspecialidad) throws DataAccessException {
        logger.fine("Service: Retrieving guardias for especialidad: " + codEspecialidad);
        return guardiaDao.findByEspecialidad(codEspecialidad);
    }

    /**
     * Update an existing guardia.
     */
    public Guardia updateGuardia(Guardia guardia) throws DataAccessException {
        logger.info("Service: Updating guardia with ID: " + guardia.getNroGuardia());
        
        validateGuardiaBusinessRules(guardia);
        
        // Verify guardia exists
        Optional<Guardia> existing = guardiaDao.findById(guardia.getNroGuardia());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Guardia not found with ID: " + guardia.getNroGuardia());
        }
        
        return guardiaDao.update(guardia);
    }

    /**
     * Delete a guardia.
     */
    public boolean deleteGuardia(int nroGuardia) throws DataAccessException {
        logger.info("Service: Deleting guardia with ID: " + nroGuardia);
        
        // Business logic: Should we prevent deletion of past guardias?
        // For now we allow it
        
        return guardiaDao.delete(nroGuardia);
    }

    /**
     * Validate business rules for a guardia.
     */
    private void validateGuardiaBusinessRules(Guardia guardia) {
        if (guardia == null) {
            throw new IllegalArgumentException("Guardia cannot be null");
        }
        
        if (guardia.getFechaHora() == null) {
            throw new IllegalArgumentException("Fecha hora cannot be null");
        }
        
        // Validate fecha hora is not too far in the past (e.g., more than 1 year)
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        if (guardia.getFechaHora().isBefore(oneYearAgo)) {
            throw new IllegalArgumentException("Fecha hora cannot be more than 1 year in the past");
        }
        
        // Validate fecha hora is not too far in the future (e.g., more than 6 months)
        LocalDateTime sixMonthsAhead = LocalDateTime.now().plusMonths(6);
        if (guardia.getFechaHora().isAfter(sixMonthsAhead)) {
            throw new IllegalArgumentException("Fecha hora cannot be more than 6 months in the future");
        }
        
        if (guardia.getMatricula() <= 0) {
            throw new IllegalArgumentException("Matricula must be positive");
        }
        
        if (guardia.getCodEspecialidad() <= 0) {
            throw new IllegalArgumentException("Cod especialidad must be positive");
        }
        
        if (guardia.getIdTurno() <= 0) {
            throw new IllegalArgumentException("Id turno must be positive");
        }
    }
}

