package org.hospital.internacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;
import org.hospital.medico.MedicoDao;
import org.hospital.medico.MedicoDaoImpl;
import org.hospital.paciente.PacienteDao;
import org.hospital.paciente.PacienteDaoImpl;

/**
 * Service layer for Internacion business logic.
 */
public class InternacionService {
    private static final Logger logger = Logger.getLogger(InternacionService.class.getName());
    private final InternacionDao internacionDao;
    private final PacienteDao pacienteDao;
    private final MedicoDao medicoDao;
    private final CamaService camaService;

    public InternacionService(InternacionDao internacionDao, PacienteDao pacienteDao, 
                            MedicoDao medicoDao, CamaService camaService) {
        this.internacionDao = internacionDao;
        this.pacienteDao = pacienteDao;
        this.medicoDao = medicoDao;
        this.camaService = camaService;
    }

    public InternacionService() {
        this(new InternacionDaoImpl(), new PacienteDaoImpl(), new MedicoDaoImpl(), new CamaService());
    }

    // Versión vieja: delega a la nueva con cama/habitación null
    public Internacion createInternacion(Internacion internacion) throws DataAccessException {
        return createInternacion(internacion, null, null);
    }

    // Nueva versión: permite pasar cama/habitación opcionales
    public Internacion createInternacion(Internacion internacion,
                                        Integer nroHabitacion,
                                        Integer nroCama) throws DataAccessException {
        logger.info("Service: Creating new internacion for paciente: " +
                    internacion.getTipoDocumento() + " " + internacion.getNroDocumento());

        validateInternacionBusinessRules(internacion);

        // Verify paciente exists
        if (!pacienteDao.findByTipoDocumentoAndNroDocumento(
                internacion.getTipoDocumento(), internacion.getNroDocumento()).isPresent()) {
            throw new IllegalArgumentException("Paciente not found");
        }

        // Verify medico exists
        if (!medicoDao.findByMatricula(internacion.getMatricula()).isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + internacion.getMatricula());
        }

        // Business rule: Check if paciente already has an active internacion
        List<Internacion> activeInternaciones = internacionDao.findByPaciente(
                internacion.getTipoDocumento(), internacion.getNroDocumento());

        for (Internacion active : activeInternaciones) {
            if (active.getFechaFin() == null) {
                throw new IllegalArgumentException(
                    "Paciente already has an active internacion (ID: " + active.getNroInternacion() + ")");
            }
        }

