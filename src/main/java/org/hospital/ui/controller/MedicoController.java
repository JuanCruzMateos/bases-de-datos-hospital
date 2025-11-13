package org.hospital.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hospital.exception.DataAccessException;
import org.hospital.medico.Especialidad;
import org.hospital.medico.Medico;
import org.hospital.medico.MedicoService;
import org.hospital.ui.view.MedicoPanel;

/**
 * Controller for Medico (Doctor) operations with Especialidad management.
 * Uses service layer for business logic and data access.
 */
public class MedicoController extends BaseController {
    private MedicoPanel view;
    private MedicoService service;
    private Medico currentMedico; // Currently selected/loaded medico
    
    public MedicoController(MedicoPanel view) {
        this.view = view;
        this.service = new MedicoService();
        initController();
        loadAvailableEspecialidades();
        loadMedicos();
    }
    
    private void initController() {
        // CRUD operations
        view.getBtnCreate().addActionListener(e -> createMedico());
        view.getBtnUpdate().addActionListener(e -> updateMedico());
        view.getBtnDelete().addActionListener(e -> deleteMedico());
        view.getBtnRefresh().addActionListener(e -> {
            loadMedicos();
            loadAvailableEspecialidades();
        });
        view.getBtnClear().addActionListener(e -> {
            view.clearForm();
            currentMedico = null;
        });
        
        // Especialidad management
        view.getBtnAddEspecialidad().addActionListener(e -> addEspecialidadToMedico());
        view.getBtnRemoveEspecialidad().addActionListener(e -> removeEspecialidadFromMedico());
        
        // Table selection
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedToForm();
            }
        });
    }
    
    private void loadMedicos() {
        try {
            logger.info("Loading all medicos");
            List<Medico> medicos = service.getAllMedicos();
            view.updateTable(medicos);
            logger.fine("Loaded " + medicos.size() + " medicos");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void loadAvailableEspecialidades() {
        try {
            List<Especialidad> especialidades = service.getAllEspecialidades();
            view.loadAvailableEspecialidades(especialidades);
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void loadSelectedToForm() {
        try {
            Medico selected = view.getSelectedMedico();
            if (selected == null) return;
            
            // Fetch full medico from service
            Optional<Medico> medicoOpt = service.findMedico(selected.getMatricula());
            if (!medicoOpt.isPresent()) {
                showError("Medico not found");
                return;
            }
            
            currentMedico = medicoOpt.get();
            
            // Load to form
            view.setMatricula(String.valueOf(currentMedico.getMatricula()));
            view.setTipoDocumento(currentMedico.getTipoDocumento());
            view.setNroDocumento(currentMedico.getNroDocumento());
            view.setNombre(currentMedico.getNombre());
            view.setApellido(currentMedico.getApellido());
            view.setCuilCuit(currentMedico.getCuilCuit());
            view.setFechaIngreso(currentMedico.getFechaIngreso() != null ? 
                    currentMedico.getFechaIngreso().toString() : "");
            view.setMaxCantGuardia(String.valueOf(currentMedico.getMaxCantGuardia()));
            view.setPeriodoVacaciones(currentMedico.getPeriodoVacaciones());
            
            // Load especialidades
            view.setCurrentEspecialidades(currentMedico.getEspecialidades());
            
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createMedico() {
        try {
            logger.info("User initiating create medico");
            
            if (!validateInputs()) return;
            
            // Validate at least one especialidad selected
            if (view.getCurrentEspecialidades().isEmpty()) {
                showError("Medico must have at least one especialidad. Use the 'Add Especialidad' button.");
                return;
            }
            
            long matricula = Long.parseLong(view.getMatricula());
            LocalDate fechaIngreso = LocalDate.parse(view.getFechaIngreso());
            int maxCantGuardia = Integer.parseInt(view.getMaxCantGuardia());
            
            Medico medico = new Medico(
                view.getTipoDocumento(),
                view.getNroDocumento(),
                view.getNombre(),
                view.getApellido(),
                "MEDICO",
                matricula,
                view.getCuilCuit(),
                fechaIngreso,
                null, // foto
                maxCantGuardia,
                view.getPeriodoVacaciones(),
                view.getCurrentEspecialidades()
            );
            
            service.createMedico(medico);
            showSuccess("Medico created successfully!");
            view.clearForm();
            currentMedico = null;
            loadMedicos();
            
        } catch (NumberFormatException e) {
            showError("Invalid number format in Matrícula or Max Guardias");
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
    
    private void updateMedico() {
        try {
            logger.info("User initiating update medico");
            
            if (view.getMatricula().isEmpty()) {
                showError("Please select a medico to update");
                return;
            }
            
            if (!validateInputs()) return;
            
            // Validate at least one especialidad
            if (view.getCurrentEspecialidades().isEmpty()) {
                showError("Medico must have at least one especialidad");
                return;
            }
            
            long matricula = Long.parseLong(view.getMatricula());
            LocalDate fechaIngreso = LocalDate.parse(view.getFechaIngreso());
            int maxCantGuardia = Integer.parseInt(view.getMaxCantGuardia());
            
            Medico medico = new Medico(
                view.getTipoDocumento(),
                view.getNroDocumento(),
                view.getNombre(),
                view.getApellido(),
                "MEDICO",
                matricula,
                view.getCuilCuit(),
                fechaIngreso,
                currentMedico != null ? currentMedico.getFoto() : null,
                maxCantGuardia,
                view.getPeriodoVacaciones(),
                view.getCurrentEspecialidades()
            );
            
            service.updateMedico(medico);
            showSuccess("Medico updated successfully!");
            view.clearForm();
            currentMedico = null;
            loadMedicos();
            
        } catch (NumberFormatException e) {
            showError("Invalid number format");
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
    
    private void deleteMedico() {
        try {
            logger.info("User initiating delete medico");
            
            if (view.getMatricula().isEmpty()) {
                showError("Please select a medico to delete");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this medico?")) {
                return;
            }
            
            long matricula = Long.parseLong(view.getMatricula());
            boolean deleted = service.deleteMedico(matricula);
            
            if (deleted) {
                showSuccess("Medico deleted successfully!");
                view.clearForm();
                currentMedico = null;
                loadMedicos();
            } else {
                showError("Medico not found");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid matricula format");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void addEspecialidadToMedico() {
        try {
            MedicoPanel.EspecialidadComboItem selected = view.getSelectedAvailableEspecialidad();
            if (selected == null) {
                showError("Please select an especialidad to add");
                return;
            }
            
            Especialidad especialidad = selected.getEspecialidad();
            
            // Check if already in current list
            if (view.getCurrentEspecialidades().stream()
                    .anyMatch(e -> e.getCodEspecialidad() == especialidad.getCodEspecialidad())) {
                showError("This especialidad is already added");
                return;
            }
            
            // If we have a loaded medico (updating), add via service
            if (currentMedico != null) {
                logger.info("Adding especialidad to existing medico");
                Medico updated = service.addEspecialidad(currentMedico.getMatricula(), 
                        especialidad.getCodEspecialidad());
                currentMedico = updated;
                view.setCurrentEspecialidades(updated.getEspecialidades());
                showSuccess("Especialidad added successfully!");
                loadMedicos();
            } else {
                // Just add to UI list (for new medico creation)
                logger.info("Adding especialidad to form (new medico)");
                Set<Especialidad> current = view.getCurrentEspecialidades();
                current.add(especialidad);
                view.setCurrentEspecialidades(current);
            }
            
        } catch (IllegalArgumentException e) {
            showError("Error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void removeEspecialidadFromMedico() {
        try {
            String selected = view.getSelectedEspecialidadInList();
            if (selected == null) {
                showError("Please select an especialidad to remove");
                return;
            }
            
            int codEspecialidad = Integer.parseInt(selected);
            
            // If we have a loaded medico (updating), remove via service
            if (currentMedico != null) {
                logger.info("Removing especialidad from existing medico");
                
                // Check if it's the last one
                if (currentMedico.getEspecialidades().size() <= 1) {
                    showError("Cannot remove last especialidad. Medico must have at least one.");
                    return;
                }
                
                Medico updated = service.removeEspecialidad(currentMedico.getMatricula(), codEspecialidad);
                currentMedico = updated;
                view.setCurrentEspecialidades(updated.getEspecialidades());
                showSuccess("Especialidad removed successfully!");
                loadMedicos();
            } else {
                // Just remove from UI list (for new medico creation)
                logger.info("Removing especialidad from form (new medico)");
                Set<Especialidad> current = view.getCurrentEspecialidades();
                
                if (current.size() <= 1) {
                    showError("Medico must have at least one especialidad");
                    return;
                }
                
                current.removeIf(e -> e.getCodEspecialidad() == codEspecialidad);
                view.setCurrentEspecialidades(current);
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid especialidad selection");
        } catch (IllegalArgumentException e) {
            showError("Error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getMatricula().isEmpty()) {
            showError("Matrícula is required");
            return false;
        }
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
        if (view.getCuilCuit().isEmpty()) {
            showError("CUIL/CUIT is required");
            return false;
        }
        if (view.getFechaIngreso().isEmpty()) {
            showError("Fecha Ingreso is required");
            return false;
        }
        if (view.getMaxCantGuardia().isEmpty()) {
            showError("Max Cant Guardia is required");
            return false;
        }
        return true;
    }
}

