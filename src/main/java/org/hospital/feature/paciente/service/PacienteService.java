package org.hospital.feature.paciente.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.paciente.domain.Paciente;
import org.hospital.feature.paciente.repository.PacienteDao;
import org.hospital.feature.paciente.repository.PacienteDaoImpl;

/**
 * Service layer for Paciente business logic.
 * This layer sits between controllers and DAOs, providing:
 * - Business logic validation
 * - Transaction coordination across multiple DAOs
 * - Centralized logging and error handling
 * - Decoupling of UI from data access
 */
public class PacienteService {
    private static final Logger logger = Logger.getLogger(PacienteService.class.getName());
    private final PacienteDao pacienteDao;

    /**
     * Constructor with dependency injection.
     */
    public PacienteService(PacienteDao pacienteDao) {
        this.pacienteDao = pacienteDao;
    }

    /**
     * Default constructor using default DAO implementation.
     */
    public PacienteService() {
        this(new PacienteDaoImpl());
    }

    private static final Set<String> TIPOS_DOCUMENTO_VALIDOS = 
            new HashSet<>(Arrays.asList("DNI", "LC", "PASAPORTE"));

    private void validateTipoDocumento(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        String normalizado = tipoDocumento.trim().toUpperCase();

        if (!TIPOS_DOCUMENTO_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException(
                "Tipo de documento inválido. Debe ser DNI, LC o PASAPORTE."
            );
        }
    }


    /**
     * Create a new paciente with business logic validation.
     * 
     * @param paciente The paciente to create
     * @return The created paciente
     * @throws DataAccessException if creation fails
     * @throws IllegalArgumentException if business rules are violated
     */
    public Paciente createPaciente(Paciente paciente) throws DataAccessException {
        logger.info("Service: Creating new paciente");
        
        validateTipoDocumento(paciente.getTipoDocumento());
        paciente.setTipoDocumento(paciente.getTipoDocumento().trim().toUpperCase());

        // Business logic validations
        validatePacienteBusinessRules(paciente);
        
        // Check for duplicates
        Optional<Paciente> existing = pacienteDao.findByTipoDocumentoAndNroDocumento(
                paciente.getTipoDocumento(), paciente.getNroDocumento());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Paciente with this document already exists");
        }
        
        return pacienteDao.create(paciente);
    }

    /**
     * Find a paciente by document.
     * 
     * @param tipoDocumento Document type
     * @param nroDocumento Document number
     * @return Optional containing the paciente if found
     * @throws DataAccessException if retrieval fails
     */
    public Optional<Paciente> findPaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.fine("Service: Finding paciente by document");
        return pacienteDao.findByTipoDocumentoAndNroDocumento(tipoDocumento, nroDocumento);
    }

    /**
     * Get all pacientes.
     * 
     * @return List of all pacientes
     * @throws DataAccessException if retrieval fails
     */
    public List<Paciente> getAllPacientes() throws DataAccessException {
        logger.fine("Service: Retrieving all pacientes");
        return pacienteDao.findAll();
    }

    /**
     * Update an existing paciente.
     * 
     * @param paciente The paciente to update
     * @return The updated paciente
     * @throws DataAccessException if update fails
     * @throws IllegalArgumentException if business rules are violated
     */
    public Paciente updatePaciente(Paciente paciente) throws DataAccessException {
        logger.info("Service: Updating paciente");
        
        validateTipoDocumento(paciente.getTipoDocumento());
        paciente.setTipoDocumento(paciente.getTipoDocumento().trim().toUpperCase());

        // Business logic validations
        validatePacienteBusinessRules(paciente);
        
        // Verify paciente exists
        Optional<Paciente> existing = pacienteDao.findByTipoDocumentoAndNroDocumento(
                paciente.getTipoDocumento(), paciente.getNroDocumento());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Paciente not found");
        }
        
        return pacienteDao.update(paciente);
    }

    /**
     * Delete a paciente.
     * 
     * @param tipoDocumento Document type
     * @param nroDocumento Document number
     * @return true if deleted, false if not found
     * @throws DataAccessException if deletion fails
     */
    public boolean deletePaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.info("Service: Deleting paciente");
        
        // Here you could add business logic checks:
        // - Check if paciente has active internaciones
        // - Check if paciente has pending appointments
        // - etc.
        
        return pacienteDao.delete(tipoDocumento, nroDocumento);
    }

    /**
     * Validate business rules for a paciente.
     * 
     * @param paciente The paciente to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePacienteBusinessRules(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente cannot be null");
        }
        
        // Validate age (for example, must be born before today)
        if (paciente.getFechaNacimiento() != null && 
            paciente.getFechaNacimiento().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        
        // Validate sex is one of allowed values
        char sexo = paciente.getSexo();
        if (sexo != 'M' && sexo != 'F' && sexo != 'X') {
            throw new IllegalArgumentException("Sex must be M, F, or X");
        }
        
        // Additional business rules can be added here
    }
}

