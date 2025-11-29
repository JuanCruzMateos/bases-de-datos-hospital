package org.hospital.feature.paciente.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.hospital.common.controller.BaseController;
import org.hospital.common.exception.DataAccessException;
import org.hospital.feature.paciente.domain.Paciente;
import org.hospital.feature.paciente.service.PacienteService;
import org.hospital.feature.paciente.ui.PacientePanel;

/**
 * Controller for Paciente (Patient) operations.
 * Handles user interactions and coordinates between view and service layer.
 * Uses service layer instead of direct DAO access for better separation of concerns.
 */
public class PacienteController extends BaseController {
    private PacientePanel view;
    private PacienteService service;
    
    public PacienteController(PacientePanel view) {
        this.view = view;
        this.service = new PacienteService();
        initController();
        loadPacientes();
    }
    
    private void initController() {
        // Attach listeners to view components
        view.getBtnCreate().addActionListener(e -> createPaciente());
        view.getBtnUpdate().addActionListener(e -> updatePaciente());
        view.getBtnDelete().addActionListener(e -> deletePaciente());
        view.getBtnRefresh().addActionListener(e -> loadPacientes());
        view.getBtnClear().addActionListener(e -> view.clearForm());
        
        // Load selected patient to form on table selection
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
            }
        });
    }
    
    private void loadPacientes() {
        try {
            logger.info("Loading all pacientes");
            List<Paciente> pacientes = service.getAllPacientes();
            view.updateTable(pacientes);
            logger.fine("Loaded " + pacientes.size() + " pacientes");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createPaciente() {
        try {
            logger.info("User initiating create paciente");
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Create Paciente object
            Paciente paciente = new Paciente(
                view.getTipoDocumento(),
                view.getNroDocumento(),
                view.getNombre(),
                view.getApellido(),
                "PACIENTE",
                LocalDate.parse(view.getFechaNacimiento()),
                view.getSexo().charAt(0)
            );
            
            // Save via service layer (includes business logic validation)
            service.createPaciente(paciente);
            showSuccess("Paciente created successfully!");
            view.clearForm();
            loadPacientes();
            
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void updatePaciente() {
        try {
            logger.info("User initiating update paciente");
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Create Paciente object
            Paciente paciente = new Paciente(
                view.getTipoDocumento(),
                view.getNroDocumento(),
                view.getNombre(),
                view.getApellido(),
                "PACIENTE",
                LocalDate.parse(view.getFechaNacimiento()),
                view.getSexo().charAt(0)
            );
            
            // Update via service layer
            service.updatePaciente(paciente);
            showSuccess("Paciente updated successfully!");
            view.clearForm();
            loadPacientes();
            
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use YYYY-MM-DD");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void deletePaciente() {
        try {
            logger.info("User initiating delete paciente");
            String tipoDoc = view.getTipoDocumento();
            String nroDoc = view.getNroDocumento();
            
            if (tipoDoc.isEmpty() || nroDoc.isEmpty()) {
                showError("Please enter Tipo Documento and Nro Documento");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this patient?")) {
                return;
            }
            
            boolean deleted = service.deletePaciente(tipoDoc, nroDoc);
            if (deleted) {
                showSuccess("Paciente deleted successfully!");
                view.clearForm();
                loadPacientes();
            } else {
                showError("Paciente not found");
            }
            
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getTipoDocumento().isEmpty()) {
            showError("Tipo Documento is required");
            return false;
        }
        if (view.getNroDocumento().isEmpty()) {
            showError("Nro Documento is required");
            return false;
        }
        if (view.getNombre().isEmpty()) {
            showError("Nombre is required");
            return false;
        }
        if (view.getApellido().isEmpty()) {
            showError("Apellido is required");
            return false;
        }
        if (view.getFechaNacimiento().isEmpty()) {
            showError("Fecha Nacimiento is required");
            return false;
        }
        return true;
    }
}

