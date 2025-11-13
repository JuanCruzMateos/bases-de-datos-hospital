package org.hospital.ui.controller;

import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.internacion.Habitacion;
import org.hospital.internacion.HabitacionService;
import org.hospital.ui.view.HabitacionPanel;

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
        view.getBtnCreate().addActionListener(e -> createHabitacion());
        view.getBtnUpdate().addActionListener(e -> updateHabitacion());
        view.getBtnDelete().addActionListener(e -> deleteHabitacion());
        view.getBtnRefresh().addActionListener(e -> loadHabitaciones());
        view.getBtnClear().addActionListener(e -> view.clearForm());
        
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
            }
        });
    }
    
    private void loadHabitaciones() {
        try {
            logger.info("Loading all habitaciones");
            List<Habitacion> habitaciones = service.getAllHabitaciones();
            view.updateTable(habitaciones);
            logger.fine("Loaded " + habitaciones.size() + " habitaciones");
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
            
            int nroHab = Integer.parseInt(view.getNroHabitacion());
            int piso = Integer.parseInt(view.getPiso());
            int idSector = Integer.parseInt(view.getIdSector());
            
            Habitacion habitacion = new Habitacion(nroHab, piso, view.getOrientacion(), idSector);
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
}

