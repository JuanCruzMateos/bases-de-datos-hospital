package org.hospital.feature.internacion.service;

import org.hospital.feature.internacion.domain.Cama;
import org.hospital.feature.internacion.domain.Habitacion;
import org.hospital.feature.internacion.domain.Sector;
import org.hospital.feature.internacion.repository.CamaDao;
import org.hospital.feature.internacion.repository.CamaDaoImpl;
import org.hospital.feature.internacion.repository.HabitacionDao;
import org.hospital.feature.internacion.repository.HabitacionDaoImpl;
import org.hospital.feature.internacion.repository.SectorDao;
import org.hospital.feature.internacion.repository.SectorDaoImpl;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hospital.common.exception.DataAccessException;

/**
 * Service layer for Habitacion business logic.
 */
public class HabitacionService {
    private static final Logger logger = Logger.getLogger(HabitacionService.class.getName());

    private final HabitacionDao habitacionDao;
    private final SectorDao sectorDao;
    private final CamaDao camaDao;   // <<< NUEVO

    // Constructor principal: inyección de DAOs
    public HabitacionService(HabitacionDao habitacionDao, SectorDao sectorDao, CamaDao camaDao) {
        this.habitacionDao = habitacionDao;
        this.sectorDao = sectorDao;
        this.camaDao = camaDao;
    }

    // Constructor antiguo (se mantiene) por compatibilidad
    public HabitacionService(HabitacionDao habitacionDao, SectorDao sectorDao) {
        this(habitacionDao, sectorDao, new CamaDaoImpl());
    }

    // Constructor por defecto usado por los controllers
    public HabitacionService() {
        this(new HabitacionDaoImpl(), new SectorDaoImpl(), new CamaDaoImpl());
    }

    /**
     * Create a new habitacion with business logic validation.
     */
    public Habitacion createHabitacion(Habitacion habitacion) throws DataAccessException {
        logger.info("Service: Creating new habitacion in sector: " + habitacion.getIdSector());
        
        validateHabitacionBusinessRules(habitacion);
        
        // Verify sector exists
        Optional<Sector> sector = sectorDao.findById(habitacion.getIdSector());
        if (!sector.isPresent()) {
            throw new IllegalArgumentException("Sector not found with ID: " + habitacion.getIdSector());
        }
        
        Habitacion creada = habitacionDao.create(habitacion);

        // Participaci��n total: toda habitaci��n debe tener al menos una cama libre
        try {
            // Intentamos crear la cama 1. Si la SP decide otro n��mero, igual asegura una cama.
            camaDao.agregarCama(creada.getNroHabitacion(), 1);
        } catch (Exception e) {
            // Si fallamos al crear la cama, revertimos la habitaci��n para no dejarla sin camas
            try {
                habitacionDao.delete(creada.getNroHabitacion());
            } catch (Exception ex) {
                logger.severe("No se pudo revertir la habitacion sin cama creada: " + ex.getMessage());
            }
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }
            throw new DataAccessException("Error creando cama inicial para la habitacion", e);
        }

        return creada;
    }

    /**
     * Find habitacion by ID.
     */
    public Optional<Habitacion> findHabitacion(int nroHabitacion) throws DataAccessException {
        logger.fine("Service: Finding habitacion by ID: " + nroHabitacion);
        return habitacionDao.findById(nroHabitacion);
    }

    /**
     * Get all habitaciones.
     */
    public List<Habitacion> getAllHabitaciones() throws DataAccessException {
        logger.fine("Service: Retrieving all habitaciones");
        return habitacionDao.findAll();
    }

    /**
     * Get habitaciones by sector.
     */
    public List<Habitacion> getHabitacionesBySector(int idSector) throws DataAccessException {
        logger.fine("Service: Retrieving habitaciones for sector: " + idSector);
        return habitacionDao.findBySector(idSector);
    }

    /**
     * Update an existing habitacion.
     */
    public Habitacion updateHabitacion(Habitacion habitacion) throws DataAccessException {
        logger.info("Service: Updating habitacion with ID: " + habitacion.getNroHabitacion());
        
        validateHabitacionBusinessRules(habitacion);
        
        // Verify habitacion exists
        Optional<Habitacion> existing = habitacionDao.findById(habitacion.getNroHabitacion());
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("Habitacion not found with ID: " + habitacion.getNroHabitacion());
        }
        
        // Verify sector exists
        Optional<Sector> sector = sectorDao.findById(habitacion.getIdSector());
        if (!sector.isPresent()) {
            throw new IllegalArgumentException("Sector not found with ID: " + habitacion.getIdSector());
        }
        
        return habitacionDao.update(habitacion);
    }

    /**
     * Delete a habitacion.
     */
    public boolean deleteHabitacion(int nroHabitacion) throws DataAccessException {
        logger.info("Service: Deleting habitacion with ID: " + nroHabitacion);
        
        // Regla de negocio: si hay camas/historial, la FK lo va a frenar
        // (o podríamos hacer chequeos previos si quisiéramos)
        
        return habitacionDao.delete(nroHabitacion);
    }

    /**
     * Validate business rules for a habitacion.
     */
    private void validateHabitacionBusinessRules(Habitacion habitacion) {
        if (habitacion == null) {
            throw new IllegalArgumentException("Habitacion cannot be null");
        }
        
        if (habitacion.getPiso() < 0) {
            throw new IllegalArgumentException("Piso cannot be negative");
        }
        
        if (habitacion.getPiso() > 50) {
            throw new IllegalArgumentException("Piso cannot exceed 50 floors");
        }
        
        if (habitacion.getOrientacion() == null || habitacion.getOrientacion().trim().isEmpty()) {
            throw new IllegalArgumentException("Orientacion cannot be empty");
        }
        
        // Validate orientacion is one of the cardinal directions or combinations
        String orientacion = habitacion.getOrientacion().toUpperCase();
        if (!orientacion.matches("^(N|S|E|O|W|NE|NO|NW|SE|SO|SW|NORTE|SUR|ESTE|OESTE)$")) {
            throw new IllegalArgumentException(
                "Orientacion must be a valid direction (N, S, E, O, NE, NO, SE, SO, etc.)"
            );
        }
        
        if (habitacion.getIdSector() <= 0) {
            throw new IllegalArgumentException("ID Sector must be positive");
        }
    }

    /* ==================== CAMAS ==================== */

    /**
     * Devuelve las camas de una habitación.
     */
    public List<Cama> getCamasByHabitacion(int nroHabitacion) throws DataAccessException {
        return camaDao.findByHabitacion(nroHabitacion);
    }

    /**
     * Agrega una cama a una habitación (usa SP sp_agregar_cama).
     */
    public void agregarCama(int nroHabitacion, int nroCama) throws DataAccessException {
        camaDao.agregarCama(nroHabitacion, nroCama);
    }

    /**
     * Elimina o desactiva una cama según su historial (usa SP sp_eliminar_o_desactivar_cama).
     */
    public void eliminarODesactivarCama(int nroHabitacion, int nroCama) throws DataAccessException {
        camaDao.eliminarODesactivarCama(nroHabitacion, nroCama);
    }
}


