package org.hospital.ui.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.guardia.Guardia;
import org.hospital.guardia.GuardiaService;
import org.hospital.guardia.Turno;
import org.hospital.guardia.TurnoDao;
import org.hospital.guardia.TurnoDaoImpl;
import org.hospital.medico.Especialidad;
import org.hospital.medico.Medico;
import org.hospital.medico.MedicoService;
import org.hospital.ui.view.GuardiaPanel;

/**
 * Controller for Guardia (Guard Shifts) operations.
 * Handles user interactions and coordinates between view and service layer.
 */
public class GuardiaController extends BaseController {
    private GuardiaPanel view;
    private GuardiaService guardiaService;
    private MedicoService medicoService;
    private TurnoDao turnoDao;
    
    // Cache data for table display
    private List<Medico> medicos;
    private List<Especialidad> especialidades;
    private List<Turno> turnos;
    
    public GuardiaController(GuardiaPanel view) {
        this.view = view;
        this.guardiaService = new GuardiaService();
        this.medicoService = new MedicoService();
        this.turnoDao = new TurnoDaoImpl();
        initController();
        loadInitialData();
    }
    
    private void initController() {
        // Attach listeners to view components
        view.getBtnCreate().addActionListener(e -> createGuardia());
        view.getBtnUpdate().addActionListener(e -> updateGuardia());
        view.getBtnDelete().addActionListener(e -> deleteGuardia());
        view.getBtnRefresh().addActionListener(e -> loadGuardias());
        view.getBtnClear().addActionListener(e -> {
            view.clearForm();
            view.getTable().clearSelection();
        });
        
        // Load selected guardia to form on table selection
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
            }
        });
    }
    
    private void loadInitialData() {
        loadGuardias();
    }
    
    private void loadGuardias() {
        try {
            reloadDropdownData();
            logger.info("Loading all guardias");
            List<Guardia> guardias = guardiaService.getAllGuardias();
            view.updateTable(guardias, medicos, especialidades, turnos);
            view.getTable().clearSelection();
            view.clearForm();
            logger.fine("Loaded " + guardias.size() + " guardias");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createGuardia() {
        try {
            logger.info("User initiating create guardia");
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            // Create Guardia object
            Guardia guardia = new Guardia();
            // nroGuardia is auto-generated, so we don't set it
            guardia.setFechaHora(LocalDateTime.parse(view.getFechaHora()));
            guardia.setMatricula(view.getSelectedMedico().getMedico().getMatricula());
            guardia.setCodEspecialidad(view.getSelectedEspecialidad().getEspecialidad().getCodEspecialidad());
            guardia.setIdTurno(view.getSelectedTurno().getTurno().getIdTurno());
            
            // Save via service layer (includes business logic validation)
            guardiaService.createGuardia(guardia);
            showSuccess("Guardia created successfully!");
            view.clearForm();
            loadGuardias();
            
        } catch (DateTimeParseException e) {
            showError("Invalid date-time format. Use YYYY-MM-DDTHH:MM (e.g., 2024-12-25T08:00)");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void updateGuardia() {
        try {
            logger.info("User initiating update guardia");
            
            // Validate inputs
            if (!validateInputs()) {
                return;
            }
            
            String nroGuardiaStr = view.getNroGuardia();
            if (nroGuardiaStr.isEmpty()) {
                    showError("Please select a guardia to update");
                return;
            }
            
            // Create Guardia object
            Guardia guardia = new Guardia();
            guardia.setNroGuardia(Integer.parseInt(nroGuardiaStr));
            guardia.setFechaHora(LocalDateTime.parse(view.getFechaHora()));
            guardia.setMatricula(view.getSelectedMedico().getMedico().getMatricula());
            guardia.setCodEspecialidad(view.getSelectedEspecialidad().getEspecialidad().getCodEspecialidad());
            guardia.setIdTurno(view.getSelectedTurno().getTurno().getIdTurno());
            
            // Update via service layer
            guardiaService.updateGuardia(guardia);
            showSuccess("Guardia updated successfully!");
            view.clearForm();
            loadGuardias();
            
        } catch (NumberFormatException e) {
            showError("Invalid Nro Guardia format");
        } catch (DateTimeParseException e) {
            showError("Invalid date-time format. Use YYYY-MM-DDTHH:MM (e.g., 2024-12-25T08:00)");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void deleteGuardia() {
        try {
            logger.info("User initiating delete guardia");
            String nroGuardiaStr = view.getNroGuardia();
            
            if (nroGuardiaStr.isEmpty()) {
                showError("Please select a guardia to delete");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this guardia?")) {
                return;
            }
            
            int nroGuardia = Integer.parseInt(nroGuardiaStr);
            boolean deleted = guardiaService.deleteGuardia(nroGuardia);
            
            if (deleted) {
                showSuccess("Guardia deleted successfully!");
                view.clearForm();
                loadGuardias();
            } else {
                showError("Guardia not found");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid Nro Guardia format");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private boolean validateInputs() {
        if (view.getFechaHora().isEmpty()) {
            showError("Fecha Hora is required");
            return false;
        }
        if (view.getSelectedMedico() == null) {
            showError("Please select a Medico");
            return false;
        }
        if (view.getSelectedEspecialidad() == null) {
            showError("Please select an Especialidad");
            return false;
        }
        if (view.getSelectedTurno() == null) {
            showError("Please select a Turno");
            return false;
        }
        return true;
    }

    private void reloadDropdownData() throws DataAccessException {
        medicos = medicoService.getAllMedicos();
        especialidades = medicoService.getAllEspecialidades();
        turnos = turnoDao.findAll();

        view.loadMedicos(medicos);
        view.loadEspecialidades(especialidades);
        view.loadTurnos(turnos);
    }
}
