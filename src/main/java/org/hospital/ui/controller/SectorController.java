package org.hospital.ui.controller;

import java.util.List;
import org.hospital.exception.DataAccessException;
import org.hospital.internacion.Sector;
import org.hospital.internacion.SectorService;
import org.hospital.ui.view.SectorPanel;

/**
 * Controller for Sector operations.
 * Uses service layer for business logic and data access.
 */
public class SectorController extends BaseController {
    private SectorPanel view;
    private SectorService service;
    
    public SectorController(SectorPanel view) {
        this.view = view;
        this.service = new SectorService();
        initController();
        loadSectores();
    }
    
    private void initController() {
        view.getBtnCreate().addActionListener(e -> createSector());
        view.getBtnUpdate().addActionListener(e -> updateSector());
        view.getBtnDelete().addActionListener(e -> deleteSector());
        view.getBtnRefresh().addActionListener(e -> loadSectores());
        view.getBtnClear().addActionListener(e -> view.clearForm());
        
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                view.loadSelectedToForm();
            }
        });
    }
    
    private void loadSectores() {
        try {
            logger.info("Loading all sectores");
            List<Sector> sectores = service.getAllSectores();
            view.updateTable(sectores);
            logger.fine("Loaded " + sectores.size() + " sectores");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        }
    }
    
    private void createSector() {
        try {
            logger.info("User initiating create sector");
            
            if (view.getDescripcion().isEmpty()) {
                showError("Descripción is required");
                return;
            }
            
            Sector sector = new Sector(0, view.getDescripcion());
            service.createSector(sector);
            showSuccess("Sector created successfully!");
            view.clearForm();
            loadSectores();
            
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void updateSector() {
        try {
            logger.info("User initiating update sector");
            
            if (view.getIdSector().isEmpty() || view.getDescripcion().isEmpty()) {
                showError("ID and Descripción are required");
                return;
            }
            
            int id = Integer.parseInt(view.getIdSector());
            Sector sector = new Sector(id, view.getDescripcion());
            service.updateSector(sector);
            showSuccess("Sector updated successfully!");
            view.clearForm();
            loadSectores();
            
        } catch (NumberFormatException e) {
            showError("Invalid ID format");
        } catch (IllegalArgumentException e) {
            showError("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    private void deleteSector() {
        try {
            logger.info("User initiating delete sector");
            
            if (view.getIdSector().isEmpty()) {
                showError("Please select a sector to delete");
                return;
            }
            
            if (!showConfirmation("Are you sure you want to delete this sector?")) {
                return;
            }
            
            int id = Integer.parseInt(view.getIdSector());
            boolean deleted = service.deleteSector(id);
            if (deleted) {
                showSuccess("Sector deleted successfully!");
                view.clearForm();
                loadSectores();
            } else {
                showError("Sector not found");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid ID format");
        } catch (DataAccessException e) {
            handleDataAccessException(e);
        } catch (Exception e) {
            handleException(e);
        }
    }
}

