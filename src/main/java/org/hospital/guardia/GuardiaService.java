package org.hospital.guardia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.exception.DataAccessException;
import org.hospital.medico.Medico;
import org.hospital.medico.MedicoDao;
import org.hospital.medico.MedicoDaoImpl;
import org.hospital.medico.SeEspecializaEnDao;
import org.hospital.medico.SeEspecializaEnDaoImpl;


/**
 * Service layer for Guardia business logic.
 */
public class GuardiaService {
    private static final Logger logger = Logger.getLogger(GuardiaService.class.getName());
    private final GuardiaDao guardiaDao;
    private final MedicoDao medicoDao;
    private final SeEspecializaEnDao seEspecializaEnDao;

    public GuardiaService(GuardiaDao guardiaDao, MedicoDao medicoDao, SeEspecializaEnDao seEspecializaEnDao) {
        this.guardiaDao = guardiaDao;
        this.medicoDao = medicoDao;
        this.seEspecializaEnDao = seEspecializaEnDao;
    }

    public GuardiaService() {
        this(new GuardiaDaoImpl(), new MedicoDaoImpl(), new SeEspecializaEnDaoImpl());
    }

    /**
     * Create a new guardia with business logic validation.
     */
    public Guardia createGuardia(Guardia guardia) throws DataAccessException {
        logger.info("Service: Creating new guardia for medico: " + guardia.getMatricula());
        
        // Reglas generales (fechas, turno, etc.)
        validateGuardiaBusinessRules(guardia);

        // Médico debe existir, tener la especialidad y no superar su máximo mensual
        validateMedicoEspecialidadYCapacidad(guardia, null);
        
        // Verify medico exists
        if (!medicoDao.findByMatricula(guardia.getMatricula()).isPresent()) {
            throw new IllegalArgumentException("Medico not found with matricula: " + guardia.getMatricula());
        }
        
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
        
        // Verificar que la guardia exista
        Guardia existente = guardiaDao.findById(guardia.getNroGuardia())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Guardia not found with ID: " + guardia.getNroGuardia()));

        // Reglas generales
        validateGuardiaBusinessRules(guardia);

        // Médico + especialidad válidos y no superar máximo mensual
        validateMedicoEspecialidadYCapacidad(guardia, existente);

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

        // La hora debe coincidir con el horario del turno
        if (!isFechaHoraWithinTurno(guardia.getFechaHora(), guardia.getIdTurno())) {
            throw new IllegalArgumentException(
                "La hora ingresada no coincide con el horario del turno seleccionado.\n" +
                "Use una hora entre 07:00 y 13:00 para el turno Mañana, " +
                "entre 13:00 y 19:00 para el turno Tarde, " +
                "o entre 19:00 y 07:00 para el turno Noche."
            );
        }
    }

        /**
     * Verifica que la hora de fechaHora caiga dentro del intervalo del turno.
     * Turno 1: 07:00-13:00
     * Turno 2: 13:00-19:00
     * Turno 3: 19:00-07:00 (noche, cruza medianoche)
     */
    private boolean isFechaHoraWithinTurno(LocalDateTime fechaHora, int idTurno) {
        int minutes = fechaHora.getHour() * 60 + fechaHora.getMinute();

        switch (idTurno) {
            case 1: // Mañana 07:00 - 13:00
                return minutes >= 7 * 60 && minutes < 13 * 60;

            case 2: // Tarde 13:00 - 19:00
                return minutes >= 13 * 60 && minutes < 19 * 60;

            case 3: // Noche 19:00 - 07:00 (cruza medianoche)
                return minutes >= 19 * 60 || minutes < 7 * 60;

            default:
                // Si el turno es desconocido, lo consideramos inválido
                return false;
        }
    }

        /**
     * Valida que:
     *  - el médico exista
     *  - el médico tenga la especialidad seleccionada (SE_ESPECIALIZA_EN)
     */
    private void validateMedicoEspecialidadYCapacidad(Guardia nueva, Guardia existenteSiUpdate) throws DataAccessException {

        // 1) Verificar que el médico exista
        Medico medico = medicoDao.findByMatricula(nueva.getMatricula())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Médico no encontrado para la matrícula: " + nueva.getMatricula()));

        // 2) Verificar que tenga la especialidad seleccionada
        if (!seEspecializaEnDao.existsByMatriculaAndEspecialidad(
                nueva.getMatricula(), nueva.getCodEspecialidad())) {

            throw new IllegalArgumentException(
                    "El médico seleccionado no posee la especialidad elegida " +
                    "o no está registrado para atender en ella.");
        }

        // 3) Verificar que no supere su máximo de guardias mensuales
        validateMaxGuardiasMensuales(nueva, medico, existenteSiUpdate);
    }

    /**
 * Verifica que el médico no supere su máximo de guardias por mes.
 * Usa maxCantGuardia definido en Medico.
 */
    private void validateMaxGuardiasMensuales(Guardia nueva, Medico medico, Guardia existenteSiUpdate) throws DataAccessException {

        int maxMensual = medico.getMaxCantGuardia();
        // Si por algún motivo es 0 o negativo, interpretamos como "sin límite"
        if (maxMensual <= 0) {
            return;
        }

        LocalDateTime fechaNueva = nueva.getFechaHora();
        int yearNueva = fechaNueva.getYear();
        int monthNueva = fechaNueva.getMonthValue();

        // Si es un UPDATE y no cambia ni médico ni mes/año, no aumenta el conteo
        if (existenteSiUpdate != null) {
            if (existenteSiUpdate.getMatricula() == nueva.getMatricula()) {
                LocalDateTime fechaVieja = existenteSiUpdate.getFechaHora();
                if (fechaVieja != null &&
                    fechaVieja.getYear() == yearNueva &&
                    fechaVieja.getMonthValue() == monthNueva) {
                    // Misma matrícula y mismo mes/año -> no cambia la cantidad del mes
                    return;
                }
            }
        }

        // Contar guardias del médico en ese mes/año
        int actuales = guardiaDao.countGuardiasByMedicoAndMonth(nueva.getMatricula(), yearNueva, monthNueva);

            if (actuales >= maxMensual) {
                throw new IllegalArgumentException(
                    "El médico seleccionado ya alcanzó su máximo de guardias mensuales (" +
                    maxMensual + ") para " + fechaNueva.getMonth() + " de " + yearNueva + "."
                );
            }
        }

}

