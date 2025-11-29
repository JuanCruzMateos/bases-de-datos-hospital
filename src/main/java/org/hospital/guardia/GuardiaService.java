package org.hospital.guardia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
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

        validateGuardiaBusinessRules(guardia);
        validateMedicoEspecialidadYCapacidad(guardia, null);

        // Redundant check kept to preserve previous behaviour
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

        Guardia existente = guardiaDao.findById(guardia.getNroGuardia())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Guardia not found with ID: " + guardia.getNroGuardia()));

        validateGuardiaBusinessRules(guardia);
        validateMedicoEspecialidadYCapacidad(guardia, existente);

        return guardiaDao.update(guardia);
    }

    /**
     * Delete a guardia.
     */
    public boolean deleteGuardia(int nroGuardia) throws DataAccessException {
        logger.info("Service: Deleting guardia with ID: " + nroGuardia);
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

        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        if (guardia.getFechaHora().isBefore(oneYearAgo)) {
            throw new IllegalArgumentException("Fecha hora cannot be more than 1 year in the past");
        }

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

        if (!isFechaHoraWithinTurno(guardia.getFechaHora(), guardia.getIdTurno())) {
            throw new IllegalArgumentException(
                    "La hora ingresada no coincide con el horario del turno seleccionado.\n"
                            + "Use una hora entre 07:00 y 13:00 para el turno Manana, "
                            + "entre 13:00 y 19:00 para el turno Tarde, "
                            + "o entre 19:00 y 07:00 para el turno Noche."
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
            case 1: // Manana 07:00 - 13:00
                return minutes >= 7 * 60 && minutes < 13 * 60;
            case 2: // Tarde 13:00 - 19:00
                return minutes >= 13 * 60 && minutes < 19 * 60;
            case 3: // Noche 19:00 - 07:00 (cruza medianoche)
                return minutes >= 19 * 60 || minutes < 7 * 60;
            default:
                return false;
        }
    }

    /**
     * Valida que el medico exista, tenga la especialidad seleccionada y cumpla
     * con las restricciones de guardias/vacaciones/cupo mensual.
     */
    private void validateMedicoEspecialidadYCapacidad(Guardia nueva, Guardia existenteSiUpdate)
            throws DataAccessException {

        Medico medico = medicoDao.findByMatricula(nueva.getMatricula())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medico no encontrado para la matricula: " + nueva.getMatricula()));

        if (!seEspecializaEnDao.existsByMatriculaAndEspecialidad(
                nueva.getMatricula(), nueva.getCodEspecialidad())) {

            throw new IllegalArgumentException(
                    "El medico seleccionado no posee la especialidad elegida "
                            + "o no esta registrado para atender en ella.");
        }

        validateNoGuardiasConsecutivas(nueva, existenteSiUpdate);
        validateMedicoFueraDeVacaciones(nueva, medico);
        validateMaxGuardiasMensuales(nueva, medico, existenteSiUpdate);
    }

    /**
     * Verifica que el medico no supere su maximo de guardias por mes.
     * Usa maxCantGuardia definido en Medico.
     */
    private void validateMaxGuardiasMensuales(Guardia nueva, Medico medico, Guardia existenteSiUpdate)
            throws DataAccessException {

        int maxMensual = medico.getMaxCantGuardia();
        if (maxMensual <= 0) {
            return;
        }

        LocalDateTime fechaNueva = nueva.getFechaHora();
        int yearNueva = fechaNueva.getYear();
        int monthNueva = fechaNueva.getMonthValue();

        if (existenteSiUpdate != null && existenteSiUpdate.getMatricula() == nueva.getMatricula()) {
            LocalDateTime fechaVieja = existenteSiUpdate.getFechaHora();
            if (fechaVieja != null
                    && fechaVieja.getYear() == yearNueva
                    && fechaVieja.getMonthValue() == monthNueva) {
                return;
            }
        }

        int actuales = guardiaDao.countGuardiasByMedicoAndMonth(nueva.getMatricula(), yearNueva, monthNueva);

        if (actuales >= maxMensual) {
            throw new IllegalArgumentException(
                    "El medico seleccionado ya alcanzo su maximo de guardias mensuales ("
                            + maxMensual + ") para " + fechaNueva.getMonth() + " de " + yearNueva + "."
            );
        }
    }

    /**
     * Evita guardias en dias consecutivos para el mismo medico.
     */
    private void validateNoGuardiasConsecutivas(Guardia nueva, Guardia existenteSiUpdate)
            throws DataAccessException {

        LocalDate fechaNueva = nueva.getFechaHora().toLocalDate();
        Integer idAExcluir = existenteSiUpdate != null ? existenteSiUpdate.getNroGuardia() : null;

        List<Guardia> guardiasMedico = guardiaDao.findByMedico(nueva.getMatricula());
        for (Guardia guardia : guardiasMedico) {
            if (idAExcluir != null && guardia.getNroGuardia() == idAExcluir) {
                continue;
            }
            if (guardia.getFechaHora() == null) {
                continue;
            }
            LocalDate fechaExistente = guardia.getFechaHora().toLocalDate();
            long diffDias = Math.abs(ChronoUnit.DAYS.between(fechaExistente, fechaNueva));
            if (diffDias == 1) {
                throw new IllegalArgumentException(
                        "El medico ya tiene una guardia el " + fechaExistente
                                + ". No puede tener guardias en dias consecutivos."
                );
            }
        }
    }

    /**
     * Evita guardias durante el periodo de vacaciones configurado del medico.
     */
    private void validateMedicoFueraDeVacaciones(Guardia nueva, Medico medico) {
        String periodoVacaciones = medico.getPeriodoVacaciones();
        Month mesVacaciones = parseSpanishMonth(periodoVacaciones);
        if (mesVacaciones == null) {
            return;
        }

        Month mesGuardia = nueva.getFechaHora().getMonth();
        if (mesGuardia.equals(mesVacaciones)) {
            String etiqueta = periodoVacaciones.trim();
            throw new IllegalArgumentException(
                    "El medico esta de vacaciones en " + etiqueta
                            + " y no puede tomar guardias en ese periodo."
            );
        }
    }

    private Month parseSpanishMonth(String periodoVacaciones) {
        if (periodoVacaciones == null) {
            return null;
        }
        String normalized = periodoVacaciones.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        switch (normalized.toUpperCase()) {
            case "ENERO":
                return Month.JANUARY;
            case "FEBRERO":
                return Month.FEBRUARY;
            case "MARZO":
                return Month.MARCH;
            case "ABRIL":
                return Month.APRIL;
            case "MAYO":
                return Month.MAY;
            case "JUNIO":
                return Month.JUNE;
            case "JULIO":
                return Month.JULY;
            case "AGOSTO":
                return Month.AUGUST;
            case "SEPTIEMBRE":
                return Month.SEPTEMBER;
            case "OCTUBRE":
                return Month.OCTOBER;
            case "NOVIEMBRE":
                return Month.NOVEMBER;
            case "DICIEMBRE":
                return Month.DECEMBER;
            default:
                throw new IllegalArgumentException(
                        "Periodo de vacaciones invalido para el medico: " + periodoVacaciones);
        }
    }
}
