package org.hospital.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.medico.Vacaciones;
import org.hospital.medico.VacacionesService;
import org.hospital.medico.Medico;
import org.hospital.medico.MedicoService;
import org.hospital.ui.view.VacacionesPanel;

/**
 * Controller for Vacaciones (Vacations) operations.
 * Handles user interactions and coordinates between view and service layer.
 * Uses transaction-based operations with proper validation.
 */
public class VacacionesController extends BaseController {
    private VacacionesPanel view;
    private VacacionesService vacacionesService;
    private MedicoService medicoService;
    
    // Cache data for table display
    private List<Medico> medicos;
    
    public VacacionesController(VacacionesPanel view) {
        this.view = view;
        this.vacacionesService = new VacacionesService();
        this.medicoService = new MedicoService();
        initController();
        loadInitialData();
    }
    
    private void initController() {
        // Attach listeners to view components
        view.getBtnCreate().addActionListener(e -> createVacaciones());
        view.getBtnUpdate().addActionListener(e -> updateVacaciones());
        view.getBtnDelete().addActionListener(e -> deleteVacaciones());
        view.getBtnRefresh().addActionListener(e -> loadVacaciones());
        view.getBtnClear().addActionListener(e -> {
            view.clearForm();
            view.getTable().clearSelection();
        });
        
        // Load selected vacaciones to form on table selection
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
            }
        });
    }
    
    private void loadInitialData() {
        loadVacaciones();
    }
    
    private void loadVacaciones() {
        try {
            logger.info("Loading all vacaciones");
            reloadDropdownData();
            List<Vacaciones> vacacionesList = vacacionesService.getAllVacaciones();
            view.updateTable(vacacionesList, medicos);
            view.getTable().clearSelection();
            view.clearForm();
            logger.fine("Loaded " + vacacionesList.size() + " vacaciones");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createVacaciones() {
        try {
            logger.info("User initiating create vacaciones");
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Create Vacaciones object
            Vacaciones vacaciones = new Vacaciones();
            vacaciones.setMatricula(view.getSelectedMedico().getMedico().getMatricula());
            vacaciones.setFechaInicio(LocalDate.parse(view.getFechaInicio()));
            vacaciones.setFechaFin(LocalDate.parse(view.getFechaFin()));
            
            // Save via service layer (includes transaction logic and validation)
            vacacionesService.createVacaciones(vacaciones);
            
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                vacaciones.getFechaInicio(), vacaciones.getFechaFin()) + 1;
            
            showSuccess(
                "Vacaciones created successfully!\n" +
                "Medico: " + view.getSelectedMedico().getMedico().getNombre() + " " + 
                view.getSelectedMedico().getMedico().getApellido() + "\n" +
                "Period: " + vacaciones.getFechaInicio() + " to " + vacaciones.getFechaFin() + "\n" +
                "Duration: " + days + " days"
            );
            
            view.clearForm();
            loadVacaciones();
            
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD (e.g., 2024-12-20)");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void updateVacaciones() {
        try {
            logger.info("User initiating update vacaciones");
            
            // Validate that a vacation is selected
            Vacaciones oldVacaciones = view.getSelectedVacaciones();
            if (oldVacaciones == null) {
                showError("Please select a vacation to update");
                return;
            }
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Create new Vacaciones object with updated values
            Vacaciones newVacaciones = new Vacaciones();
            newVacaciones.setMatricula(view.getSelectedMedico().getMedico().getMatricula());
            newVacaciones.setFechaInicio(LocalDate.parse(view.getFechaInicio()));
            newVacaciones.setFechaFin(LocalDate.parse(view.getFechaFin()));
            
            // Update via service layer (uses transaction logic)
            vacacionesService.updateVacaciones(oldVacaciones, newVacaciones);
            
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                newVacaciones.getFechaInicio(), newVacaciones.getFechaFin()) + 1;
            
            showSuccess(
                "Vacaciones updated successfully!\n" +
                "Medico: " + view.getSelectedMedico().getMedico().getNombre() + " " + 
                view.getSelectedMedico().getMedico().getApellido() + "\n" +
                "New Period: " + newVacaciones.getFechaInicio() + " to " + newVacaciones.getFechaFin() + "\n" +
                "Duration: " + days + " days"
            );
            
            view.clearForm();
            loadVacaciones();
            
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD (e.g., 2024-12-20)");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void deleteVacaciones() {
        try {
            logger.info("User initiating delete vacaciones");
            
            Vacaciones selected = view.getSelectedVacaciones();
            if (selected == null) {
                showError("Please select a vacation to delete");
                return;
            }
            
            if (!showConfirmation(
                "Are you sure you want to delete this vacation?\n" +
                "Period: " + selected.getFechaInicio() + " to " + selected.getFechaFin()
            )) {
                return;
            }
            
            boolean deleted = vacacionesService.deleteVacaciones(
                selected.getMatricula(),
                selected.getFechaInicio(),
                selected.getFechaFin()
            );
            
            if (deleted) {
                showSuccess("Vacation deleted successfully!");
                view.clearForm();
                loadVacaciones();
            } else {
                showError("Vacation not found");
            }
            
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getSelectedMedico() == null) {
            showError("Please select a Medico");
            return false;
        }
        
        String fechaInicio = view.getFechaInicio();
        if (fechaInicio.isEmpty()) {
            showError("Fecha Inicio is required");
            return false;
        }
        
        String fechaFin = view.getFechaFin();
        if (fechaFin.isEmpty()) {
            showError("Fecha Fin is required");
            return false;
        }
        
        // Try to parse dates to validate format
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            
            if (inicio.isAfter(fin)) {
                showError("Fecha Inicio must be before or equal to Fecha Fin");
                return false;
            }
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD (e.g., 2024-12-20)");
            return false;
        }
        
        return true;
    }

    private void reloadDropdownData() throws DataAccessException {
        medicos = medicoService.getAllMedicos();
        view.loadMedicos(medicos);
    }
}

