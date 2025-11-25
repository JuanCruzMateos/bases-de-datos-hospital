package org.hospital.medico;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import org.hospital.exception.DataAccessException;

/**
 * Service layer for Medico business logic.
 */
public class MedicoService {
    private static final Logger logger = Logger.getLogger(MedicoService.class.getName());
    private final MedicoDao medicoDao;
    private final EspecialidadDao especialidadDao;

    public MedicoService(MedicoDao medicoDao, EspecialidadDao especialidadDao) {
        this.medicoDao = medicoDao;
        this.especialidadDao = especialidadDao;
    }

    public MedicoService() {
        this(new MedicoDaoImpl(), new EspecialidadDaoImpl());
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
     * Create a new medico with business logic validation.
     */
    public Medico createMedico(Medico medico) throws DataAccessException {
        logger.info("Service: Creating new medico with matricula: " + medico.getMatricula());
        
        // Normalizamos y validamos tipo doc
        validateTipoDocumento(medico.getTipoDocumento());
        medico.setTipoDocumento(medico.getTipoDocumento().trim().toUpperCase());

        validateMedicoBusinessRules(medico);
        
        // Check for duplicate matricula
        Optional<Medico> existing = medicoDao.findByMatricula(medico.getMatricula());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Medico with this matricula already exists");
        }
        
        return medicoDao.create(medico);
    }

    /**
     * Find a medico by matricula.
     */
    public Optional<Medico> findMedico(long matricula) throws DataAccessException {
        logger.fine("Service: Finding medico by matricula: " + matricula);
        return medicoDao.findByMatricula(matricula);
    }

    /**
     * Get all medicos.
     */
    public List<Medico> getAllMedicos() throws DataAccessException {
        logger.fine("Service: Retrieving all medicos");
        return medicoDao.findAll();
    }

    /**
     * Update an existing medico.
     */
    public Medico updateMedico(Medico medico) throws DataAccessException {
        logger.info("Service: Updating medico with matricula: " + medico.getMatricula());
        
        validateTipoDocumento(medico.getTipoDocumento());
        medico.setTipoDocumento(medico.getTipoDocumento().trim().toUpperCase());


        validateMedicoBusinessRules(medico);
        
        // Verify medico exists
        Optional<Medico> existing = medicoDao.findByMatricula(medico.getMatricula());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + medico.getMatricula());
        }
        
        return medicoDao.update(medico);
    }

    /**
     * Delete a medico.
     */
    public boolean deleteMedico(long matricula) throws DataAccessException {
        logger.info("Service: Deleting medico with matricula: " + matricula);
        
        // Business logic: Check if medico has active guardias or internaciones
        // This would require additional queries to related tables
        // For now, we'll allow deletion (could be enhanced)
        
        return medicoDao.delete(matricula);
    }

    /**
     * Get all available especialidades.
     */
    public List<Especialidad> getAllEspecialidades() throws DataAccessException {
        logger.fine("Service: Retrieving all especialidades");
        return especialidadDao.findAll();
    }

    /**
     * Add an especialidad to a medico.
     */
    public Medico addEspecialidad(long matricula, int codEspecialidad) throws DataAccessException {
        logger.info("Service: Adding especialidad " + codEspecialidad + " to medico " + matricula);
        
        // Verify medico exists
        Optional<Medico> medicoOpt = medicoDao.findByMatricula(matricula);
        if (!medicoOpt.isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + matricula);
        }
        
        // Verify especialidad exists
        Optional<Especialidad> espOpt = especialidadDao.findByCodEspecialidad(codEspecialidad);
        if (!espOpt.isPresent()) {
            throw new IllegalArgumentException("Especialidad not found with code: " + codEspecialidad);
        }
        
        Medico medico = medicoOpt.get();
        Especialidad especialidad = espOpt.get();
        
        // Check if already has this especialidad
        if (medico.getEspecialidades().stream()
                .anyMatch(e -> e.getCodEspecialidad() == codEspecialidad)) {
            throw new IllegalArgumentException("Medico already has this especialidad");
        }
        
        // Add especialidad
        Set<Especialidad> especialidades = new HashSet<>(medico.getEspecialidades());
        especialidades.add(especialidad);
        medico.setEspecialidades(especialidades);
        
        return medicoDao.update(medico);
    }

    /**
     * Remove an especialidad from a medico.
     */
    public Medico removeEspecialidad(long matricula, int codEspecialidad) throws DataAccessException {
        logger.info("Service: Removing especialidad " + codEspecialidad + " from medico " + matricula);
        
        // Verify medico exists
        Optional<Medico> medicoOpt = medicoDao.findByMatricula(matricula);
        if (!medicoOpt.isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + matricula);
        }
        
        Medico medico = medicoOpt.get();
        
        // Check if has only one especialidad
        if (medico.getEspecialidades().size() <= 1) {
            throw new IllegalArgumentException("Cannot remove last especialidad. Medico must have at least one.");
        }
        
        // Remove especialidad
        Set<Especialidad> especialidades = new HashSet<>(medico.getEspecialidades());
        boolean removed = especialidades.removeIf(e -> e.getCodEspecialidad() == codEspecialidad);
        
        if (!removed) {
            throw new IllegalArgumentException("Medico does not have this especialidad");
        }
        
        medico.setEspecialidades(especialidades);
        return medicoDao.update(medico);
    }

    /**
     * Validate business rules for a medico.
     */
    private void validateMedicoBusinessRules(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("Medico cannot be null");
        }
        
        if (medico.getMatricula() <= 0) {
            throw new IllegalArgumentException("Matricula must be positive");
        }
        
        // Validate fecha ingreso is not in the future
        if (medico.getFechaIngreso() != null && 
            medico.getFechaIngreso().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Fecha ingreso cannot be in the future");
        }
        
        // Validate max cant guardia is reasonable
        if (medico.getMaxCantGuardia() < 0 || medico.getMaxCantGuardia() > 31) {
            throw new IllegalArgumentException("Max cant guardia must be between 0 and 31");
        }
        
        // Validate has at least one especialidad
        if (medico.getEspecialidades() == null || medico.getEspecialidades().isEmpty()) {
            throw new IllegalArgumentException("Medico must have at least one especialidad");
        }
    }
}