        // AHORA usamos el nuevo método del DAO que recibe cama/habitación
        return internacionDao.create(internacion, nroHabitacion, nroCama);
    }


    /**
     * Find an internacion by ID.
     */
    public Optional<Internacion> findInternacion(int nroInternacion) throws DataAccessException {
        logger.fine("Service: Finding internacion by ID: " + nroInternacion);
        return internacionDao.findById(nroInternacion);
    }

    /**
     * Get all internaciones.
     */
    public List<Internacion> getAllInternaciones() throws DataAccessException {
        logger.fine("Service: Retrieving all internaciones");
        return internacionDao.findAll();
    }

    /**
     * Get internaciones by paciente.
     */
    public List<Internacion> getInternacionesByPaciente(String tipoDocumento, String nroDocumento) 
            throws DataAccessException {
        logger.fine("Service: Retrieving internaciones for paciente: " + tipoDocumento + " " + nroDocumento);
        return internacionDao.findByPaciente(tipoDocumento, nroDocumento);
    }

    /**
     * Get only active internaciones (fecha_fin is null).
     */
    public List<Internacion> getActivasInternaciones() throws DataAccessException {
        logger.fine("Service: Retrieving active internaciones");
        return internacionDao.findActivasInternaciones();
    }

    /**
     * Update an existing internacion.
     */
    public Internacion updateInternacion(Internacion internacion) throws DataAccessException {
        logger.info("Service: Updating internacion with ID: " + internacion.getNroInternacion());
        
        validateInternacionBusinessRules(internacion);
        
        // Verify internacion exists
        Optional<Internacion> existing = internacionDao.findById(internacion.getNroInternacion());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Internacion not found with ID: " + internacion.getNroInternacion());
        }
        
        return internacionDao.update(internacion);
    }

    /**
     * Close an internacion by setting fecha_fin to today.
     * Also releases the assigned bed.
     */
    public Internacion closeInternacion(int nroInternacion) throws DataAccessException {
        logger.info("Service: Closing internacion with ID: " + nroInternacion);
        
        Optional<Internacion> existing = internacionDao.findById(nroInternacion);
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Internacion not found with ID: " + nroInternacion);
        }
        
        Internacion internacion = existing.get();
        if (internacion.getFechaFin() != null) {
            throw new IllegalArgumentException("Internacion is already closed");
        }
        
        // Release any assigned beds for this internacion
        List<SeUbica> bedAssignments = camaService.getBedAssignmentsByInternacion(nroInternacion);
        for (SeUbica assignment : bedAssignments) {
            camaService.releaseBed(assignment.getNroCama(), assignment.getNroHabitacion());
        }
        
        internacion.setFechaFin(LocalDate.now());
        return internacionDao.update(internacion);
    }

    /**
     * Delete an internacion.
     */
    public boolean deleteInternacion(int nroInternacion) throws DataAccessException {
        logger.info("Service: Deleting internacion with ID: " + nroInternacion);
        
        // Business logic: Should we allow deletion? Or only closing?
        // For now we allow it, but this could be restricted
        
        return internacionDao.delete(nroInternacion);
    }

    /**
     * Assign a bed to an internacion.
     */
    public SeUbica assignBedToInternacion(int nroInternacion, int nroCama, int nroHabitacion) 
            throws DataAccessException {
        logger.info("Service: Assigning bed " + nroCama + " to internacion " + nroInternacion);
        
        // Verify internacion exists
        Optional<Internacion> internacionOpt = internacionDao.findById(nroInternacion);
        if (!internacionOpt.isPresent()) {
            throw new IllegalArgumentException("Internacion not found with ID: " + nroInternacion);
        }
        
        Internacion internacion = internacionOpt.get();
        
        // Business rule: Cannot assign bed to closed internacion
        if (internacion.getFechaFin() != null) {
            throw new IllegalArgumentException("Cannot assign bed to a closed internacion");
        }
        
        return camaService.assignBedToInternacion(nroInternacion, nroCama, nroHabitacion);
    }

    /**
     * Get bed assignments for an internacion.
     */
    public List<SeUbica> getBedAssignments(int nroInternacion) throws DataAccessException {
        logger.fine("Service: Getting bed assignments for internacion: " + nroInternacion);
        return camaService.getBedAssignmentsByInternacion(nroInternacion);
    }

    /**
     * Get all available beds.
     */
    public List<Cama> getAvailableBeds() throws DataAccessException {
        logger.fine("Service: Getting all available beds");
        return camaService.getCamasByEstado(CamaService.ESTADO_DISPONIBLE);
    }

    /**
     * Get available beds in a specific room.
     */
    public List<Cama> getAvailableBedsByRoom(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Getting available beds for room: " + nroHabitacion);
        return camaService.getAvailableCamasByHabitacion(nroHabitacion);
    }

    /**
     * Validate business rules for an internacion.
     */
    private void validateInternacionBusinessRules(Internacion internacion) {
        if (internacion == null) {
            throw new IllegalArgumentException("Internacion cannot be null");
        }
        
        if (internacion.getFechaInicio() == null) {
            throw new IllegalArgumentException("Fecha inicio cannot be null");
        }
        
        // Validate fecha inicio is not in the future
        if (internacion.getFechaInicio().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Fecha inicio cannot be in the future");
        }
        
        // If fecha fin is set, validate it's after fecha inicio
        if (internacion.getFechaFin() != null) {
            if (internacion.getFechaFin().isBefore(internacion.getFechaInicio())) {
                throw new IllegalArgumentException("Fecha fin must be after fecha inicio");
            }
            
            if (internacion.getFechaFin().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Fecha fin cannot be in the future");
            }
        }
        
        if (internacion.getTipoDocumento() == null || internacion.getTipoDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo documento cannot be empty");
        }
        
        if (internacion.getNroDocumento() == null || internacion.getNroDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("Nro documento cannot be empty");
        }
        
        if (internacion.getMatricula() <= 0) {
            throw new IllegalArgumentException("Matricula must be positive");
        }
    }

    public void changeBed(int nroInternacion, Integer nroHabitacion, Integer nroCama) throws DataAccessException {
        if (nroHabitacion == null || nroCama == null) {
            // Nada que hacer
            return;
        }

        // Opcional: verificar que la internación exista
        if (!internacionDao.findById(nroInternacion).isPresent()) {
            throw new IllegalArgumentException("Internacion not found: " + nroInternacion);
        }

        internacionDao.changeBed(nroInternacion, nroHabitacion, nroCama);
    }


}

