package org.hospital.feature.internacion.controller;

import java.util.List;

import org.hospital.common.controller.BaseController;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.internacion.domain.Habitacion;
import org.hospital.feature.internacion.service.HabitacionService;
import org.hospital.feature.internacion.ui.HabitacionPanel;
import org.hospital.feature.internacion.domain.Cama;


/**
 * Controller for Habitacion (Room) operations.
 * Uses service layer for business logic and data access.
 */
public class HabitacionController extends BaseController {
    private HabitacionPanel view;
    private HabitacionService service;
    
    public HabitacionController(HabitacionPanel view) {
        this.view = view;
        this.service = new HabitacionService();
        initController();
        loadHabitaciones();
    }
    
    private void initController() {
        // Botones de HABITACION (lo que ya tenías)
        view.getBtnCreate().addActionListener(e -> createHabitacion());
        view.getBtnUpdate().addActionListener(e -> updateHabitacion());
        view.getBtnDelete().addActionListener(e -> deleteHabitacion());
        view.getBtnRefresh().addActionListener(e -> loadHabitaciones());
        view.getBtnClear().addActionListener(e -> {
            view.clearForm();
            // opcional: también vaciar tabla de camas
            view.updateCamasTable(java.util.Collections.emptyList());
            view.setNroCama("");
        });

        // NUEVO: botones de CAMAS
        view.getBtnAddCama().addActionListener(e -> addCama());
        view.getBtnDeleteCama().addActionListener(e -> deleteCama());
        
        // Cuando seleccionás una habitación, cargamos form + camas
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
                loadCamasForSelectedHabitacion();   // <--- método nuevo
            }
        });
    }
    
    /* ==================== HABITACIONES ==================== */

    private void loadHabitaciones() {
        try {
            logger.info("Loading all habitaciones");
            List<Habitacion> habitaciones = service.getAllHabitaciones();
            view.updateTable(habitaciones);
            logger.fine("Loaded " + habitaciones.size() + " habitaciones");

            // si no hay selección, limpiamos camas
            view.updateCamasTable(java.util.Collections.emptyList());
            view.setNroCama("");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createHabitacion() {
        try {
            logger.info("User initiating create habitacion");
            
            if (!validateInputs()) return;
            
            int piso = Integer.parseInt(view.getPiso());
            int idSector = Integer.parseInt(view.getIdSector());
            
            // Para alta: nroHabitacion = 0, lo genera la BD
            Habitacion habitacion = new Habitacion(0, piso, view.getOrientacion(), idSector);

            service.createHabitacion(habitacion);
            showSuccess("Habitación created successfully!");
            view.clearForm();
            loadHabitaciones();
            
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void updateHabitacion() {
        try {
            logger.info("User initiating update habitacion");
            
            if (view.getNroHabitacion().isEmpty() || !validateInputs()) return;
            
            int nroHab   = Integer.parseInt(view.getNroHabitacion());
            int piso     = Integer.parseInt(view.getPiso());
            int idSector = Integer.parseInt(view.getIdSector());
            
            Habitacion habitacion = new Habitacion(
                nroHab,
                piso,
                view.getOrientacion(),
                idSector
            );
            
            service.updateHabitacion(habitacion);
            showSuccess("Habitación updated successfully!");
            view.clearForm();
            loadHabitaciones();
            
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void deleteHabitacion() {
        try {
            logger.info("User initiating delete habitacion");
            
            if (view.getNroHabitacion().isEmpty()) {
                showError("Please select a habitación to delete");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this habitación?")) {
                return;
            }
            
            int nroHab = Integer.parseInt(view.getNroHabitacion());
            boolean deleted = service.deleteHabitacion(nroHab);
            if (deleted) {
                showSuccess("Habitación deleted successfully!");
                view.clearForm();
                loadHabitaciones();
            } else {
                showError("Habitación not found");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        } catch (DataAccessException e) {
            // si hay camas con historial, el DAO/BD tirará error de FK
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getPiso().isEmpty()) {
            showError("Piso is required");
            return false;
        }
        if (view.getIdSector().isEmpty()) {
            showError("ID Sector is required");
            return false;
        }
        return true;
    }

    /* ==================== CAMAS ==================== */

    /** Cargar las camas de la habitación seleccionada en la tabla de la derecha */
    private void loadCamasForSelectedHabitacion() {
        try {
            String nroHabStr = view.getNroHabitacion();
            if (nroHabStr == null || nroHabStr.trim().isEmpty()) {
                view.updateCamasTable(java.util.Collections.emptyList());
                view.setNroCama("");
                return;
            }

            int nroHab = Integer.parseInt(nroHabStr);
            List<Cama> camas = service.getCamasByHabitacion(nroHab); 
            view.updateCamasTable(camas);
            view.setNroCama(""); // limpiamos campo cama

        } catch (NumberFormatException e) {
            // algo raro con el nro de habitación, limpiamos
            view.updateCamasTable(java.util.Collections.emptyList());
            view.setNroCama("");
            logger.warning("Invalid nroHabitacion when loading camas: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** Alta de cama para la habitación seleccionada */
    private void addCama() {
        try {
            logger.info("User initiating add cama");

            String habTxt = view.getNroHabitacion();
            String camaTxt = view.getNroCama();

            if (habTxt == null || habTxt.trim().isEmpty()) {
                showError("Debe seleccionar una habitación para agregar una cama.");
                return;
            }
            if (camaTxt == null || camaTxt.trim().isEmpty()) {
                showError("Debe ingresar el número de cama.");
                return;
            }

            int nroHab = Integer.parseInt(habTxt.trim());
            int nroCama = Integer.parseInt(camaTxt.trim());

            service.agregarCama(nroHab, nroCama);   // llama a sp_agregar_cama

            showSuccess("Cama creada correctamente.");
            loadCamasForSelectedHabitacion();

        } catch (NumberFormatException e) {
            showError("Nro Habitación y Nro Cama deben ser numéricos.");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /** Baja / desactivación de cama según historial */
    private void deleteCama() {
        try {
            logger.info("User initiating delete cama");

            String habTxt = view.getNroHabitacion();
            String camaTxt = view.getNroCama();

            if (habTxt == null || habTxt.trim().isEmpty()) {
                showError("Debe seleccionar una habitación.");
                return;
            }
            if (camaTxt == null || camaTxt.trim().isEmpty()) {
                showError("Debe seleccionar o ingresar una cama.");
                return;
            }

            int nroHab = Integer.parseInt(habTxt.trim());
            int nroCama = Integer.parseInt(camaTxt.trim());

            if (!showConfirmation("¿Eliminar / desactivar cama " + nroCama + " de la habitación " + nroHab + "?")) {
                return;
            }

            service.eliminarODesactivarCama(nroHab, nroCama); // sp_eliminar_o_desactivar_cama

            showSuccess("Cama eliminada / desactivada correctamente.");
            loadCamasForSelectedHabitacion();

        } catch (NumberFormatException e) {
            showError("Nro Habitación y Nro Cama deben ser numéricos.");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
}


